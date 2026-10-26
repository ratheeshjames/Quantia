package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.AngleUnit
import com.example.domain.NumberFormatMode
import com.example.ui.theme.ExpressiveThemePreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quantiaDataStore by preferencesDataStore(name = "quantia_preferences")

data class UserPreferences(
    val themePreset: ExpressiveThemePreset = ExpressiveThemePreset.SAGE_LEAFIT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val numberFormat: NumberFormatMode = NumberFormatMode.DECIMAL,
    val hapticsEnabled: Boolean = true
)

class QuantiaRepository(
    private val context: Context,
    private val dao: QuantiaDao
) {
    private object Keys {
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ANGLE_UNIT = stringPreferencesKey("angle_unit")
        val NUMBER_FORMAT = stringPreferencesKey("number_format")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
    }

    val historyFlow: Flow<List<CalculationHistoryEntity>> = dao.observeHistory()
    val variablesFlow: Flow<List<SavedVariableEntity>> = dao.observeVariables()
    val formulasFlow: Flow<List<CustomFormulaEntity>> = dao.observeFormulas()

    val preferencesFlow: Flow<UserPreferences> = context.quantiaDataStore.data.map { prefs ->
        val presetName = prefs[Keys.THEME_PRESET] ?: ExpressiveThemePreset.SAGE_LEAFIT.name
        val modeName = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val angleName = prefs[Keys.ANGLE_UNIT] ?: AngleUnit.DEG.name
        val formatName = prefs[Keys.NUMBER_FORMAT] ?: NumberFormatMode.DECIMAL.name

        UserPreferences(
            themePreset = runCatching { ExpressiveThemePreset.valueOf(presetName) }
                .getOrDefault(ExpressiveThemePreset.SAGE_LEAFIT),
            themeMode = runCatching { ThemeMode.valueOf(modeName) }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            angleUnit = runCatching { AngleUnit.valueOf(angleName) }
                .getOrDefault(AngleUnit.DEG),
            numberFormat = runCatching { NumberFormatMode.valueOf(formatName) }
                .getOrDefault(NumberFormatMode.DECIMAL),
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true
        )
    }

    suspend fun ensureSeedData() {
        val initialVars = listOf(
            SavedVariableEntity("M", 0.0, 0.0, "Primary Memory Register"),
            SavedVariableEntity("A", 9.80665, 0.0, "Standard Gravity g"),
            SavedVariableEntity("B", 1.61803398875, 0.0, "Golden Ratio φ"),
            SavedVariableEntity("C", 299792458.0, 0.0, "Speed of Light c (m/s)"),
            SavedVariableEntity("D", 0.0, 0.0, "User Register D"),
            SavedVariableEntity("E", 0.0, 0.0, "User Register E"),
            SavedVariableEntity("F", 0.0, 0.0, "User Register F")
        )
        dao.insertInitialVariables(initialVars)

        if (dao.countFormulas() == 0) {
            dao.insertFormulas(
                listOf(
                    CustomFormulaEntity(
                        title = "Kinetic Energy",
                        category = "Physics",
                        expression = "0.5 * A * B^2",
                        variablesCsv = "A=mass(kg), B=velocity(m/s)",
                        description = "Translational kinetic energy E_k = ½mv²",
                        isFavorite = true
                    ),
                    CustomFormulaEntity(
                        title = "Relativistic Lorentz Factor γ",
                        category = "Physics",
                        expression = "1 / sqrt(1 - (A / 299792458)^2)",
                        variablesCsv = "A=velocity(m/s)",
                        description = "Time dilation / length contraction factor",
                        isFavorite = true
                    ),
                    CustomFormulaEntity(
                        title = "Shannon Entropy / Channel Capacity",
                        category = "Engineering",
                        expression = "A * log2(1 + B)",
                        variablesCsv = "A=Bandwidth(Hz), B=SNR(linear)",
                        description = "Shannon-Hartley maximum channel bitrate (bps)",
                        isFavorite = true
                    ),
                    CustomFormulaEntity(
                        title = "Escape Velocity",
                        category = "Astronomy",
                        expression = "sqrt((2 * 6.6743e-11 * A) / B)",
                        variablesCsv = "A=Mass(kg), B=Radius(m)",
                        description = "Gravitational escape speed from spherical body",
                        isFavorite = false
                    ),
                    CustomFormulaEntity(
                        title = "Black-Scholes D1 Growth Term",
                        category = "Finance",
                        expression = "A * (1 + B / C)^(C * D)",
                        variablesCsv = "A=Principal, B=Rate, C=Compounds/yr, D=Years",
                        description = "Compound growth future value formula",
                        isFavorite = false
                    ),
                    CustomFormulaEntity(
                        title = "Gaussian Bell Curve PDF",
                        category = "Statistics",
                        expression = "(1 / (B * sqrt(2 * pi))) * exp(-0.5 * ((A - C) / B)^2)",
                        variablesCsv = "A=x, B=sigma, C=mu",
                        description = "Normal probability density at x",
                        isFavorite = false
                    )
                )
            )
        }
    }

    suspend fun addHistory(
        expression: String,
        result: String,
        exactFraction: String,
        suiteCategory: String = "Scientific"
    ) {
        dao.insertHistory(
            CalculationHistoryEntity(
                expression = expression,
                result = result,
                exactFraction = exactFraction,
                suiteCategory = suiteCategory
            )
        )
    }

    suspend fun togglePinHistory(item: CalculationHistoryEntity) {
        dao.updateHistory(item.copy(isPinned = !item.isPinned))
    }

    suspend fun deleteHistory(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearUnpinnedHistory() {
        dao.clearUnpinnedHistory()
    }

    suspend fun saveVariable(symbol: String, realValue: Double, imagValue: Double = 0.0, description: String = "User Variable") {
        dao.upsertVariable(
            SavedVariableEntity(
                symbol = symbol,
                realValue = realValue,
                imagValue = imagValue,
                description = description,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addCustomFormula(
        title: String,
        category: String,
        expression: String,
        variablesCsv: String,
        description: String
    ) {
        dao.insertFormula(
            CustomFormulaEntity(
                title = title,
                category = category,
                expression = expression,
                variablesCsv = variablesCsv,
                description = description,
                isFavorite = false
            )
        )
    }

    suspend fun toggleFavoriteFormula(formula: CustomFormulaEntity) {
        dao.updateFormula(formula.copy(isFavorite = !formula.isFavorite))
    }

    suspend fun deleteFormula(id: Long) {
        dao.deleteFormulaById(id)
    }

    suspend fun setThemePreset(preset: ExpressiveThemePreset) {
        context.quantiaDataStore.edit { it[Keys.THEME_PRESET] = preset.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.quantiaDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.quantiaDataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAngleUnit(unit: AngleUnit) {
        context.quantiaDataStore.edit { it[Keys.ANGLE_UNIT] = unit.name }
    }

    suspend fun setNumberFormat(format: NumberFormatMode) {
        context.quantiaDataStore.edit { it[Keys.NUMBER_FORMAT] = format.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.quantiaDataStore.edit { it[Keys.HAPTICS] = enabled }
    }
}
