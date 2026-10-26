package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuantiaDao {
    // Calculation History
    @Query("SELECT * FROM calculation_history ORDER BY isPinned DESC, timestamp DESC")
    fun observeHistory(): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalculationHistoryEntity): Long

    @Update
    suspend fun updateHistory(item: CalculationHistoryEntity)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM calculation_history WHERE isPinned = 0")
    suspend fun clearUnpinnedHistory()

    // Saved Variables (A-Z, M)
    @Query("SELECT * FROM saved_variables ORDER BY symbol ASC")
    fun observeVariables(): Flow<List<SavedVariableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVariable(variable: SavedVariableEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialVariables(variables: List<SavedVariableEntity>)

    @Query("UPDATE saved_variables SET realValue = 0.0, imagValue = 0.0 WHERE symbol = :symbol")
    suspend fun resetVariable(symbol: String)

    // Custom Formulas
    @Query("SELECT * FROM custom_formulas ORDER BY isFavorite DESC, category ASC, title ASC")
    fun observeFormulas(): Flow<List<CustomFormulaEntity>>

    @Query("SELECT COUNT(*) FROM custom_formulas")
    suspend fun countFormulas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: CustomFormulaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<CustomFormulaEntity>)

    @Update
    suspend fun updateFormula(formula: CustomFormulaEntity)

    @Query("DELETE FROM custom_formulas WHERE id = :id")
    suspend fun deleteFormulaById(id: Long)
}
