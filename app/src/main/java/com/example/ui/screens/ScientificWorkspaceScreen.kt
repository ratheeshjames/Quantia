package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedVariableEntity
import com.example.data.UserPreferences
import com.example.domain.EvaluationOutput
import com.example.domain.MathEngine
import com.example.domain.NumberFormatMode
import com.example.ui.ActiveOverlaySheet
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalcKeyShapeStyle
import com.example.ui.components.ExpressiveStatusChip
import com.example.ui.components.ExpressiveWavyDivider
import com.example.ui.components.MorphingCalcButton
import com.example.ui.theme.LocalExpressivePalette

private data class FnChipSpec(val label: String, val token: String, val sub: String = "")

@Composable
fun ScientificWorkspaceScreen(
    uiState: CalculatorUiState,
    preferences: UserPreferences,
    evaluation: EvaluationOutput,
    variables: List<SavedVariableEntity>,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalExpressivePalette.current

    // Use a viewport-bounded Column with proportional weights so the entire calculator
    // fits 100% above the bottom NavigationBar without any clipping or hidden buttons.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. ASYMMETRIC EXPRESSIVE DISPLAY CARD
        ExpressiveDisplayCard(
            expression = uiState.expression,
            evaluation = evaluation,
            preferences = preferences,
            secondShift = uiState.secondShift,
            hypShift = uiState.hypShift,
            onCycleAngle = viewModel::cycleAngleUnit,
            onCycleFormat = viewModel::cycleNumberFormat,
            onSelectFormat = viewModel::setNumberFormat,
            onOpenHistory = { viewModel.openSheet(ActiveOverlaySheet.HISTORY_AND_MEMORY) },
            onOpenCalculus = { viewModel.openSheet(ActiveOverlaySheet.CALCULUS_LAB) },
            onOpenConstants = { viewModel.openSheet(ActiveOverlaySheet.PHYSICS_CONSTANTS) },
            onOpenFormulas = { viewModel.openSheet(ActiveOverlaySheet.CUSTOM_FORMULAS) }
        )

        // 2. MEMORY, VARIABLES & EXPANDABLE SCIENTIFIC FUNCTION STRIP
        MemoryAndScientificStrip(
            uiState = uiState,
            variables = variables,
            hapticsEnabled = preferences.hapticsEnabled,
            viewModel = viewModel
        )

        // 3. MORPHING PRIMARY KEYPAD + SIGNATURE 2x2 / DOCKED OPERATOR HUB
        // Fills all remaining vertical space above the bottom navigation bar using weight(1f)
        ExpressiveMorphingKeypad(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            hapticsEnabled = preferences.hapticsEnabled,
            palette = palette,
            viewModel = viewModel
        )
    }
}

