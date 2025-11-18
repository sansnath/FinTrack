package com.example.fintrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    suspend fun getTransactionsByUser(userId: Long): List<Transaction>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'income'")
    suspend fun getTotalIncome(userId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'expense'")
    suspend fun getTotalExpense(userId: Long): Double?
}