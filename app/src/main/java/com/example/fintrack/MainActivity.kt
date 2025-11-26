package com.example.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.fintrack.data.Transaction
import com.example.fintrack.ui.*
import com.example.fintrack.ui.theme.FinTrackTheme
import com.example.fintrack.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinTrackApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun FinTrackApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    var editTransaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, _, _ ->
            viewModel.clearError()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAnalytics = {
                    navController.navigate("analytics")
                },
                onNavigateToAddTransaction = {
                    navController.navigate("add_transaction")
                },
                onNavigateToEditTransaction = { transaction ->
                    editTransaction = transaction
                    navController.navigate("edit_transaction")
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("add_transaction") {
            AddTransactionScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }

        composable("edit_transaction") {
            editTransaction?.let {
                EditTransactionScreen(
                    viewModel = viewModel,
                    transaction = it
                ) {
                    navController.popBackStack()
                }
            }
        }

        // 🔥 NEW: ANALYTICS SCREEN
        composable("analytics") {
            AnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
