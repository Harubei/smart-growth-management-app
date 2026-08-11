package com.smartgrowth.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Bumped to version 12 to force the local database to accept the new hireDate column!
@Database(entities = [Student::class, Tutor::class, Session::class, Payment::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun tutorDao(): TutorDao
    abstract fun sessionDao(): SessionDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_growth_offline_db"
                )
                    .fallbackToDestructiveMigration() // Wipes the old Int DB, Cloud Sync immediately restores it!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}