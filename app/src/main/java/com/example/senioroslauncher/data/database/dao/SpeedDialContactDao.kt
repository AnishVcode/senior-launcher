package com.example.senioroslauncher.data.database.dao

import androidx.room.*
import com.example.senioroslauncher.data.database.entity.SpeedDialContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedDialContactDao {
    @Query("SELECT * FROM speed_dial_contacts ORDER BY position ASC")
    fun getAllSpeedDialContacts(): Flow<List<SpeedDialContactEntity>>

    @Query("SELECT * FROM speed_dial_contacts ORDER BY position ASC")
    suspend fun getAllSpeedDialContactsSync(): List<SpeedDialContactEntity>

    @Query("SELECT * FROM speed_dial_contacts WHERE position = :position LIMIT 1")
    suspend fun getContactAtPosition(position: Int): SpeedDialContactEntity?

    @Query("SELECT * FROM speed_dial_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): SpeedDialContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: SpeedDialContactEntity): Long

    @Update
    suspend fun update(contact: SpeedDialContactEntity)

    @Delete
    suspend fun delete(contact: SpeedDialContactEntity)

    @Query("DELETE FROM speed_dial_contacts WHERE position = :position")
    suspend fun deleteAtPosition(position: Int)

    @Query("DELETE FROM speed_dial_contacts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
