package com.example.shared.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.datetime.Clock

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** EXPENSE, INCOME, or TRANSFER */
    val type: String,
    val amount: Double,
    val category: String,
    val subCategory: String,
    val description: String,
    val dateEpochMillis: Long = Clock.System.now().toEpochMilliseconds(),
    val dayName: String = "",
    val isRecurring: Boolean = false,
    val recurringFrequency: String = "One-time", // "Daily", "Weekly", "Monthly", "One-time"
    /** Null while a quick capture is waiting for the user to choose its source. */
    val accountId: Int? = null,
    /** Used only by TRANSFER entries. */
    val destinationAccountId: Int? = null,
    /** PENDING_SOURCE or COMPLETED. */
    val status: String = "PENDING_SOURCE",
    val source: String = "MANUAL"
)
