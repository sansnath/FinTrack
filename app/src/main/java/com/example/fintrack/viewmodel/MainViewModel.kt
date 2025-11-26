package com.example.fintrack.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.fintrack.data.Transaction
import com.example.fintrack.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // === STATE ===
    var currentUser by mutableStateOf<User?>(null)
        private set

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // === FIRESTORE ===
    private var transactionListener: ListenerRegistration? = null


    // === COMPUTED SUMMARY (SELALU REAKTIF) ===
    val totalIncome: Double
        get() = transactions.filter { it.type == "income" }.sumOf { it.amount }

    val totalExpense: Double
        get() = transactions.filter { it.type == "expense" }.sumOf { it.amount }

    val balance: Double
        get() = totalIncome - totalExpense


    // INIT -----------------------------------------------------------
    init {
        auth.currentUser?.uid?.let {
            loadCurrentUser {
                startTransactionListener()
            }
        }
    }


    // REGISTER -------------------------------------------------------
    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        isLoading = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val userData = mapOf(
                    "id" to uid,
                    "name" to name,
                    "email" to email
                )

                db.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        isLoading = false
                        errorMessage = null
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.message
                    }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.message
            }
    }


    // LOGIN ----------------------------------------------------------
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password are required"
            return
        }

        isLoading = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                stopTransactionListener()

                loadCurrentUser {
                    startTransactionListener()
                    isLoading = false
                    errorMessage = null
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.message
            }
    }


    // LOAD USER ------------------------------------------------------
    private fun loadCurrentUser(onLoaded: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                currentUser = User(
                    id = uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    password = ""
                )

                onLoaded()
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to load user: ${e.message}"
            }
    }


    // FIRESTORE LISTENER ---------------------------------------------
    private fun startTransactionListener() {
        val uid = auth.currentUser?.uid ?: return

        Log.d("DEBUG", "Starting listener for uid=$uid")

        transactionListener = db.collection("transactions")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("DEBUG", "Snapshot error: ${error.message}")
                    errorMessage = "Failed to load transactions"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Transaction(
                            firestoreId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            title = doc.getString("title") ?: "",
                            amount = doc.getDouble("amount") ?: 0.0,
                            type = doc.getString("type") ?: "",
                            category = doc.getString("category") ?: "",
                            date = doc.getString("date") ?: ""
                        )
                    }

                    Log.d("DEBUG", "Snapshot triggered: items=${list.size}")

                    transactions = list
                }
            }
    }

    private fun stopTransactionListener() {
        transactionListener?.remove()
        transactionListener = null
        Log.d("DEBUG", "Transaction listener STOPPED")
    }


    // ADD ------------------------------------------------------------
    fun addTransaction(
        title: String,
        amount: String,
        type: String,
        category: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val amountValue = amount.toDoubleOrNull() ?: 0.0

        val data = mapOf(
            "userId" to uid,
            "title" to title,
            "amount" to amountValue,
            "type" to type,
            "category" to category,
            "date" to date
        )

        db.collection("transactions")
            .add(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                errorMessage = e.message
            }
    }


    // UPDATE ---------------------------------------------------------
    fun updateTransaction(
        transaction: Transaction,
        title: String,
        amount: String,
        type: String,
        category: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        val docId = transaction.firestoreId ?: return
        val amountValue = amount.toDoubleOrNull() ?: 0.0

        val updates = mapOf(
            "title" to title,
            "amount" to amountValue,
            "type" to type,
            "category" to category,
            "date" to date
        )

        db.collection("transactions")
            .document(docId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                errorMessage = e.message
            }
    }


    // DELETE ---------------------------------------------------------
    fun deleteTransaction(transaction: Transaction) {
        val docId = transaction.firestoreId ?: return

        db.collection("transactions")
            .document(docId)
            .delete()
            .addOnFailureListener { e ->
                errorMessage = e.message
            }
    }


    // UTILITIES ------------------------------------------------------
    fun clearError() {
        errorMessage = null
    }

    fun setError(msg: String?) {
        errorMessage = msg
    }


    fun logout() {
        stopTransactionListener()

        auth.signOut()
        currentUser = null
        transactions = emptyList()
        errorMessage = null

        Log.d("DEBUG", "User logged out and state cleared")
    }
}
