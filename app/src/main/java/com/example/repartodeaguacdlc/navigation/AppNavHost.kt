package com.example.repartodeaguacdlc.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.repartodeaguacdlc.data.ClientesUpdateViewModelFactory
import com.example.repartodeaguacdlc.data.ClientesViewModelFactory
import com.example.repartodeaguacdlc.view.AddNewClient
import com.example.repartodeaguacdlc.view.ClientDetail
import com.example.repartodeaguacdlc.view.ClientesList
import com.example.repartodeaguacdlc.view.HomeScreen
import com.example.repartodeaguacdlc.view.LoginScreen
import com.example.repartodeaguacdlc.view.PedidosList
import com.example.repartodeaguacdlc.view.RegisterScreen
import com.example.repartodeaguacdlc.view.SettingsApp
import com.example.repartodeaguacdlc.view.SplashScreen
import com.example.repartodeaguacdlc.view.UpdateClientScreen
import com.example.repartodeaguacdlc.view.VentaScreen
import com.example.common.viewmodel.AuthViewModel
import com.example.repartodeaguacdlc.data.VentasViewModelFactory
import com.example.repartodeaguacdlc.viewmodel.ClientesUpdateViewModel
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.example.repartodeaguacdlc.viewmodel.VentasViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost()
{
    val navController = rememberNavController()
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel()
    val globalClientesViewModel: ClientesViewModel = viewModel(
        factory = ClientesViewModelFactory(context)
    )

    val globalVentasViewModel: VentasViewModel = viewModel(
        factory = VentasViewModelFactory(context)
    )

    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()


    // Sincronizacion global, se activa en cuanto el repartidor tiene un routeId
    LaunchedEffect(userProfile?.routeId) {
        userProfile?.routeId?.let { routeId ->
            if (routeId.isNotBlank()) {

                globalClientesViewModel.setRouteId(routeId)

                // Ponemos el listener de clientes para bajar cambios de la nube
                globalClientesViewModel.iniciarSincronizacion(routeId)

                // Despertamos el worker para que suba a firestore los clientes que estan pendientes localmente
                globalClientesViewModel.activarRespaldoPendiente()

                globalVentasViewModel.setRouteId(routeId)

                // Ponemos el listener de ventas para bajar cambios de la nube
                globalVentasViewModel.iniciarSincronizacionContinua(routeId)

                // Despertamos el worker para que suba a firestore las ventas que estan pendientes localmente
                globalVentasViewModel.activarRespaldoPendiente()

                println("Sincronización global iniciada por la ruta $routeId")
            }
        }
    }

    SharedTransitionLayout {

        NavHost(navController = navController, startDestination = "splash") {

            composable("splash") {
                SplashScreen(
                    authViewModel,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    authViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {

                HomeScreen(
                    authViewModel,
                    globalVentasViewModel,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onAddNewClient = {
                        navController.navigate("addnewclient") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onPedidos = {
                        navController.navigate("pedidos")
                    },
                    onClientes = {
                        navController.navigate("clientes")
                    },
                    onSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            composable("addnewclient") {
                val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

                AddNewClient(
                    routeId = userProfile?.routeId ?: "",
                    onNavigateToHomeFromAddNewClient = {
                        navController.navigate("home") {
                            popUpTo("addnewclient") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("pedidos") {
                PedidosList()
            }

            composable("clientes") {

                this@SharedTransitionLayout.ClientesList(
                    clientesViewModel = globalClientesViewModel,
                    animatedVisibilityScope = this,
                    onAddNewClient = {
                        navController.navigate("addnewclient") {
                            popUpTo("clientes") { inclusive = true }
                        }
                    },

                    onClientClick = { clienteId ->
                        globalClientesViewModel.selectClient(clienteId)
                        navController.navigate("clienteDetails/$clienteId")
                    },
                    // Navegacion directa a la venta
                    onVentaClick = { clienteId ->
                        globalClientesViewModel.selectClient(clienteId)
                        navController.navigate("venta/$clienteId")
                    }
                )
            }

            composable(
                "clienteDetails/{clienteId}",
                arguments = listOf(navArgument("clienteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

                LaunchedEffect(clienteId) {
                    globalClientesViewModel.selectClient(clienteId)
                }

                this@SharedTransitionLayout.ClientDetail(
                    animatedVisibilityScope = this,
                    clientesViewModel = globalClientesViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToEdit = { clienteId ->
                        navController.navigate("updateClient/$clienteId")
                    },
                    onNavigateToVenta = { clienteId ->
                        navController.navigate("venta/$clienteId")
                    }
                )
            }

            composable(
                "updateClient/{clienteId}",
                arguments = listOf(navArgument("clienteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
                val context = LocalContext.current

                val updateViewModel: ClientesUpdateViewModel = viewModel(
                    factory = ClientesUpdateViewModelFactory(context)
                )

                UpdateClientScreen(
                    clienteId = clienteId,
                    clientesViewModel = globalClientesViewModel,
                    viewModel = updateViewModel,
                    onUpdateSuccess = {
                        //clientesViewModel.refreshSelectedClient()

                        navController.navigate("clienteDetails/$clienteId") {
                            popUpTo("updateClient/$clienteId") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    // Suponiendo que UpdateClientScreen también tiene un onBack
                    onBack = { navController.popBackStack()
                    },

                    onClientDeleted = {
                        navController.navigate("clientes") {
                            popUpTo("clientes") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("venta/{clienteId}",
                arguments = listOf(navArgument("clienteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

                LaunchedEffect(clienteId) {
                    globalClientesViewModel.selectClient(clienteId)
                }
                VentaScreen(
                    clientesViewModel = globalClientesViewModel,
                    ventasViewModel = globalVentasViewModel,
                    clienteId = clienteId,
                    onBack = {
                        globalVentasViewModel.limpiarCarrito()
                        navController.popBackStack()
                     },
                    onBackToClientList = {
                        globalVentasViewModel.limpiarCarrito()
                        navController.navigate("clientes") {
                            popUpTo("clientes") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("settings") {
                SettingsApp()
            }
        }
    }
}