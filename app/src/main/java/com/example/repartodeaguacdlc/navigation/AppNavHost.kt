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
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel

@Composable
fun AppNavHost(viewModel: AuthViewModel = AuthViewModel(), clientesViewModel: ClientesViewModel = ClientesViewModel()) {

    val navController = rememberNavController()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    val startDestination = if (isAuthenticated) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("splash") {
            SplashScreen(
                viewModel,
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
                viewModel,
                onLoginSuccess = { navController.navigate("home") {
                    popUpTo ("login") {inclusive = true} } },
                onNavigateToRegister = { navController.navigate("register") {
                    popUpTo ("login") {inclusive = true} }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel,
                onRegisterSuccess = { navController.navigate("home") {
                    popUpTo ("register") {inclusive = true} } },
                onNavigateToLogin = { navController.navigate("login") {
                    popUpTo ("register") {inclusive = true} }
                }
            )
        }

        composable("home") {
            HomeScreen(viewModel, clientesViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true } }
                }
            )
        }
    }
}