@Composable
private fun ExpressiveDisplayCard(
    expression: String,
    evaluation: EvaluationOutput,
    preferences: UserPreferences,
    secondShift: Boolean,
    hypShift: Boolean,
    onCycleAngle: () -> Unit,
    onCycleFormat: () -> Unit,
    onSelectFormat: (NumberFormatMode) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCalculus: () -> Unit,
    onOpenConstants: () -> Unit,
    onOpenFormulas: () -> Unit
) {
    val palette = LocalExpressivePalette.current
    val exprScroll = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expressive_display_card"),
        shape = RoundedCornerShape(26.dp),
        color = palette.cardElevated,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Expressive Row: Warm Result Pill Badge + History Circle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onCycleFormat,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("result_format_pill"),
                    shape = RoundedCornerShape(50),
                    color = palette.resultPillContainer.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = palette.resultPillContainer
                        ) {
                            Text(
                                text = "${preferences.numberFormat.label}: ${evaluation.formattedPrimary}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.resultPillContent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = if (evaluation.formattedFraction.isNotEmpty() && preferences.numberFormat != NumberFormatMode.FRACTION) {
                                "≈ ${evaluation.formattedFraction}"
                            } else {
                                evaluation.formattedPolar
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_history_button"),
                    shape = CircleShape,
                    color = palette.backspaceKeyContainer.copy(alpha = 0.65f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Open Calculation History & Memory Tape",
                            tint = palette.backspaceKeyContent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compact Status & Quick Tool Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactPillChip(
                    label = preferences.angleUnit.label,
                    active = true,
                    onClick = onCycleAngle,
                    tag = "angle_unit_chip"
                )
                NumberFormatMode.entries.forEach { mode ->
                    CompactPillChip(
                        label = mode.label,
                        active = preferences.numberFormat == mode,
                        onClick = { onSelectFormat(mode) },
                        tag = "format_chip_${mode.name}"
                    )
                }
                CompactPillChip(
                    label = "∫ d/dx Lab",
                    active = false,
                    onClick = onOpenCalculus,
                    tag = "open_calculus_chip"
                )
                CompactPillChip(
                    label = "52 Constants",
                    active = false,
                    onClick = onOpenConstants,
                    tag = "open_constants_chip"
                )
                CompactPillChip(
                    label = "Formulas",
                    active = false,
                    onClick = onOpenFormulas,
                    tag = "open_formulas_chip"
                )
            }

            // Giant Expression Readout + Live Result in a clean high-contrast block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScroll),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (expression.length > 16) 24.sp else 30.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("expression_display_text")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (evaluation.primeFactors.isNotEmpty()) {
                        Text(
                            text = "Factors: ${evaluation.primeFactors}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (secondShift || hypShift) {
                        Text(
                            text = listOfNotNull(
                                if (secondShift) "2nd SHIFT" else null,
                                if (hypShift) "HYP" else null
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Text(
                    text = "= ${evaluation.formattedPrimary}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (evaluation.error != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("live_result_text")
                )
            }
        }
    }
}

@Composable
private fun CompactPillChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (!active) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null,
        modifier = Modifier
            .height(28.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun MemoryAndScientificStrip(
    uiState: CalculatorUiState,
    variables: List<SavedVariableEntity>,
    hapticsEnabled: Boolean,
    viewModel: CalculatorViewModel
) {
    val palette = LocalExpressivePalette.current
    val memVal = variables.firstOrNull { it.symbol == "M" }?.realValue ?: 0.0
    val rowHeight = 36.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Memory & Expand Drawer Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MorphingCalcButton(
                    label = "2nd",
                    onClick = viewModel::toggleSecondShift,
                    modifier = Modifier.weight(1f).height(rowHeight),
                    containerColor = if (uiState.secondShift) MaterialTheme.colorScheme.primary else palette.fnKeyContainer,
                    contentColor = if (uiState.secondShift) MaterialTheme.colorScheme.onPrimary else palette.fnKeyContent,
                    shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                    fontSize = 12.sp,
                    hapticsEnabled = hapticsEnabled
                )
                MorphingCalcButton(
                    label = "HYP",
                    onClick = viewModel::toggleHypShift,
                    modifier = Modifier.weight(1f).height(rowHeight),
                    containerColor = if (uiState.hypShift) MaterialTheme.colorScheme.tertiary else palette.fnKeyContainer,
                    contentColor = if (uiState.hypShift) MaterialTheme.colorScheme.onTertiary else palette.fnKeyContent,
                    shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                    fontSize = 12.sp,
                    hapticsEnabled = hapticsEnabled
                )
                MorphingCalcButton(
                    label = "M+",
                    onClick = viewModel::memoryAdd,
                    modifier = Modifier.weight(1f).height(rowHeight),
                    containerColor = palette.fnKeyContainer,
                    contentColor = palette.fnKeyContent,
                    shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                    fontSize = 12.sp,
                    hapticsEnabled = hapticsEnabled
                )
                MorphingCalcButton(
                    label = "M−",
                    onClick = viewModel::memorySubtract,
                    modifier = Modifier.weight(1f).height(rowHeight),
                    containerColor = palette.fnKeyContainer,
                    contentColor = palette.fnKeyContent,
                    shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                    fontSize = 12.sp,
                    hapticsEnabled = hapticsEnabled
                )
                MorphingCalcButton(
                    label = if (memVal != 0.0) "MR*" else "MR",
                    onClick = viewModel::memoryRecall,
                    onLongClick = viewModel::memoryClear,
                    modifier = Modifier.weight(1f).height(rowHeight),
                    containerColor = palette.fnKeyContainer,
                    contentColor = palette.fnKeyContent,
                    shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                    fontSize = 12.sp,
                    hapticsEnabled = hapticsEnabled
                )
                Surface(
                    onClick = viewModel::toggleFunctionDrawer,
                    modifier = Modifier
                        .height(rowHeight)
                        .testTag("toggle_fn_drawer_button"),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (uiState.isFunctionDrawerExpanded) "Less" else "More",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = if (uiState.isFunctionDrawerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Function Sheet",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Core Scientific Row 1
            val trigSin = when {
                uiState.secondShift && uiState.hypShift -> FnChipSpec("asinh", "asinh(")
                uiState.secondShift -> FnChipSpec("sin⁻¹", "asin(")
                uiState.hypShift -> FnChipSpec("sinh", "sinh(")
                else -> FnChipSpec("sin", "sin(")
            }
            val trigCos = when {
                uiState.secondShift && uiState.hypShift -> FnChipSpec("acosh", "acosh(")
                uiState.secondShift -> FnChipSpec("cos⁻¹", "acos(")
                uiState.hypShift -> FnChipSpec("cosh", "cosh(")
                else -> FnChipSpec("cos", "cos(")
            }
            val trigTan = when {
                uiState.secondShift && uiState.hypShift -> FnChipSpec("atanh", "atanh(")
                uiState.secondShift -> FnChipSpec("tan⁻¹", "atan(")
                uiState.hypShift -> FnChipSpec("tanh", "tanh(")
                else -> FnChipSpec("tan", "tan(")
            }
            val logKey = if (uiState.secondShift) FnChipSpec("10ˣ", "10^(") else FnChipSpec("log", "log(")
            val lnKey = if (uiState.secondShift) FnChipSpec("eˣ", "exp(") else FnChipSpec("ln", "ln(")
            val rootKey = if (uiState.secondShift) FnChipSpec("∛x", "cbrt(") else FnChipSpec("√x", "sqrt(")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(trigSin, trigCos, trigTan, logKey, lnKey, rootKey).forEach { spec ->
                    MorphingCalcButton(
                        label = spec.label,
                        onClick = { viewModel.appendToken(spec.token) },
                        modifier = Modifier.weight(1f).height(rowHeight),
                        containerColor = palette.fnKeyContainer,
                        contentColor = palette.fnKeyContent,
                        shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                        fontSize = 13.sp,
                        hapticsEnabled = hapticsEnabled
                    )
                }
            }

            // Core Scientific Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val row2Specs = listOf(
                    FnChipSpec("xʸ", "^"),
                    FnChipSpec(if (uiState.secondShift) "x³" else "x²", if (uiState.secondShift) "³" else "²"),
                    FnChipSpec("n!", "!"),
                    FnChipSpec("π", "π"),
                    FnChipSpec("e", "e"),
                    FnChipSpec("i", "i")
                )
                row2Specs.forEach { spec ->
                    MorphingCalcButton(
                        label = spec.label,
                        onClick = { viewModel.appendToken(spec.token) },
                        modifier = Modifier.weight(1f).height(rowHeight),
                        containerColor = palette.fnKeyContainer,
                        contentColor = palette.fnKeyContent,
                        shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                        fontSize = 13.sp,
                        hapticsEnabled = hapticsEnabled
                    )
                }
            }

            // Expandable Categorized Scientific Sheet
            AnimatedVisibility(
                visible = uiState.isFunctionDrawerExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categories = listOf(
                        "Trig & Hyp",
                        "Combinatorics & Num",
                        "Complex & Polar",
                        "Variables A–F"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            CompactPillChip(
                                label = cat,
                                active = uiState.selectedDrawerCategory == cat,
                                onClick = { viewModel.selectDrawerCategory(cat) },
                                tag = "drawer_cat_$cat"
                            )
                        }
                    }

                    val activeSpecs = when (uiState.selectedDrawerCategory) {
                        "Trig & Hyp" -> listOf(
                            FnChipSpec("cot", "cot("),
                            FnChipSpec("sec", "sec("),
                            FnChipSpec("csc", "csc("),
                            FnChipSpec("sinh", "sinh("),
                            FnChipSpec("cosh", "cosh("),
                            FnChipSpec("tanh", "tanh("),
                            FnChipSpec("atan2", "atan2("),
                            FnChipSpec("deg", "deg("),
                            FnChipSpec("rad", "rad("),
                            FnChipSpec("hypot", "hypot(")
                        )
                        "Combinatorics & Num" -> listOf(
                            FnChipSpec("nCr", "nCr"),
                            FnChipSpec("nPr", "nPr"),
                            FnChipSpec("Γ(x)", "gamma("),
                            FnChipSpec("gcd", "gcd("),
                            FnChipSpec("lcm", "lcm("),
                            FnChipSpec("mod", "mod"),
                            FnChipSpec("floor", "floor("),
                            FnChipSpec("ceil", "ceil("),
                            FnChipSpec("round", "round("),
                            FnChipSpec("log₂", "log2(")
                        )
                        "Complex & Polar" -> listOf(
                            FnChipSpec("|z|", "abs("),
                            FnChipSpec("arg", "arg("),
                            FnChipSpec("conj", "conj("),
                            FnChipSpec("Re", "re("),
                            FnChipSpec("Im", "im("),
                            FnChipSpec("r∠θ", "∠"),
                            FnChipSpec("polar", "polar("),
                            FnChipSpec("φ", "φ")
                        )
                        else -> variables.filter { it.symbol != "M" }.map { v ->
                            FnChipSpec(
                                label = "${v.symbol}=${MathEngine.formatReal(v.realValue)}",
                                token = v.symbol
                            )
                        }
                    }

                    activeSpecs.chunked(5).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            chunk.forEach { spec ->
                                val isVar = uiState.selectedDrawerCategory == "Variables A–F"
                                MorphingCalcButton(
                                    label = spec.label,
                                    onClick = { viewModel.appendToken(spec.token) },
                                    onLongClick = if (isVar) {
                                        { viewModel.storeCurrentInVariable(spec.token) }
                                    } else null,
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    containerColor = palette.cardAccentSoft,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                                    fontSize = 11.sp,
                                    hapticsEnabled = hapticsEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveMorphingKeypad(
    modifier: Modifier = Modifier,
    hapticsEnabled: Boolean,
    palette: com.example.ui.theme.ExpressivePalette,
    viewModel: CalculatorViewModel
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: AC, ( ), %, Backspace Tag Button (Weighted so it scales proportionally)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.95f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MorphingCalcButton(
                label = "AC",
                onClick = viewModel::clearAll,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shapeStyle = CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE,
                fontSize = 18.sp,
                hapticsEnabled = hapticsEnabled,
                tag = "key_ac"
            )
            MorphingCalcButton(
                label = "( )",
                onClick = viewModel::insertSmartParenthesis,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = palette.fnKeyContainer,
                contentColor = palette.fnKeyContent,
                shapeStyle = CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE,
                fontSize = 18.sp,
                hapticsEnabled = hapticsEnabled,
                tag = "key_parens"
            )
            MorphingCalcButton(
                label = "%",
                onClick = { viewModel.appendToken("%") },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = palette.fnKeyContainer,
                contentColor = palette.fnKeyContent,
                shapeStyle = CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE,
                fontSize = 20.sp,
                hapticsEnabled = hapticsEnabled,
                tag = "key_percent"
            )
            MorphingCalcButton(
                label = "⌫",
                subLabel = "Hold:AC",
                onClick = viewModel::backspace,
                onLongClick = viewModel::clearAll,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = palette.backspaceKeyContainer,
                contentColor = palette.backspaceKeyContent,
                shapeStyle = CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE,
                fontSize = 20.sp,
                hapticsEnabled = hapticsEnabled,
                tag = "key_backspace",
                accessibilityLabel = "Backspace"
            )
        }

        // Main Split Grid: Left 3x4 Number Matrix + Right Expressive Operator Cluster
        // Uses weight(4f) and fillMaxHeight() so all 4 bottom rows stay 100% visible above the NavigationBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val numberRows = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3"),
                    listOf("Ans", "0", ".")
                )
                numberRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            MorphingCalcButton(
                                label = key,
                                onClick = { viewModel.appendToken(key) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                containerColor = palette.numberKeyContainer,
                                contentColor = palette.numberKeyContent,
                                shapeStyle = CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE,
                                fontSize = if (key == "Ans") 16.sp else 22.sp,
                                hapticsEnabled = hapticsEnabled,
                                tag = "key_$key"
                            )
                        }
                    }
                }
            }

            // Right Expressive Operator Hub (Fills exact height of the 4 number rows)
            Surface(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight()
                    .testTag("floating_operator_hub"),
                shape = RoundedCornerShape(26.dp),
                color = palette.operatorClusterSurface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        MorphingCalcButton(
                            label = "+",
                            onClick = { viewModel.appendToken("+") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = palette.operatorKeyContainer,
                            contentColor = palette.operatorKeyContent,
                            shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                            fontSize = 20.sp,
                            hapticsEnabled = hapticsEnabled,
                            tag = "key_plus",
                            accessibilityLabel = "Add"
                        )
                        MorphingCalcButton(
                            label = "−",
                            onClick = { viewModel.appendToken("−") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = palette.operatorKeyContainer,
                            contentColor = palette.operatorKeyContent,
                            shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                            fontSize = 20.sp,
                            hapticsEnabled = hapticsEnabled,
                            tag = "key_minus",
                            accessibilityLabel = "Subtract"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        MorphingCalcButton(
                            label = "×",
                            onClick = { viewModel.appendToken("×") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = palette.operatorKeyContainer,
                            contentColor = palette.operatorKeyContent,
                            shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                            fontSize = 20.sp,
                            hapticsEnabled = hapticsEnabled,
                            tag = "key_multiply",
                            accessibilityLabel = "Multiply"
                        )
                        MorphingCalcButton(
                            label = "÷",
                            onClick = { viewModel.appendToken("÷") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = palette.operatorKeyContainer,
                            contentColor = palette.operatorKeyContent,
                            shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                            fontSize = 20.sp,
                            hapticsEnabled = hapticsEnabled,
                            tag = "key_divide",
                            accessibilityLabel = "Divide"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.8f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        MorphingCalcButton(
                            label = ",",
                            onClick = { viewModel.appendToken(",") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = palette.fnKeyContainer,
                            contentColor = palette.fnKeyContent,
                            shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                            fontSize = 15.sp,
                            hapticsEnabled = hapticsEnabled,
                            tag = "key_comma"
                        )
                        MorphingCalcButton(
                            label = "1/x",
                            onClick = { viewModel.appendToken("^(-1)") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            containerColor = palette.fnKeyContainer,
                            contentColor = palette.fnKeyContent,
                            shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                            fontSize = 12.sp,
                            hapticsEnabled = hapticsEnabled,
                            tag = "key_reciprocal"
                        )
                    }

                    // Signature 12-Lobe Scalloped Cookie Evaluate (=) Badge
                    MorphingCalcButton(
                        label = "=",
                        subLabel = "EVAL",
                        onClick = { viewModel.evaluateAndCommit("Scientific") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.35f),
                        containerColor = palette.scallopedBadgeContainer,
                        contentColor = palette.scallopedBadgeContent,
                        shapeStyle = CalcKeyShapeStyle.SCALLOPED_BADGE,
                        fontSize = 24.sp,
                        hapticsEnabled = hapticsEnabled,
                        tag = "key_equals",
                        accessibilityLabel = "Evaluate Expression"
                    )
                }
            }
        }
    }
}
