package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val result: String,
    val exactFraction: String = "",
    val suiteCategory: String = "Scientific",
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_variables")
data class SavedVariableEntity(
    @PrimaryKey val symbol: String, // "A".."Z", "M"
    val realValue: Double,
    val imagValue: Double = 0.0,
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_formulas")
data class CustomFormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val expression: String,
    val variablesCsv: String, // e.g. "m,c" or "v,u,a,t"
    val description: String,
    val isFavorite: Boolean = false
)
