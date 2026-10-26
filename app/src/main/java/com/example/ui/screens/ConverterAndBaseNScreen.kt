package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CatalogAndConverterEngine
import com.example.domain.MathEngine
import com.example.domain.WordSize
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalcKeyShapeStyle
import com.example.ui.components.ExpressiveStatusChip
import com.example.ui.components.ExpressiveWavyDivider
import com.example.ui.components.MorphingCalcButton
import com.example.ui.theme.LocalExpressivePalette

@Composable
fun ConverterAndBaseNScreen(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalExpressivePalette.current
    var activeSubTab by remember { mutableStateOf("30 Converters") }

    // Converter State
    var categorySearch by remember { mutableStateOf("") }
    val filteredCategories = remember(categorySearch) {
        if (categorySearch.isBlank()) CatalogAndConverterEngine.unitCategories
        else CatalogAndConverterEngine.unitCategories.filter {
            it.title.contains(categorySearch, ignoreCase = true) ||
                it.units.any { u -> u.name.contains(categorySearch, ignoreCase = true) }
        }
    }
    var selectedCategory by remember { mutableStateOf(CatalogAndConverterEngine.unitCategories.first()) }
    var fromUnit by remember(selectedCategory) { mutableStateOf(selectedCategory.units.first()) }
    var toUnit by remember(selectedCategory) {
        mutableStateOf(selectedCategory.units.getOrElse(1) { selectedCategory.units.first() })
    }
    var inputAmount by remember { mutableStateOf("100") }

    // Constants Explorer State
    var constantQuery by remember { mutableStateOf("") }
    var constantCategory by remember { mutableStateOf("All") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sub-Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("30 Converters", "Base-N Bitwise", "52 Constants").forEach { tab ->
                ExpressiveStatusChip(
                    label = tab,
                    active = activeSubTab == tab,
                    onClick = { activeSubTab = tab },
                    modifier = Modifier.weight(1f),
                    tag = "convert_subtab_$tab"
                )
            }
        }

        when (activeSubTab) {
            "30 Converters" -> {
                OutlinedTextField(
                    value = categorySearch,
                    onValueChange = { categorySearch = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unit_category_search"),
                    singleLine = true,
                    label = { Text("Search 30 Unit Categories (Length, Torque, Pressure, Storage…)") }
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filteredCategories) { cat ->
                        ExpressiveStatusChip(
                            label = cat.title,
                            active = selectedCategory.id == cat.id,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                val numericIn = inputAmount.toDoubleOrNull() ?: 0.0
                val convertedValue = CatalogAndConverterEngine.convertUnit(selectedCategory, fromUnit, toUnit, numericIn)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${selectedCategory.title} Converter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("converter_amount_input"),
                            singleLine = true,
                            label = { Text("Input Value (${fromUnit.name} [${fromUnit.symbol}])") }
                        )

                        Text("From Unit:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(selectedCategory.units) { u ->
                                FilterChip(
                                    selected = fromUnit.symbol == u.symbol,
                                    onClick = { fromUnit = u },
                                    label = { Text("${u.symbol} (${u.name})") }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("To Unit:", style = MaterialTheme.typography.labelMedium)
                            FilledTonalIconButton(
                                onClick = {
                                    val tmp = fromUnit
                                    fromUnit = toUnit
                                    toUnit = tmp
                                },
                                modifier = Modifier.size(48.dp).testTag("swap_units_button")
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = "Swap From and To Units")
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(selectedCategory.units) { u ->
                                FilterChip(
                                    selected = toUnit.symbol == u.symbol,
                                    onClick = { toUnit = u },
                                    label = { Text("${u.symbol} (${u.name})") }
                                )
                            }
                        }

                        // Highlighted Conversion Output Pill
                        Surface(
                            onClick = {
                                viewModel.sendValueToCalculator(
                                    convertedValue.toString(),
                                    "${fromUnit.symbol}→${toUnit.symbol}"
                                )
                            },
                            shape = RoundedCornerShape(22.dp),
                            color = palette.resultPillContainer,
                            modifier = Modifier.fillMaxWidth().testTag("converted_result_pill")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${MathEngine.formatReal(numericIn)} ${fromUnit.symbol} =",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.resultPillContent.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${MathEngine.formatReal(convertedValue)} ${toUnit.symbol}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.resultPillContent
                                )
                                Text(
                                    text = "Tap to send to Scientific Calculator",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.resultPillContent.copy(alpha = 0.7f)
                                )
                            }
                        }

                        ExpressiveWavyDivider()
                        Text(
                            text = "All ${selectedCategory.title} Equivalents",
                            style = MaterialTheme.typography.titleSmall
                        )
                        selectedCategory.units.forEach { u ->
                            val eqVal = CatalogAndConverterEngine.convertUnit(selectedCategory, fromUnit, u, numericIn)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${u.name} (${u.symbol})", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = MathEngine.formatReal(eqVal),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            "Base-N Bitwise" -> {
                val valA = uiState.programmerValue
                val valB = uiState.programmerOperandB
                val ws = uiState.programmerWordSize
                val bitResult = CatalogAndConverterEngine.applyBitwiseOp(valA, valB, uiState.programmerOp, ws)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Base-N Programmer & Bitwise Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            WordSize.entries.forEach { size ->
                                ExpressiveStatusChip(
                                    label = size.label,
                                    active = ws == size,
                                    onClick = { viewModel.setProgrammerWordSize(size) }
                                )
                            }
                        }

                        // Live Simultaneous Readouts: HEX, DEC (Signed/Unsigned), OCT, BIN
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("HEX: 0x${CatalogAndConverterEngine.formatHex(valA, ws)}", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "DEC (Signed): ${CatalogAndConverterEngine.toSignedValue(valA, ws)}  •  Unsigned: ${java.lang.Long.toUnsignedString(valA)}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text("OCT: 0o${CatalogAndConverterEngine.formatOct(valA, ws)}", style = MaterialTheme.typography.labelLarge)
                                Text("BIN: ${CatalogAndConverterEngine.formatBin(valA, ws)}", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Interactive 16-bit / 8-bit Bit-Toggle Matrix
                        Text(
                            text = "Interactive Bit Flipper (Tap any bit 15..0 to toggle)",
                            style = MaterialTheme.typography.labelMedium
                        )
                        val visibleBits = minOf(ws.bits, 16)
                        for (rowStart in (visibleBits - 1) downTo 0 step 8) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (bit in rowStart downTo (rowStart - 7).coerceAtLeast(0)) {
                                    val isOne = ((valA ushr bit) and 1L) == 1L
                                    MorphingCalcButton(
                                        label = if (isOne) "1" else "0",
                                        subLabel = "b$bit",
                                        onClick = { viewModel.toggleProgrammerBit(bit) },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        containerColor = if (isOne) palette.scallopedBadgeContainer else palette.numberKeyContainer,
                                        contentColor = if (isOne) palette.scallopedBadgeContent else palette.numberKeyContent,
                                        shapeStyle = CalcKeyShapeStyle.SQUIRCLE_TO_PILL,
                                        fontSize = 16.sp,
                                        hapticsEnabled = hapticsEnabled,
                                        tag = "bit_toggle_$bit"
                                    )
                                }
                            }
                        }

                        ExpressiveWavyDivider()
                        // Operand A & B Decimal / Hex inputs and Bitwise Operations
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = valA.toString(),
                                onValueChange = { it.toLongOrNull()?.let(viewModel::setProgrammerValue) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Register A (DEC)") }
                            )
                            OutlinedTextField(
                                value = valB.toString(),
                                onValueChange = { it.toLongOrNull()?.let(viewModel::setProgrammerOperandB) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Register B / Shift") }
                            )
                        }

                        val bitOps = listOf("AND", "OR", "XOR", "NAND", "NOR", "NOT", "SHL", "SHR", "ROL", "ROR")
                        bitOps.chunked(5).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { op ->
                                    MorphingCalcButton(
                                        label = op,
                                        onClick = { viewModel.setProgrammerOp(op) },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        containerColor = if (uiState.programmerOp == op) MaterialTheme.colorScheme.primary else palette.fnKeyContainer,
                                        contentColor = if (uiState.programmerOp == op) MaterialTheme.colorScheme.onPrimary else palette.fnKeyContent,
                                        shapeStyle = CalcKeyShapeStyle.SOFT_PILL,
                                        fontSize = 13.sp,
                                        hapticsEnabled = hapticsEnabled
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.resultPillContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "A ${uiState.programmerOp} B  →  0x${CatalogAndConverterEngine.formatHex(bitResult, ws)} (${CatalogAndConverterEngine.toSignedValue(bitResult, ws)})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.resultPillContent
                                )
                                Text(
                                    text = "BIN: ${CatalogAndConverterEngine.formatBin(bitResult, ws)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.resultPillContent
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                // 52 CODATA Physics, Chemistry, Astronomy & Math Constants
                OutlinedTextField(
                    value = constantQuery,
                    onValueChange = { constantQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search 52 CODATA Constants (Planck, Boltzmann, Solar Mass…)") }
                )

                val cats = listOf("All", "Universal", "Quantum", "Atomic", "Thermo", "Chemistry", "Astronomy", "Math")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cats) { cat ->
                        ExpressiveStatusChip(
                            label = cat,
                            active = constantCategory == cat,
                            onClick = { constantCategory = cat }
                        )
                    }
                }

                val constants = CatalogAndConverterEngine.physicsConstants.filter { c ->
                    (constantCategory == "All" || c.category == constantCategory) &&
                        (constantQuery.isBlank() || c.name.contains(constantQuery, ignoreCase = true) || c.symbol.contains(constantQuery, ignoreCase = true))
                }

                constants.forEach { c ->
                    Surface(
                        onClick = { viewModel.sendValueToCalculator(c.valueString, c.name) },
                        shape = RoundedCornerShape(20.dp),
                        color = palette.cardElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${c.symbol} — ${c.name}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${c.valueString} ${c.unit}  •  ${c.category}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
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
}
