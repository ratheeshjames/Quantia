package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CalculationHistoryEntity
import com.example.data.CustomFormulaEntity
import com.example.data.SavedVariableEntity
import com.example.data.UserPreferences
import com.example.domain.AngleUnit
import com.example.domain.CatalogAndConverterEngine
import com.example.domain.MathEngine
import com.example.ui.ActiveOverlaySheet
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.components.ExpressiveStatusChip
import com.example.ui.components.ExpressiveWavyDivider
import com.example.ui.theme.ExpressiveThemePreset
import com.example.ui.theme.LocalExpressivePalette
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantiaOverlaySheets(
    uiState: CalculatorUiState,
    preferences: UserPreferences,
    history: List<CalculationHistoryEntity>,
    variables: List<SavedVariableEntity>,
    formulas: List<CustomFormulaEntity>,
    viewModel: CalculatorViewModel
) {
    if (uiState.activeSheet == ActiveOverlaySheet.NONE) return

    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        when (uiState.activeSheet) {
            ActiveOverlaySheet.HISTORY_AND_MEMORY -> HistoryAndVariablesSheetContent(
                history = history,
                variables = variables,
                viewModel = viewModel
            )
            ActiveOverlaySheet.CALCULUS_LAB -> CalculusLabSheetContent(
                uiState = uiState,
                angleUnit = preferences.angleUnit,
                viewModel = viewModel
            )
            ActiveOverlaySheet.PHYSICS_CONSTANTS -> ConstantsCatalogSheetContent(
                viewModel = viewModel
            )
            ActiveOverlaySheet.CUSTOM_FORMULAS -> CustomFormulasSheetContent(
                formulas = formulas,
                viewModel = viewModel
            )
            ActiveOverlaySheet.THEME_STUDIO -> ExpressiveThemeStudioSheetContent(
                preferences = preferences,
                viewModel = viewModel
            )
            ActiveOverlaySheet.NONE -> Unit
        }
    }
}

