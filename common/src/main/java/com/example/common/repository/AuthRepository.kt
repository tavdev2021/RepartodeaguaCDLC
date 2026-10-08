package com.example.common.repository

import android.content.Context
import androidx.core.net.toUri
import com.example.common.model.Ruta
import com.example.common.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    ) {
    val currentUser get() = auth.currentUser

    fun getAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 🔴 NUEVO MÉTODO: Registro de Trabajadores realizado por el Admin usando la Instancia Secundaria
    suspend fun registerUserByAdmin(
        context: Context,
        fullName: String,
        routeId: String,
        email: String,
        password: String,
        imagenUrl: String,
        rutaAsignada: String,
        rolAsignado: String
    ): Result<Unit> {
        return try {
            // 1. Inicializar o recuperar la instancia secundaria aislada de Firebase
            val secondaryApp = FirebaseApp.getApps(context).find { it.name == "AdminSecondaryApp" }
                ?: FirebaseApp.initializeApp(
                    context,
                    FirebaseApp.getInstance().options,
                    "AdminSecondaryApp"
                )
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

            // 2. Crear las credenciales del repartidor en la instancia secundaria
            val authResult = secondaryAuth.createUserWithEmailAndPassword(email, password).await()
            val newUser = authResult.user

            if (newUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .setPhotoUri(imagenUrl.toUri())
                    .build()

                newUser.updateProfile(profileUpdates).await()

                // 3. Crear el documento del nuevo repartidor en Firestore
                val userData = mapOf(
                    "uid" to newUser.uid,
                    "nombre" to fullName,
                    "routeId" to routeId,
                    "email" to email,
                    "imagenUrl" to imagenUrl,
                    "ruta" to rutaAsignada,
                    "role" to rolAsignado
                )
                db.collection("usuarios").document(newUser.uid).set(userData).await()

                // 4. Limpiar la sesión secundaria para mantener la del Admin intacta
                secondaryAuth.signOut()

                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al crear las credenciales del trabajador"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Nueva función para obtener el perfil y validar rol
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) { null }
    }

    // Obtener rutas en tiepo real
    fun getRutasFlow(): Flow<List<Ruta>> = callbackFlow {
        val subscription = db.collection("rutas")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val rutas = snapshots?.documents?.mapNotNull { it.toObject(Ruta::class.java) }
                    ?: emptyList()
                trySend(rutas)
            }
        awaitClose { subscription.remove() }
    }

    //Crear o editar ruta
    suspend fun crearOActualizarRuta(ruta: Ruta): Result<Unit> = try {
        db.collection("rutas").document(ruta.id).set(ruta).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
        fun logout() {
            auth.signOut()
        }
    }