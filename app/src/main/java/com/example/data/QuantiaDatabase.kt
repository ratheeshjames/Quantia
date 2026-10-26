package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CalculationHistoryEntity::class,
        SavedVariableEntity::class,
        CustomFormulaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuantiaDatabase : RoomDatabase() {
    abstract fun quantiaDao(): QuantiaDao

    companion object {
        @Volatile
        private var INSTANCE: QuantiaDatabase? = null

        fun getInstance(context: Context): QuantiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuantiaDatabase::class.java,
                    "quantia_calculator.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
