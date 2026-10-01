package com.example.admin_app.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.admin_app.view.HomeScreen
import com.example.admin_app.view.LoginScreen
import com.example.admin_app.view.ManageRoutesScreen
import com.example.admin_app.view.RegisterScreen
import com.example.admin_app.view.SplashScreen
import com.example.common.viewmodel.AuthViewModel

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
                    authViewModel,
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
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
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onManageRoutes = {
                        navController.navigate("manage_routes")
                    }
                )
            }
            composable("manage_routes") {
                ManageRoutesScreen(authViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}