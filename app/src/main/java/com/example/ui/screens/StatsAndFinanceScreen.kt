package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.MathEngine
import com.example.domain.StatsAndFinanceEngine
import com.example.ui.CalculatorViewModel
import com.example.ui.components.ExpressiveStatusChip
import com.example.ui.components.ExpressiveWavyDivider
import com.example.ui.components.ScallopedCookieShape
import com.example.ui.components.WavyProgressIndicator
import com.example.ui.theme.LocalExpressivePalette

@Composable
fun StatsAndFinanceScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalExpressivePalette.current
    var activeTab by remember { mutableStateOf("Statistics") }

    // Statistics Dataset & Regression State
    var datasetXInput by remember { mutableStateOf("12, 18, 24, 32, 28, 41, 36") }
    var datasetYInput by remember { mutableStateOf("22, 35, 42, 58, 51, 79, 68") }
    var targetPredictX by remember { mutableStateOf("50") }

    // Probability Distributions State
    var distType by remember { mutableStateOf("Normal") }
    var param1 by remember { mutableStateOf("1.2") } // x or k
    var param2 by remember { mutableStateOf("0.0") } // mean or n or lambda
    var param3 by remember { mutableStateOf("1.0") } // stdDev or p

    // Financial Suite State
    var loanPrincipal by remember { mutableStateOf("12345.56") }
    var annualRate by remember { mutableStateOf("6.5") }
    var loanYears by remember { mutableStateOf("5") }
    var monthlyContrib by remember { mutableStateOf("250") }
    var billAmount by remember { mutableStateOf("145.80") }
    var tipPercent by remember { mutableStateOf("18") }
    var taxPercent by remember { mutableStateOf("8.25") }
    var splitCount by remember { mutableStateOf("4") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Statistics", "Distributions", "Finance & TVM").forEach { tab ->
                ExpressiveStatusChip(
                    label = tab,
                    active = activeTab == tab,
                    onClick = { activeTab = tab },
                    modifier = Modifier.weight(1f),
                    tag = "stats_subtab_$tab"
                )
            }
        }

        when (activeTab) {
            "Statistics" -> {
                val xValues = remember(datasetXInput) {
                    datasetXInput.split(",", " ").mapNotNull { it.trim().toDoubleOrNull() }
                }
                val yValues = remember(datasetYInput) {
                    datasetYInput.split(",", " ").mapNotNull { it.trim().toDoubleOrNull() }
                }
                val stats = remember(xValues) { StatsAndFinanceEngine.analyzeDataset(xValues) }
                val regression = remember(xValues, yValues, targetPredictX) {
                    StatsAndFinanceEngine.computeLinearRegression(
                        xValues,
                        yValues,
                        targetPredictX.toDoubleOrNull() ?: 10.0
                    )
                }

                // Expressive Rounded Pill Bar Chart Card (Inspired by the "Steps 5,586" reference screenshot)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stats?.let { MathEngine.formatReal(it.mean) } ?: "0",
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Dataset Mean (x̄)  •  N = ${xValues.size} samples",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = ScallopedCookieShape(10, 0.09f),
                                color = palette.scallopedBadgeContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "σ",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = palette.scallopedBadgeContent
                                    )
                                }
                            }
                        }

                        // Rounded Pill Bar Chart
                        if (xValues.isNotEmpty()) {
                            val maxVal = (xValues.maxOrNull() ?: 1.0).coerceAtLeast(1e-6)
                            val barColor = MaterialTheme.colorScheme.primary
                            val peakColor = palette.scallopedBadgeContainer
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                val count = xValues.size
                                val gap = 10.dp.toPx()
                                val barW = ((size.width - gap * (count - 1)) / count).coerceAtLeast(12f)
                                xValues.forEachIndexed { idx, v ->
                                    val ratio = (v / maxVal).toFloat().coerceIn(0.12f, 1f)
                                    val barH = size.height * ratio
                                    val x = idx * (barW + gap)
                                    val y = size.height - barH
                                    drawRoundRect(
                                        color = if (v == maxVal) peakColor else barColor,
                                        topLeft = Offset(x, y),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = datasetXInput,
                            onValueChange = { datasetXInput = it },
                            modifier = Modifier.fillMaxWidth().testTag("stats_x_input"),
                            singleLine = true,
                            label = { Text("Dataset X (comma-separated values)") }
                        )

                        if (stats != null) {
                            ExpressiveWavyDivider()
                            val metrics = listOf(
                                "Sum (Σx)" to MathEngine.formatReal(stats.sum),
                                "Median (Q2)" to MathEngine.formatReal(stats.median),
                                "Mode" to stats.mode,
                                "Sample StdDev (s)" to MathEngine.formatReal(stats.sampleStdDev),
                                "Pop StdDev (σ)" to MathEngine.formatReal(stats.popStdDev),
                                "Sample Variance (s²)" to MathEngine.formatReal(stats.sampleVariance),
                                "Quartiles Q1 / Q3" to "${MathEngine.formatReal(stats.q1)} / ${MathEngine.formatReal(stats.q3)}",
                                "IQR (Q3 − Q1)" to MathEngine.formatReal(stats.iqr),
                                "Min / Max / Range" to "${MathEngine.formatReal(stats.min)} / ${MathEngine.formatReal(stats.max)} (${MathEngine.formatReal(stats.range)})",
                                "Geometric / Harmonic" to "${MathEngine.formatReal(stats.geometricMean)} / ${MathEngine.formatReal(stats.harmonicMean)}"
                            )
                            metrics.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(k, style = MaterialTheme.typography.bodySmall)
                                    Text(v, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                // 2-Variable Linear Regression Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = palette.cardAccentSoft
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "2-Variable Linear Regression (ŷ = mx + b)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = datasetYInput,
                                onValueChange = { datasetYInput = it },
                                modifier = Modifier.weight(2f),
                                singleLine = true,
                                label = { Text("Dataset Y (comma-separated)") }
                            )
                            OutlinedTextField(
                                value = targetPredictX,
                                onValueChange = { targetPredictX = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Predict at x") }
                            )
                        }
                        if (regression != null) {
                            Text(
                                text = regression.equationText,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Correlation r = ${MathEngine.formatReal(regression.correlationR)}  •  R² = ${MathEngine.formatReal(regression.rSquared)}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            WavyProgressIndicator(progress = regression.rSquared.toFloat())
                            Text(
                                text = "Predicted ŷ(${targetPredictX}) = ${MathEngine.formatReal(regression.predictedYAtTarget)}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            "Distributions" -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.cardElevated
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Probability Distributions Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Normal", "Binomial", "Poisson").forEach { dt ->
                                ExpressiveStatusChip(
                                    label = dt,
                                    active = distType == dt,
                                    onClick = {
                                        distType = dt
                                        when (dt) {
                                            "Normal" -> { param1 = "1.2"; param2 = "0.0"; param3 = "1.0" }
                                            "Binomial" -> { param1 = "6"; param2 = "10"; param3 = "0.5" }
                                            "Poisson" -> { param1 = "3"; param2 = "4.2" }
                                        }
                                    }
                                )
                            }
                        }

                        val distRes = when (distType) {
                            "Normal" -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(param1, { param1 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Target x") })
                                    OutlinedTextField(param2, { param2 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Mean μ") })
                                    OutlinedTextField(param3, { param3 = it }, Modifier.weight(1f), singleLine = true, label = { Text("StdDev σ") })
                                }
                                StatsAndFinanceEngine.normalDistribution(
                                    param1.toDoubleOrNull() ?: 0.0,
                                    param2.toDoubleOrNull() ?: 0.0,
                                    param3.toDoubleOrNull() ?: 1.0
                                )
                            }
                            "Binomial" -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(param1, { param1 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Successes k") })
                                    OutlinedTextField(param2, { param2 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Trials n") })
                                    OutlinedTextField(param3, { param3 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Prob p (0-1)") })
                                }
                                StatsAndFinanceEngine.binomialDistribution(
                                    param1.toIntOrNull() ?: 1,
                                    param2.toIntOrNull() ?: 10,
                                    param3.toDoubleOrNull() ?: 0.5
                                )
                            }
                            else -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(param1, { param1 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Events k") })
                                    OutlinedTextField(param2, { param2 = it }, Modifier.weight(1f), singleLine = true, label = { Text("Rate λ") })
                                }
                                StatsAndFinanceEngine.poissonDistribution(
                                    param1.toIntOrNull() ?: 2,
                                    param2.toDoubleOrNull() ?: 3.5
                                )
                            }
                        }

                        ExpressiveWavyDivider()
                        Text(distRes.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                        // Leafit-inspired Wavy Progress Cards for Probabilities
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Density / Exact Probability P(X = x): ${MathEngine.formatReal(distRes.exactProb * 100)}%",
                                style = MaterialTheme.typography.labelLarge
                            )
                            WavyProgressIndicator(progress = distRes.exactProb.toFloat())

                            Text(
                                text = "Cumulative Lower Tail P(X ≤ x): ${MathEngine.formatReal(distRes.cumulativeLower * 100)}%",
                                style = MaterialTheme.typography.labelLarge
                            )
                            WavyProgressIndicator(
                                progress = distRes.cumulativeLower.toFloat(),
                                activeColor = MaterialTheme.colorScheme.tertiary
                            )

                            Text(
                                text = "Upper Tail P(X > x): ${MathEngine.formatReal(distRes.cumulativeUpper * 100)}%",
                                style = MaterialTheme.typography.labelLarge
                            )
                            WavyProgressIndicator(
                                progress = distRes.cumulativeUpper.toFloat(),
                                activeColor = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Text(
                            text = "Expected Value E[X] = ${MathEngine.formatReal(distRes.expectedMean)}  •  Variance Var(X) = ${MathEngine.formatReal(distRes.variance)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            else -> {
                // Financial Calculators (Styled after the "Total budget $12,345.56 / $935.9 Spent" reference image)
                val finRes = StatsAndFinanceEngine.computeLoanAndTvm(
                    principal = loanPrincipal.toDoubleOrNull() ?: 12345.56,
                    annualRatePercent = annualRate.toDoubleOrNull() ?: 6.5,
                    years = loanYears.toDoubleOrNull() ?: 5.0,
                    monthlyContribution = monthlyContrib.toDoubleOrNull() ?: 250.0
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Lime Green Expressive Card (like "$935.9 Spent" card in reference)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = palette.scallopedBadgeContainer
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "$${MathEngine.formatReal(finRes.monthlyPayment)}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.scallopedBadgeContent
                            )
                            Text(
                                text = "Monthly Payment",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.scallopedBadgeContent.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Principal ratio: ${(finRes.principalRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.scallopedBadgeContent
                            )
                        }
                    }

                    // Dark Espresso/Contrast Card (like "$12,345.56 Total budget" card in reference)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = palette.operatorKeyContainer
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "$${MathEngine.formatReal(finRes.futureValueCompound)}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.operatorKeyContent
                            )
                            Text(
                                text = "Compound Future Value",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.operatorKeyContent.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "EAR: ${MathEngine.formatReal(finRes.effectiveAnnualRate)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.operatorKeyContent
                            )
                        }
                    }
                }

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
                            text = "Loan Amortization & TVM Compound Growth",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(loanPrincipal, { loanPrincipal = it }, Modifier.weight(1f), singleLine = true, label = { Text("Principal ($)") })
                            OutlinedTextField(annualRate, { annualRate = it }, Modifier.weight(1f), singleLine = true, label = { Text("APR (%)") })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(loanYears, { loanYears = it }, Modifier.weight(1f), singleLine = true, label = { Text("Term (Years)") })
                            OutlinedTextField(monthlyContrib, { monthlyContrib = it }, Modifier.weight(1f), singleLine = true, label = { Text("Monthly Deposit ($)") })
                        }
                        WavyProgressIndicator(progress = finRes.principalRatio)
                        Text(
                            text = "Total Loan Payback: $${MathEngine.formatReal(finRes.totalPayment)}  •  Total Interest: $${MathEngine.formatReal(finRes.totalInterest)}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Tip, Tax & Split Calculator Card
                val rawBill = billAmount.toDoubleOrNull() ?: 0.0
                val tipVal = rawBill * ((tipPercent.toDoubleOrNull() ?: 0.0) / 100.0)
                val taxVal = rawBill * ((taxPercent.toDoubleOrNull() ?: 0.0) / 100.0)
                val grandTotal = rawBill + tipVal + taxVal
                val perPerson = grandTotal / (splitCount.toIntOrNull()?.coerceAtLeast(1) ?: 1)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = palette.cardAccentSoft
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tax, Tip & Bill Split Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(billAmount, { billAmount = it }, Modifier.weight(1f), singleLine = true, label = { Text("Subtotal ($)") })
                            OutlinedTextField(tipPercent, { tipPercent = it }, Modifier.weight(1f), singleLine = true, label = { Text("Tip (%)") })
                            OutlinedTextField(taxPercent, { taxPercent = it }, Modifier.weight(1f), singleLine = true, label = { Text("Tax (%)") })
                            OutlinedTextField(splitCount, { splitCount = it }, Modifier.weight(1f), singleLine = true, label = { Text("Split (N)") })
                        }
                        Text(
                            text = "Total: $${MathEngine.formatReal(grandTotal)}  •  Per Person: $${MathEngine.formatReal(perPerson)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tip: $${MathEngine.formatReal(tipVal)}  •  Tax: $${MathEngine.formatReal(taxVal)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
