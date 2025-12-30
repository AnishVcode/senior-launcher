package com.example.senioroslauncher.data.database.dao

import androidx.room.*
import com.example.senioroslauncher.data.database.entity.MedicationScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationScheduleDao {
    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId")
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationScheduleEntity>>

    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<MedicationScheduleEntity>

    @Query("SELECT * FROM medication_schedules WHERE isEnabled = 1")
    suspend fun getAllEnabledSchedules(): List<MedicationScheduleEntity>

    @Query("SELECT * FROM medication_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): MedicationScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: MedicationScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<MedicationScheduleEntity>)

    @Update
    suspend fun update(schedule: MedicationScheduleEntity)

    @Delete
    suspend fun delete(schedule: MedicationScheduleEntity)

    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteAllForMedication(medicationId: Long)
}
