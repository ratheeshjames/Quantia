package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistoryEntity
import com.example.data.CustomFormulaEntity
import com.example.data.QuantiaDatabase
import com.example.data.QuantiaRepository
import com.example.data.SavedVariableEntity
import com.example.data.UserPreferences
import com.example.domain.AngleUnit
import com.example.domain.CatalogAndConverterEngine
import com.example.domain.ComplexVal
import com.example.domain.EvaluationOutput
import com.example.domain.MathEngine
import com.example.domain.NumberFormatMode
import com.example.domain.WordSize
import com.example.ui.theme.ExpressiveThemePreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SuiteTab(val title: String, val badge: String) {
    SCIENTIFIC("Scientific", "36+"),
    GRAPHING("Graphing", "2D"),
    MATRIX_ALGEBRA("Algebra", "Mat/Eq"),
    CONVERTER_BASEN("Convert", "80+"),
    STATS_FINANCE("Stats & Fin", "Pro")
}

enum class ActiveOverlaySheet {
    NONE,
    HISTORY_AND_MEMORY,
    PHYSICS_CONSTANTS,
    CUSTOM_FORMULAS,
    THEME_STUDIO,
    CALCULUS_LAB
}

data class GraphFunctionItem(
    val id: Int,
    val label: String,
    val expression: String,
    val enabled: Boolean = true
)

data class CalculatorUiState(
    val activeSuite: SuiteTab = SuiteTab.SCIENTIFIC,
    val activeSheet: ActiveOverlaySheet = ActiveOverlaySheet.NONE,
    val expression: String = "68+12.8-24.89",
    val lastAnswer: ComplexVal = ComplexVal(55.91),
    val secondShift: Boolean = false,
    val hypShift: Boolean = false,
    val isFunctionDrawerExpanded: Boolean = false,
    val selectedDrawerCategory: String = "Trig & Hyp",
    // Calculus Lab state
    val calculusExpression: String = "x^3 - 3*x",
    val calculusAtX: String = "2.0",
    val integralLowerA: String = "0.0",
    val integralUpperB: String = "3.0",
    val seriesStartK: String = "1",
    val seriesEndN: String = "10",
    // 2D Graphing state
    val graphFunctions: List<GraphFunctionItem> = listOf(
        GraphFunctionItem(1, "f₁(x)", "sin(x) * cos(0.5*x)", true),
        GraphFunctionItem(2, "f₂(x)", "0.25 * x^2 - 2", true),
        GraphFunctionItem(3, "f₃(x)", "ln(x + 4)", false),
        GraphFunctionItem(4, "f₄(x)", "exp(-0.3*x) * sin(2*x)", false)
    ),
    val graphXMin: Float = -8f,
    val graphXMax: Float = 8f,
    val graphYMin: Float = -5f,
    val graphYMax: Float = 5f,
    val graphTraceX: Float = 1.5f,
    val showTangentLine: Boolean = true,
    val showIntegralShade: Boolean = true,
    val shadeLowerX: Float = 0f,
    val shadeUpperX: Float = 3.14f,
    val showGraphTable: Boolean = false,
    val tableStart: Double = -4.0,
    val tableStep: Double = 1.0,
    // Base-N Programmer state
    val programmerValue: Long = 0x2FA8L,
    val programmerOperandB: Long = 0x00FFL,
    val programmerWordSize: WordSize = WordSize.BIT_16,
    val programmerOp: String = "XOR",
    // Toast / Banner feedback
    val statusBanner: String? = null
)

