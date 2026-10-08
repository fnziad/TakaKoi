package com.example.shared.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    /** CASH, BANK, MOBILE_WALLET, CARD, SAVINGS */
    val type: String = "CASH",
    val openingBalance: Double = 0.0,
    val currencyCode: String = "USD",
    val isArchived: Boolean = false,
    val isDefault: Boolean = false,
    val createdAtEpochMillis: Long = 0L
)
