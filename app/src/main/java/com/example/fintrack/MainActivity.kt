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
// Hapus impor NavType dan navArgument yang tidak terpakai
// import androidx.navigation.NavType
// import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.fintrack.data.Transaction
// Ganti impor wildcard (.*) dengan impor eksplisit
import com.example.fintrack.ui.LoginScreen
import com.example.fintrack.ui.RegisterScreen
import com.example.fintrack.ui.HomeScreen
import com.example.fintrack.ui.AddTransactionScreen
import com.example.fintrack.ui.EditTransactionScreen // <-- Referensi EditTransactionScreen yang diperlukan
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

    // Clear error when navigating
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
                    navController.navigate(route = "home") { // Diperbaiki: Menggunakan route
                        popUpTo(route = "login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(route = "register") // Diperbaiki: Menggunakan route
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(route = "login") { // Diperbaiki: Menggunakan route
                        popUpTo(route = "register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddTransaction = {
                    navController.navigate(route = "add_transaction") // Diperbaiki: Menggunakan route
                },
                onNavigateToEditTransaction = { transaction ->
                    editTransaction = transaction
                    navController.navigate(route = "edit_transaction") // Diperbaiki: Menggunakan route
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(route = "login") { // Diperbaiki: Menggunakan route
                        popUpTo(route = "home") { inclusive = true }
                    }
                }
            )
        }

        composable("add_transaction") {
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_transaction") {
            editTransaction?.let { transaction ->
                EditTransactionScreen(
                    viewModel = viewModel,
                    transaction = transaction,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}