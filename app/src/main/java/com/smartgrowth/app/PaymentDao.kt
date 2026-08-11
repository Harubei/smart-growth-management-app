package com.smartgrowth.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE isSynced = 0")
    suspend fun getUnsyncedPayments(): List<Payment>

    @Query("DELETE FROM payments WHERE isSynced = 1")
    suspend fun clearSyncedPayments()
}