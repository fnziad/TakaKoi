package com.example.shared.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_streams")
data class IncomeStreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    /** ONE_TIME, DAILY, WEEKLY, MONTHLY, CUSTOM */
    val frequency: String = "ONE_TIME",
    val nextDueEpochMillis: Long = 0L,
    val accountId: Int? = null,
    val isActive: Boolean = true,
    val lastConfirmedEpochMillis: Long = 0L
)