class CalculatorViewModel(
    private val repository: QuantiaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val history: StateFlow<List<CalculationHistoryEntity>> = repository.historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val variables: StateFlow<List<SavedVariableEntity>> = repository.variablesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customFormulas: StateFlow<List<CustomFormulaEntity>> = repository.formulasFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val liveEvaluation: StateFlow<EvaluationOutput> = combine(
        _uiState,
        preferences,
        variables
    ) { state, prefs, vars ->
        val varMap = vars.associate { it.symbol to ComplexVal(it.realValue, it.imagValue) }.toMutableMap()
        varMap["Ans"] = state.lastAnswer
        MathEngine.evaluate(
            rawExpression = state.expression,
            angleUnit = prefs.angleUnit,
            formatMode = prefs.numberFormat,
            variables = varMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MathEngine.evaluate("68+12.8-24.89")
    )

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    fun selectSuite(suite: SuiteTab) {
        _uiState.update { it.copy(activeSuite = suite) }
    }

    fun openSheet(sheet: ActiveOverlaySheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = ActiveOverlaySheet.NONE) }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    fun appendToken(token: String) {
        _uiState.update { current ->
            val nextExpr = if (current.expression == "0" && token.all { it.isDigit() }) {
                token
            } else {
                current.expression + token
            }
            current.copy(expression = nextExpr)
        }
    }

    fun setExpression(newExpression: String) {
        _uiState.update { it.copy(expression = newExpression) }
    }

    fun backspace() {
        _uiState.update { current ->
            val expr = current.expression
            if (expr.isEmpty()) return@update current
            val multiTokens = listOf(
                "asinh(", "acosh(", "atanh(", "asin(", "acos(", "atan(",
                "sinh(", "cosh(", "tanh(", "sin(", "cos(", "tan(",
                "cot(", "sec(", "csc(", "sqrt(", "cbrt(", "log2(", "log(", "ln(",
                "abs(", "arg(", "conj(", "fact(", "gamma(", "gcd(", "lcm(", "Ans"
            )
            val matched = multiTokens.firstOrNull { expr.endsWith(it) }
            val trimmed = if (matched != null) {
                expr.dropLast(matched.length)
            } else {
                expr.dropLast(1)
            }
            current.copy(expression = trimmed)
        }
    }

    fun clearAll() {
        _uiState.update { it.copy(expression = "") }
    }

    fun insertSmartParenthesis() {
        _uiState.update { current ->
            val expr = current.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }
            val lastChar = expr.lastOrNull()
            val shouldClose = openCount > closeCount &&
                lastChar != null &&
                (lastChar.isDigit() || lastChar == ')' || lastChar == 'i' || lastChar == 'π' || lastChar.isLetter())
            val token = if (shouldClose) ")" else "("
            current.copy(expression = expr + token)
        }
    }

    fun toggleSecondShift() {
        _uiState.update { it.copy(secondShift = !it.secondShift) }
    }

    fun toggleHypShift() {
        _uiState.update { it.copy(hypShift = !it.hypShift) }
    }

    fun toggleFunctionDrawer() {
        _uiState.update { it.copy(isFunctionDrawerExpanded = !it.isFunctionDrawerExpanded) }
    }

    fun selectDrawerCategory(category: String) {
        _uiState.update { it.copy(selectedDrawerCategory = category) }
    }

    fun evaluateAndCommit(suiteCategory: String = "Scientific") {
        val currentExpr = _uiState.value.expression.trim()
        if (currentExpr.isEmpty()) return
        val output = liveEvaluation.value
        if (output.error != null) {
            _uiState.update { it.copy(statusBanner = output.error) }
            return
        }
        val balanced = MathEngine.autoBalanceParentheses(currentExpr)
        val formattedResult = output.formattedPrimary
        _uiState.update {
            it.copy(
                expression = formattedResult.replace(",", "").replace(" ", ""),
                lastAnswer = output.value,
                statusBanner = "Saved to History: $balanced = $formattedResult"
            )
        }
        viewModelScope.launch {
            repository.addHistory(
                expression = balanced,
                result = formattedResult,
                exactFraction = output.formattedFraction,
                suiteCategory = suiteCategory
            )
        }
    }

    fun recordExternalCalculation(expression: String, result: String, suiteCategory: String) {
        viewModelScope.launch {
            repository.addHistory(
                expression = expression,
                result = result,
                exactFraction = "",
                suiteCategory = suiteCategory
            )
        }
        _uiState.update { it.copy(statusBanner = "Logged to History Tape ($suiteCategory)") }
    }

    fun sendValueToCalculator(numericOrExpr: String, label: String) {
        _uiState.update {
            it.copy(
                activeSuite = SuiteTab.SCIENTIFIC,
                activeSheet = ActiveOverlaySheet.NONE,
                expression = if (it.expression.isEmpty() || it.expression == "0") numericOrExpr else "${it.expression}$numericOrExpr",
                statusBanner = "Inserted $label into expression"
            )
        }
    }

    // Memory & Variable operations
    fun memoryAdd() {
        val currentM = variables.value.firstOrNull { it.symbol == "M" }?.realValue ?: 0.0
        val addVal = liveEvaluation.value.value.re
        if (addVal.isFinite()) {
            val next = currentM + addVal
            viewModelScope.launch {
                repository.saveVariable("M", next, 0.0, "Primary Memory Register")
            }
            _uiState.update { it.copy(statusBanner = "M+ → ${MathEngine.formatReal(next)}") }
        }
    }

    fun memorySubtract() {
        val currentM = variables.value.firstOrNull { it.symbol == "M" }?.realValue ?: 0.0
        val subVal = liveEvaluation.value.value.re
        if (subVal.isFinite()) {
            val next = currentM - subVal
            viewModelScope.launch {
                repository.saveVariable("M", next, 0.0, "Primary Memory Register")
            }
            _uiState.update { it.copy(statusBanner = "M− → ${MathEngine.formatReal(next)}") }
        }
    }

    fun memoryRecall() {
        appendToken("M")
    }

    fun memoryClear() {
        viewModelScope.launch {
            repository.saveVariable("M", 0.0, 0.0, "Primary Memory Register")
        }
        _uiState.update { it.copy(statusBanner = "Memory M Cleared") }
    }

    fun storeCurrentInVariable(symbol: String, description: String = "User Variable") {
        val v = liveEvaluation.value.value
        if (v.re.isFinite()) {
            viewModelScope.launch {
                repository.saveVariable(symbol, v.re, v.im, description)
            }
            _uiState.update {
                it.copy(statusBanner = "Stored ${MathEngine.formatComplexDecimal(v)} → $symbol")
            }
        }
    }

    fun updateVariableDirect(symbol: String, realValue: Double, description: String) {
        viewModelScope.launch {
            repository.saveVariable(symbol, realValue, 0.0, description)
        }
    }

    // History & Custom Formulas
    fun togglePinHistory(item: CalculationHistoryEntity) {
        viewModelScope.launch { repository.togglePinHistory(item) }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch { repository.deleteHistory(id) }
    }

    fun clearUnpinnedHistory() {
        viewModelScope.launch { repository.clearUnpinnedHistory() }
    }

    fun addCustomFormula(title: String, category: String, expression: String, variablesCsv: String, description: String) {
        viewModelScope.launch {
            repository.addCustomFormula(title, category, expression, variablesCsv, description)
        }
        _uiState.update { it.copy(statusBanner = "Saved formula: $title") }
    }

    fun toggleFavoriteFormula(formula: CustomFormulaEntity) {
        viewModelScope.launch { repository.toggleFavoriteFormula(formula) }
    }

    fun deleteFormula(id: Long) {
        viewModelScope.launch { repository.deleteFormula(id) }
    }

    // Preferences & Theme Studio
    fun cycleAngleUnit() {
        val next = when (preferences.value.angleUnit) {
            AngleUnit.DEG -> AngleUnit.RAD
            AngleUnit.RAD -> AngleUnit.GRAD
            AngleUnit.GRAD -> AngleUnit.DEG
        }
        viewModelScope.launch { repository.setAngleUnit(next) }
    }

    fun cycleNumberFormat() {
        val modes = NumberFormatMode.entries
        val nextIdx = (modes.indexOf(preferences.value.numberFormat) + 1) % modes.size
        viewModelScope.launch { repository.setNumberFormat(modes[nextIdx]) }
    }

    fun setNumberFormat(mode: NumberFormatMode) {
        viewModelScope.launch { repository.setNumberFormat(mode) }
    }

    fun setThemePreset(preset: ExpressiveThemePreset) {
        viewModelScope.launch { repository.setThemePreset(preset) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    // Calculus Lab Updates
    fun updateCalculusFields(
        expr: String = _uiState.value.calculusExpression,
        atX: String = _uiState.value.calculusAtX,
        lowerA: String = _uiState.value.integralLowerA,
        upperB: String = _uiState.value.integralUpperB,
        startK: String = _uiState.value.seriesStartK,
        endN: String = _uiState.value.seriesEndN
    ) {
        _uiState.update {
            it.copy(
                calculusExpression = expr,
                calculusAtX = atX,
                integralLowerA = lowerA,
                integralUpperB = upperB,
                seriesStartK = startK,
                seriesEndN = endN
            )
        }
    }

    // 2D Graphing Studio Updates
    fun updateGraphFunction(id: Int, expression: String, enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                graphFunctions = state.graphFunctions.map {
                    if (it.id == id) it.copy(expression = expression, enabled = enabled) else it
                }
            )
        }
    }

    fun zoomGraph(factor: Float) {
        _uiState.update { state ->
            val cx = (state.graphXMin + state.graphXMax) / 2f
            val cy = (state.graphYMin + state.graphYMax) / 2f
            val halfW = ((state.graphXMax - state.graphXMin) / 2f * factor).coerceIn(0.5f, 100f)
            val halfH = ((state.graphYMax - state.graphYMin) / 2f * factor).coerceIn(0.5f, 100f)
            state.copy(
                graphXMin = cx - halfW,
                graphXMax = cx + halfW,
                graphYMin = cy - halfH,
                graphYMax = cy + halfH
            )
        }
    }

    fun panGraph(dxUnits: Float, dyUnits: Float) {
        _uiState.update { state ->
            state.copy(
                graphXMin = state.graphXMin + dxUnits,
                graphXMax = state.graphXMax + dxUnits,
                graphYMin = state.graphYMin + dyUnits,
                graphYMax = state.graphYMax + dyUnits
            )
        }
    }

    fun resetGraphViewport() {
        _uiState.update {
            it.copy(
                graphXMin = -8f,
                graphXMax = 8f,
                graphYMin = -5f,
                graphYMax = 5f,
                graphTraceX = 1.5f
            )
        }
    }

    fun setGraphTraceX(x: Float) {
        _uiState.update { it.copy(graphTraceX = x.coerceIn(it.graphXMin, it.graphXMax)) }
    }

    fun toggleTangentLine() {
        _uiState.update { it.copy(showTangentLine = !it.showTangentLine) }
    }

    fun toggleIntegralShade() {
        _uiState.update { it.copy(showIntegralShade = !it.showIntegralShade) }
    }

    fun toggleGraphTable() {
        _uiState.update { it.copy(showGraphTable = !it.showGraphTable) }
    }

    // Base-N Programmer Mode Updates
    fun setProgrammerValue(value: Long) {
        _uiState.update {
            it.copy(programmerValue = CatalogAndConverterEngine.maskForWordSize(value, it.programmerWordSize))
        }
    }

    fun setProgrammerOperandB(value: Long) {
        _uiState.update {
            it.copy(programmerOperandB = CatalogAndConverterEngine.maskForWordSize(value, it.programmerWordSize))
        }
    }

    fun toggleProgrammerBit(bitIndex: Int) {
        _uiState.update { state ->
            val toggled = state.programmerValue xor (1L shl bitIndex)
            state.copy(programmerValue = CatalogAndConverterEngine.maskForWordSize(toggled, state.programmerWordSize))
        }
    }

    fun setProgrammerWordSize(size: WordSize) {
        _uiState.update { state ->
            state.copy(
                programmerWordSize = size,
                programmerValue = CatalogAndConverterEngine.maskForWordSize(state.programmerValue, size),
                programmerOperandB = CatalogAndConverterEngine.maskForWordSize(state.programmerOperandB, size)
            )
        }
    }

    fun setProgrammerOp(op: String) {
        _uiState.update { it.copy(programmerOp = op) }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = QuantiaDatabase.getInstance(context)
                    val repo = QuantiaRepository(context.applicationContext, db.quantiaDao())
                    return CalculatorViewModel(repo) as T
                }
            }
        }
    }
}
