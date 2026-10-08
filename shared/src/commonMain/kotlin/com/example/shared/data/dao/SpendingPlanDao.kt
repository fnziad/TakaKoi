package com.example.shared.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shared.data.model.SpendingPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendingPlanDao {
    @Query("SELECT * FROM spending_plans ORDER BY isActive DESC, id DESC")
    fun getAll(): Flow<List<SpendingPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: SpendingPlanEntity): Long

    @Update
    suspend fun update(plan: SpendingPlanEntity)

    @Query("DELETE FROM spending_plans")
    suspend fun deleteAll()
}