@Composable
private fun HistoryAndVariablesSheetContent(
    history: List<CalculationHistoryEntity>,
    variables: List<SavedVariableEntity>,
    viewModel: CalculatorViewModel
) {
    val palette = LocalExpressivePalette.current
    var tab by remember { mutableStateOf("History Tape") }
    var search by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 580.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History Tape & Memory Registers",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = viewModel::closeSheet) {
                Icon(Icons.Default.Close, contentDescription = "Close Sheet")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpressiveStatusChip("History Tape (${history.size})", tab == "History Tape", { tab = "History Tape" })
            ExpressiveStatusChip("Variables A–F & M", tab == "Variables", { tab = "Variables" })
            if (tab == "History Tape" && history.isNotEmpty()) {
                TextButton(onClick = viewModel::clearUnpinnedHistory) {
                    Text("Clear Unpinned")
                }
            }
        }

        if (tab == "History Tape") {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search calculations or suite…") }
            )

            val filtered = history.filter {
                search.isBlank() ||
                    it.expression.contains(search, ignoreCase = true) ||
                    it.result.contains(search, ignoreCase = true) ||
                    it.suiteCategory.contains(search, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = palette.cardElevated,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.empty_history_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.empty_history_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        Surface(
                            onClick = { viewModel.sendValueToCalculator(item.expression, "History Expression") },
                            shape = RoundedCornerShape(20.dp),
                            color = if (item.isPinned) palette.cardAccentSoft else palette.cardElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.suiteCategory}  •  ${item.expression}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "= ${item.result}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (item.exactFraction.isNotEmpty() && item.exactFraction != item.result) {
                                        Text(
                                            text = "Rational: ${item.exactFraction}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.togglePinHistory(item) }) {
                                    Icon(
                                        imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                        contentDescription = "Pin Calculation"
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteHistory(item.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Calculation"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(variables, key = { it.symbol }) { v ->
                    var textVal by remember(v.realValue) { mutableStateOf(v.realValue.toString()) }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = palette.cardElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Register ${v.symbol}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(v.description, style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedTextField(
                                value = textVal,
                                onValueChange = {
                                    textVal = it
                                    it.toDoubleOrNull()?.let { d ->
                                        viewModel.updateVariableDirect(v.symbol, d, v.description)
                                    }
                                },
                                modifier = Modifier.width(130.dp),
                                singleLine = true,
                                label = { Text("Value") }
                            )
                            AssistChip(
                                onClick = { viewModel.sendValueToCalculator(v.symbol, "Variable ${v.symbol}") },
                                label = { Text("Use") }
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CalculusLabSheetContent(
    uiState: CalculatorUiState,
    angleUnit: AngleUnit,
    viewModel: CalculatorViewModel
) {
    val palette = LocalExpressivePalette.current
    val expr = uiState.calculusExpression
    val atX = uiState.calculusAtX.toDoubleOrNull() ?: 2.0
    val lowerA = uiState.integralLowerA.toDoubleOrNull() ?: 0.0
    val upperB = uiState.integralUpperB.toDoubleOrNull() ?: 3.0
    val startK = (uiState.seriesStartK.toIntOrNull() ?: 1).coerceIn(-100, 500)
    val endN = (uiState.seriesEndN.toIntOrNull() ?: 10).coerceIn(startK, startK + 500)

    val derivVal = remember(expr, atX, angleUnit) {
        MathEngine.numericalDerivative(expr, atX, angleUnit)
    }
    val integralVal = remember(expr, lowerA, upperB, angleUnit) {
        MathEngine.numericalIntegral(expr, lowerA, upperB, steps = 200, angleUnit = angleUnit)
    }
    val (sumSeries, prodSeries) = remember(expr, startK, endN, angleUnit) {
        var s = 0.0
        var p = 1.0
        for (k in startK..endN) {
            val y = MathEngine.evaluateRealFunction(expr, k.toDouble(), angleUnit)
            if (y.isFinite()) {
                s += y
                p *= y
            }
        }
        s to p
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Symbolic & Numerical Calculus Studio",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        OutlinedTextField(
            value = uiState.calculusExpression,
            onValueChange = { viewModel.updateCalculusFields(expr = it) },
            modifier = Modifier.fillMaxWidth().testTag("calculus_expr_input"),
            singleLine = true,
            label = { Text("Function f(x) (e.g. x^3 - 3*x, sin(x), exp(-x^2))") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.calculusAtX,
                onValueChange = { viewModel.updateCalculusFields(atX = it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Derivative at x =") }
            )
            OutlinedTextField(
                value = uiState.integralLowerA,
                onValueChange = { viewModel.updateCalculusFields(lowerA = it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Integral Lower a") }
            )
            OutlinedTextField(
                value = uiState.integralUpperB,
                onValueChange = { viewModel.updateCalculusFields(upperB = it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Integral Upper b") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.seriesStartK,
                onValueChange = { viewModel.updateCalculusFields(startK = it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Series Start k") }
            )
            OutlinedTextField(
                value = uiState.seriesEndN,
                onValueChange = { viewModel.updateCalculusFields(endN = it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Series End N") }
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.cardElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "d/dx [$expr] at x = $atX  ⇒  ${MathEngine.formatReal(derivVal)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "∫[$lowerA → $upperB] ($expr) dx  ⇒  ${MathEngine.formatReal(integralVal)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary
                )
                ExpressiveWavyDivider()
                Text(
                    text = "Summation Σ(x=$startK..$endN) f(x) = ${MathEngine.formatReal(sumSeries)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Product Π(x=$startK..$endN) f(x) = ${MathEngine.formatReal(prodSeries)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.sendValueToCalculator(derivVal.toString(), "d/dx") }) {
                        Text("Insert d/dx")
                    }
                    OutlinedButton(onClick = { viewModel.sendValueToCalculator(integralVal.toString(), "∫ f(x)dx") }) {
                        Text("Insert ∫ dx")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ConstantsCatalogSheetContent(viewModel: CalculatorViewModel) {
    val palette = LocalExpressivePalette.current
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "52 CODATA Physics & Math Constants",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Filter constants by name, symbol, or category…") }
        )
        val list = CatalogAndConverterEngine.physicsConstants.filter {
            query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.symbol.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.symbol + it.name }) { c ->
                Surface(
                    onClick = { viewModel.sendValueToCalculator(c.valueString, c.name) },
                    shape = RoundedCornerShape(18.dp),
                    color = palette.cardElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${c.symbol} — ${c.name}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("${c.valueString} ${c.unit} (${c.category})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        AssistChip(
                            onClick = { viewModel.sendValueToCalculator(c.valueString, c.name) },
                            label = { Text("Insert") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomFormulasSheetContent(
    formulas: List<CustomFormulaEntity>,
    viewModel: CalculatorViewModel
) {
    val palette = LocalExpressivePalette.current
    var showCreate by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Engineering") }
    var newExpr by remember { mutableStateOf("") }
    var newVars by remember { mutableStateOf("A=input1, B=input2") }
    var newDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 580.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom & Engineering Formulas",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Button(onClick = { showCreate = !showCreate }) {
                Text(if (showCreate) "Close Form" else "+ New Formula")
            }
        }

        if (showCreate) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = palette.cardAccentSoft,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(newTitle, { newTitle = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Formula Title") })
                    OutlinedTextField(newExpr, { newExpr = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Expression using A, B, C (e.g. 0.5 * A * B^2)") })
                    OutlinedTextField(newVars, { newVars = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Variables Legend") })
                    OutlinedTextField(newDesc, { newDesc = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Description") })
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newExpr.isNotBlank()) {
                                viewModel.addCustomFormula(newTitle, newCategory, newExpr, newVars, newDesc)
                                showCreate = false
                                newTitle = ""; newExpr = ""; newDesc = ""
                            }
                        }
                    ) {
                        Text("Save to Local Database")
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(formulas, key = { it.id }) { f ->
                Surface(
                    onClick = {
                        viewModel.setExpression(f.expression)
                        viewModel.closeSheet()
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = palette.cardElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${f.title} (${f.category})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(f.expression, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text("${f.variablesCsv} • ${f.description}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.toggleFavoriteFormula(f) }) {
                            Icon(
                                imageVector = if (f.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite Formula"
                            )
                        }
                        IconButton(onClick = { viewModel.deleteFormula(f.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Formula")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveThemeStudioSheetContent(
    preferences: UserPreferences,
    viewModel: CalculatorViewModel
) {
    val palette = LocalExpressivePalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Material 3 Expressive Theme Studio",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Text("Color Mode", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                ExpressiveStatusChip(
                    label = mode.label,
                    active = preferences.themeMode == mode,
                    onClick = { viewModel.setThemeMode(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text("Expressive Curated Palettes", style = MaterialTheme.typography.titleSmall)
        ExpressiveThemePreset.entries.forEach { preset ->
            val selected = preferences.themePreset == preset && !preferences.dynamicColor
            Surface(
                onClick = {
                    viewModel.setDynamicColor(false)
                    viewModel.setThemePreset(preset)
                },
                shape = RoundedCornerShape(22.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else palette.cardElevated,
                modifier = Modifier.fillMaxWidth().testTag("theme_preset_${preset.name}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(preset.tagline, style = MaterialTheme.typography.bodySmall)
                    }
                    RadioButton(
                        selected = selected,
                        onClick = {
                            viewModel.setDynamicColor(false)
                            viewModel.setThemePreset(preset)
                        }
                    )
                }
            }
        }

        ExpressiveWavyDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Wallpaper Dynamic Material You", style = MaterialTheme.typography.titleSmall)
                Text("Use Android 12+ system wallpaper Monet colors", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = preferences.dynamicColor,
                onCheckedChange = viewModel::setDynamicColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tactile Morphing Haptics", style = MaterialTheme.typography.titleSmall)
                Text("Haptic pulse on morphing keypad and scalloped badge presses", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = preferences.hapticsEnabled,
                onCheckedChange = viewModel::setHapticsEnabled
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
