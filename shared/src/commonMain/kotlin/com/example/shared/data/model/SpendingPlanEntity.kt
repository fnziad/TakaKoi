package com.example.shared.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spending_plans")
data class SpendingPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val limitAmount: Double,
    /** WEEKLY, MONTHLY, PAYDAY, CUSTOM */
    val cycleType: String = "MONTHLY",
    val anchorDayOfMonth: Int = 1,
    val customStartEpochMillis: Long = 0L,
    val customEndEpochMillis: Long = 0L,
    val isActive: Boolean = true
)
