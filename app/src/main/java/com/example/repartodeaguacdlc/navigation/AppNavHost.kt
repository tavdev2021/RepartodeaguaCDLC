package com.example.repartodeaguacdlc.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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

    val authViewModel: AuthViewModel = viewModel()

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
                val context = LocalContext.current
                val ventasViewModel: VentasViewModel = viewModel(
                    factory = VentasViewModelFactory(context)
                )

                HomeScreen(
                    authViewModel,
                    ventasViewModel,
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
                AddNewClient(
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
                val context = LocalContext.current
                val clientesViewModel: ClientesViewModel = viewModel(
                    factory = ClientesViewModelFactory(context)
                )

                this@SharedTransitionLayout.ClientesList(
                    animatedVisibilityScope = this,
                    onAddNewClient = {
                        navController.navigate("addnewclient") {
                            popUpTo("clientes") { inclusive = true }
                        }
                    },

                    onClientClick = { clienteId ->
                        clientesViewModel.selectClient(clienteId)
                        navController.navigate("clienteDetails/$clienteId")
                    }
                )
            }

            composable(
                "clienteDetails/{clienteId}",
                arguments = listOf(navArgument("clienteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
                val context = LocalContext.current
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("clientes")
                }
                val clientesViewModel: ClientesViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ClientesViewModelFactory(context)
                )

                LaunchedEffect(clienteId) {
                    clientesViewModel.selectClient(clienteId)
                }

                this@SharedTransitionLayout.ClientDetail(
                    animatedVisibilityScope = this,
                    clientesViewModel = clientesViewModel,
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

                // 1. Obtiene el ViewModel compartido desde el "padre" ("clientes")
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("clientes")
                }
                val clientesViewModel: ClientesViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ClientesViewModelFactory(context)
                )

                val viewModel: ClientesUpdateViewModel = viewModel(
                    factory = ClientesUpdateViewModelFactory(context)
                )

                UpdateClientScreen(
                    clienteId = clienteId,
                    clientesViewModel = clientesViewModel,
                    viewModel = viewModel,
                    onUpdateSuccess = {
                        clientesViewModel.refreshSelectedClient()

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
                val context = LocalContext.current

                // 1. Obtiene el ViewModel compartido desde el "padre" ("clientes")
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("clientes")
                }
                val clientesViewModel: ClientesViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ClientesViewModelFactory(context)
                )

                remember(clienteId) {
                    clientesViewModel.selectClient(clienteId)
                    true
                }
                val ventasViewModel: VentasViewModel = viewModel(
                    factory = VentasViewModelFactory(context)
                )
                VentaScreen(
                    clientesViewModel = clientesViewModel,
                    ventasViewModel = ventasViewModel,
                    clienteId = clienteId,
                    onBack = {
                        ventasViewModel.limpiarCarrito()
                        navController.popBackStack()
                     },
                    onBackToClientList = {
                        ventasViewModel.limpiarCarrito()
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