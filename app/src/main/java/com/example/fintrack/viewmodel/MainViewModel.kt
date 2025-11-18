package com.example.fintrack.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintrack.data.AppDatabase
import com.example.fintrack.data.Transaction
import com.example.fintrack.data.User
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val transactionDao = database.transactionDao()

    var currentUser by mutableStateOf<User?>(null)
        private set

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set

    var totalIncome by mutableStateOf(0.0)
        private set

    var totalExpense by mutableStateOf(0.0)
        private set

    var balance by mutableStateOf(0.0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    errorMessage = "User with this email already exists"
                    return@launch
                }

                val user = User(name = name, email = email, password = password)
                userDao.insertUser(user)
                errorMessage = null
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Registration failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password are required"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                val user = userDao.loginUser(email, password)
                if (user != null) {
                    currentUser = user
                    loadTransactions()
                    errorMessage = null
                    onSuccess()
                } else {
                    errorMessage = "Invalid email or password"
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadTransactions() {
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    transactions = transactionDao.getTransactionsByUser(user.id)
                    totalIncome = transactionDao.getTotalIncome(user.id) ?: 0.0
                    totalExpense = transactionDao.getTotalExpense(user.id) ?: 0.0
                    balance = totalIncome - totalExpense
                } catch (e: Exception) {
                    errorMessage = "Failed to load transactions: ${e.message}"
                }
            }
        }
    }

    fun addTransaction(
        title: String,
        amount: String,
        type: String,
        category: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || amount.isBlank() || category.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            errorMessage = "Please enter a valid amount"
            return
        }

        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val transaction = Transaction(
                        userId = user.id,
                        title = title,
                        amount = amountValue,
                        type = type,
                        category = category,
                        date = date
                    )
                    transactionDao.insertTransaction(transaction)
                    loadTransactions()
                    errorMessage = null
                    onSuccess()
                } catch (e: Exception) {
                    errorMessage = "Failed to add transaction: ${e.message}"
                }
            }
        }
    }

    fun updateTransaction(
        transaction: Transaction,
        title: String,
        amount: String,
        type: String,
        category: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || amount.isBlank() || category.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            errorMessage = "Please enter a valid amount"
            return
        }

        viewModelScope.launch {
            try {
                val updatedTransaction = transaction.copy(
                    title = title,
                    amount = amountValue,
                    type = type,
                    category = category,
                    date = date
                )
                transactionDao.updateTransaction(updatedTransaction)
                loadTransactions()
                errorMessage = null
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to update transaction: ${e.message}"
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionDao.deleteTransaction(transaction)
                loadTransactions()
            } catch (e: Exception) {
                errorMessage = "Failed to delete transaction: ${e.message}"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun logout() {
        currentUser = null
        transactions = emptyList()
        totalIncome = 0.0
        totalExpense = 0.0
        balance = 0.0
        errorMessage = null
    }
}