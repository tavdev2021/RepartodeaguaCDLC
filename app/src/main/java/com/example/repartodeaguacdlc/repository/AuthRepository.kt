package com.example.repartodeaguacdlc.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri

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

    suspend fun register(fullName: String, email: String, password: String, imagenUrl: String,rutaAsignada: String): Result<Unit> {
    return try {
        // 1. Crear el usuario con email y contraseña.
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()

        // 2. Si la creación es exitosa (no lanza excepción), obtener el usuario...
        val user = authResult.user

        if (user != null) {
            // ...y crear una solicitud para actualizar su perfil.
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                // Aquí también podrías añadir una URL de foto de perfil por defecto si quisieras
                .setPhotoUri(imagenUrl.toUri())
                .build()

            // 3. Aplicar la actualización al perfil del usuario.
            user.updateProfile(profileUpdates).await()
            user.reload().await()

            // 2. Guardar en Firestore la asociación Usuario <-> Ruta
            val userData = mapOf(
                "uid" to user.uid,
                "nombre" to fullName,
                "imagenUrl" to imagenUrl,
                "ruta" to rutaAsignada // Aquí guardamos "Ruta-01"
            )
            db.collection("repartidores").document(user.uid).set(userData).await()

            // 4. Si todo ha ido bien, devolvermos éxito.
            Result.success(Unit)
        } else {
            Result.failure(Exception("Error al obtener el usuario después del registro"))
        }

    } catch (e: Exception)
    {
        // Si algo falla (email ya en uso, contraseña débil, etc.), capturamos el error
        Result.failure(e)
    }
}

    // Función para obtener la ruta del usuario actual
    suspend fun getRutaAsignada(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val document = db.collection("repartidores").document(uid).get().await()
            document.getString("ruta") // Retorna el nombre de la ruta, ej: "Ruta 01"
        } catch (e: Exception) {

            null
        }
    }

        fun logout() {
            auth.signOut()
        }
    }