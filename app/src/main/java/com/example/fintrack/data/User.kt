package com.example.fintrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = ""
)