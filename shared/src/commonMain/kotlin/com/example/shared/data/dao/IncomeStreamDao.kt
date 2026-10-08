package com.example.shared.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.shared.data.model.IncomeStreamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeStreamDao {
    @Query("SELECT * FROM income_streams WHERE isActive = 1 ORDER BY id DESC")
    fun getActive(): Flow<List<IncomeStreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stream: IncomeStreamEntity): Long

    @Update
    suspend fun update(stream: IncomeStreamEntity)

    @Query("DELETE FROM income_streams")
    suspend fun deleteAll()
}
