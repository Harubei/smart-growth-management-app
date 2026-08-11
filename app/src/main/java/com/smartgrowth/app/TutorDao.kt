package com.smartgrowth.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorDao {
    @Query("SELECT * FROM tutors ORDER BY fullName ASC")
    fun getAllTutors(): Flow<List<Tutor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutor(tutor: Tutor)

    @Update
    suspend fun updateTutor(tutor: Tutor)

    @Delete
    suspend fun deleteTutor(tutor: Tutor)

    @Query("SELECT * FROM tutors WHERE isSynced = 0")
    suspend fun getUnsyncedTutors(): List<Tutor>

    @Query("DELETE FROM tutors WHERE isSynced = 1")
    suspend fun clearSyncedTutors()
}