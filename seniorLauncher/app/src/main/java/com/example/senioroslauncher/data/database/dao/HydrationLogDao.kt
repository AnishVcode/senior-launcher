package com.example.senioroslauncher.data.database.dao

import androidx.room.*
import com.example.senioroslauncher.data.database.entity.HydrationLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface HydrationLogDao {
    @Query("SELECT * FROM hydration_logs WHERE date BETWEEN :startOfDay AND :endOfDay LIMIT 1")
    fun getTodayLog(startOfDay: Date, endOfDay: Date): Flow<HydrationLogEntity?>

    @Query("SELECT * FROM hydration_logs WHERE date BETWEEN :startOfDay AND :endOfDay LIMIT 1")
    suspend fun getTodayLogSync(startOfDay: Date, endOfDay: Date): HydrationLogEntity?

    @Query("SELECT * FROM hydration_logs ORDER BY date DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 7): Flow<List<HydrationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HydrationLogEntity): Long

    @Update
    suspend fun update(log: HydrationLogEntity)

    @Transaction
    suspend fun incrementGlasses(startOfDay: Date, endOfDay: Date) {
        val existingLog = getTodayLogSync(startOfDay, endOfDay)
        if (existingLog != null) {
            update(existingLog.copy(glassesCount = existingLog.glassesCount + 1))
        } else {
            insert(HydrationLogEntity(date = startOfDay, glassesCount = 1))
        }
    }

    @Transaction
    suspend fun decrementGlasses(startOfDay: Date, endOfDay: Date) {
        val existingLog = getTodayLogSync(startOfDay, endOfDay)
        if (existingLog != null && existingLog.glassesCount > 0) {
            update(existingLog.copy(glassesCount = existingLog.glassesCount - 1))
        }
    }
}
