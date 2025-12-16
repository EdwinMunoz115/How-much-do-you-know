package com.example.howyouknow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.howyouknow.ui.screens.auth.LoginScreen
import com.example.howyouknow.ui.screens.auth.RegisterScreen
import com.example.howyouknow.ui.screens.game.GameScreen
import com.example.howyouknow.ui.screens.home.HomeScreen
import com.example.howyouknow.ui.screens.pairing.PairingScreen
import com.example.howyouknow.ui.screens.profile.ProfileScreen
import com.example.howyouknow.ui.screens.questions.CreateQuestionScreen
import com.example.howyouknow.ui.screens.results.ResultsScreen
import com.example.howyouknow.ui.viewmodel.AuthViewModel
import com.example.howyouknow.ui.viewmodel.GameViewModel
import com.example.howyouknow.ui.viewmodel.PairingViewModel
import com.example.howyouknow.ui.viewmodel.QuestionViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Pairing : Screen("pairing")
    object CreateQuestion : Screen("create_question")
    object Game : Screen("game")
    object Results : Screen("results/{sessionId}") {
        fun createRoute(sessionId: String) = "results/$sessionId"
    }
    
    companion object {
        const val RESULTS_SESSION_ID = "sessionId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val authViewModel: com.example.howyouknow.ui.viewmodel.AuthViewModel = viewModel()
    val pairingViewModel: com.example.howyouknow.ui.viewmodel.PairingViewModel = viewModel()
    // TODO: Actualizar QuestionViewModel y GameViewModel para usar Room
    val questionViewModel: com.example.howyouknow.ui.viewmodel.QuestionViewModel = viewModel()
    val gameViewModel: com.example.howyouknow.ui.viewmodel.GameViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Home.route) {
            val userId = authViewModel.getCurrentUserId() ?: ""
            var hasPartner by remember { mutableStateOf(false) }
            
            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    val user = authViewModel.uiState.value.user
                    hasPartner = user?.partnerId != null
                }
            }

            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToGame = {
                    navController.navigate(Screen.Game.route)
                },
                onNavigateToPairing = {
                    navController.navigate(Screen.Pairing.route)
                },
                onNavigateToCreateQuestion = {
                    navController.navigate(Screen.CreateQuestion.route)
                },
                hasPartner = hasPartner
            )
        }

        composable(Screen.Profile.route) {
            val userId = authViewModel.getCurrentUserId() ?: ""
            ProfileScreen(
                userId = userId,
                onNavigateToPairing = {
                    navController.navigate(Screen.Pairing.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Pairing.route) {
            PairingScreen(
                viewModel = pairingViewModel,
                onPairingSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Pairing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateQuestion.route) {
            CreateQuestionScreen(
                viewModel = questionViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                viewModel = gameViewModel,
                onGameComplete = { sessionId ->
                    navController.navigate(Screen.Results.createRoute(sessionId)) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(navArgument(Screen.RESULTS_SESSION_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString(Screen.RESULTS_SESSION_ID) ?: ""
            ResultsScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}


