package com.example.fintrack.data

data class Transaction(
    val firestoreId: String? = null,
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val date: String = ""
)
