package com.example.admin_app.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.admin_app.view.HomeScreen
import com.example.admin_app.view.LoginScreen
import com.example.admin_app.view.ManageRoutesScreen
import com.example.admin_app.view.RegisterScreen
import com.example.admin_app.view.SplashScreen
import com.example.common.viewmodel.AuthViewModel

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
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    authViewModel,
                    onRegisterSuccess = {
                        navController.popBackStack()
                    },
                    onBackNavigation = {
                        navController.popBackStack()
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    authViewModel,
                    onManageRoutes = {
                        navController.navigate("manage_routes")
                    },
                    onRegisterRepartidor = {
                        navController.navigate("register")
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },

                )
            }
            composable("manage_routes") {
                ManageRoutesScreen(authViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}