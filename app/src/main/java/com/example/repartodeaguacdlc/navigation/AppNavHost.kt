package com.example.repartodeaguacdlc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.repartodeaguacdlc.view.HomeScreen
import com.example.repartodeaguacdlc.view.LoginScreen
import com.example.repartodeaguacdlc.view.RegisterScreen
import com.example.repartodeaguacdlc.view.SplashScreen
import com.example.repartodeaguacdlc.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.repartodeaguacdlc.data.ClientesViewModelFactory
import com.example.repartodeaguacdlc.view.AddNewClient
import com.example.repartodeaguacdlc.view.ClientDetail
import com.example.repartodeaguacdlc.view.ClientesList
import com.example.repartodeaguacdlc.view.PedidosList
import com.example.repartodeaguacdlc.view.SettingsApp
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel

@Composable
fun AppNavHost()
{
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()

    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    val startDestination = if (isAuthenticated) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("splash") {
            SplashScreen(
                authViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel,
                onLoginSuccess = { navController.navigate("home") {
                    popUpTo ("login") {inclusive = true} 
                    launchSingleTop = true } },
                onNavigateToRegister = { navController.navigate("register") {
                    popUpTo ("login") {inclusive = true} }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel,
                onRegisterSuccess = { navController.navigate("home") {
                    popUpTo ("register") {inclusive = true}
                    launchSingleTop = true } },
                onNavigateToLogin = { navController.navigate("login") {
                    popUpTo ("register") {inclusive = true} } }
            )
        }

        composable("home") {
            HomeScreen(
                authViewModel,
                onLogout = { navController.navigate("login") {
                        popUpTo("home") { inclusive = true } } },
                onAddNewClient = {
                    navController.navigate("addnewclient") {
                        popUpTo("home") { inclusive = true } } },
                onPedidos = {
                    navController.navigate("pedidos") },
                onClientes = {
                    navController.navigate("clientes") },
                onSettings = {
                    navController.navigate("settings")}
            )
        }

        composable("addnewclient") {
            AddNewClient(
                onNavigateToHomeFromAddNewClient = {
                    navController.navigate("home") {
                    popUpTo ("addnewclient") {inclusive = true} } }
            )
        }

        composable("pedidos") {
            PedidosList()
        }

        composable("clientes") {
            ClientesList(
                onAddNewClient = {
                    navController.navigate("addnewclient") {
                        popUpTo("clientes") { inclusive = true }
                    }
                },

                onClientClick = { clienteId ->
                    navController.navigate("clienteDetails/$clienteId")
                }
            )
        }

        composable("clienteDetails/{clienteId}",
            arguments = listOf(navArgument("clienteId") { type = NavType.IntType })
        ){ backStackEntry ->
            val id = backStackEntry.arguments?.getInt("clienteId") ?: 0
            val context = androidx.compose.ui.platform.LocalContext.current

            // 1. Instanciar el ViewModel usando el Factory
            val clientesViewModel: ClientesViewModel = viewModel(
                factory = ClientesViewModelFactory(context)
            )

            ClientDetail(
                clienteId = id,
                clientesViewModel = clientesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsApp()
        }
    }
}