package com.example.senioroslauncher.data.database.dao

import androidx.room.*
import com.example.senioroslauncher.data.database.entity.MedicalProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalProfileDao {
    @Query("SELECT * FROM medical_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<MedicalProfileEntity?>

    @Query("SELECT * FROM medical_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): MedicalProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: MedicalProfileEntity)

    @Update
    suspend fun update(profile: MedicalProfileEntity)

    @Transaction
    suspend fun saveProfile(profile: MedicalProfileEntity) {
        insert(profile.copy(id = 1))
    }
}
