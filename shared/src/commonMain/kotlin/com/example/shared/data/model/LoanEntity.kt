package com.example.shared.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.datetime.Clock

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val loanType: String, // "SHORT_TERM" or "LONG_TERM"
    val direction: String, // "I_OWE" or "OWED_TO_ME"
    val dueDateEpochMillis: Long = Clock.System.now().toEpochMilliseconds() + (14L * 24 * 3600 * 1000),
    val isSettled: Boolean = false,
    val note: String = "",
    /** FRIEND_FAMILY, CASH_ADVANCE, SIMPLE_APR, COMPOUND_APR, MFS_FEE */
    val template: String = "FRIEND_FAMILY",
    /** NONE, SIMPLE, COMPOUND, FLAT_FEE */
    val interestModel: String = "NONE",
    val annualInterestRate: Double = 0.0,
    /** DAILY, MONTHLY, YEARLY */
    val compoundingFrequency: String = "MONTHLY",
    val fees: Double = 0.0
)
