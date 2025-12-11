package com.example.senioroslauncher.data.database.dao

import androidx.room.*
import com.example.senioroslauncher.data.database.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MedicationLogDao {
    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY actionTime DESC")
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE actionTime BETWEEN :startDate AND :endDate ORDER BY actionTime DESC")
    fun getLogsBetweenDates(startDate: Date, endDate: Date): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs ORDER BY actionTime DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<MedicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MedicationLogEntity): Long

    @Delete
    suspend fun delete(log: MedicationLogEntity)

    @Query("DELETE FROM medication_logs WHERE medicationId = :medicationId")
    suspend fun deleteAllForMedication(medicationId: Long)
}
