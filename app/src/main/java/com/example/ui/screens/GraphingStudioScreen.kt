package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.AngleUnit
import com.example.domain.MathEngine
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.components.ExpressiveStatusChip
import com.example.ui.components.ExpressiveWavyDivider
import com.example.ui.theme.LocalExpressivePalette
import kotlin.math.abs

@Composable
fun GraphingStudioScreen(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalExpressivePalette.current
    val primaryFn = uiState.graphFunctions.firstOrNull { it.enabled } ?: uiState.graphFunctions.first()

    // Live analysis at trace point x = uiState.graphTraceX
    val traceX = uiState.graphTraceX.toDouble()
    val traceY = remember(primaryFn.expression, traceX) {
        MathEngine.evaluateRealFunction(primaryFn.expression, traceX, AngleUnit.RAD)
    }
    val slopeAtTrace = remember(primaryFn.expression, traceX) {
        MathEngine.numericalDerivative(primaryFn.expression, traceX, AngleUnit.RAD)
    }
    val definiteIntegralArea = remember(primaryFn.expression, uiState.shadeLowerX, uiState.shadeUpperX) {
        MathEngine.numericalIntegral(
            primaryFn.expression,
            uiState.shadeLowerX.toDouble(),
            uiState.shadeUpperX.toDouble(),
            steps = 120,
            angleUnit = AngleUnit.RAD
        )
    }

    // Automatic Root & Local Extrema scan in current viewport
    val detectedFeatures = remember(primaryFn.expression, uiState.graphXMin, uiState.graphXMax) {
        scanCurveFeatures(primaryFn.expression, uiState.graphXMin.toDouble(), uiState.graphXMax.toDouble())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. INTERACTIVE 2D MULTI-FUNCTION CANVAS CARD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .testTag("graph_canvas_card"),
            shape = RoundedCornerShape(28.dp),
            color = palette.cardElevated,
            tonalElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val curveColors = palette.graphColors
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                val shadeColor = curveColors[0].copy(alpha = 0.22f)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.graphXMin, uiState.graphXMax, uiState.graphYMin, uiState.graphYMax) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (zoom != 1f) {
                                    viewModel.zoomGraph(1f / zoom.coerceIn(0.5f, 2f))
                                }
                                if (pan != Offset.Zero) {
                                    val xSpan = uiState.graphXMax - uiState.graphXMin
                                    val ySpan = uiState.graphYMax - uiState.graphYMin
                                    val dx = -(pan.x / size.width) * xSpan
                                    val dy = (pan.y / size.height) * ySpan
                                    viewModel.panGraph(dx, dy)
                                }
                            }
                        }
                        .pointerInput(uiState.graphXMin, uiState.graphXMax) {
                            detectTapGestures { offset ->
                                val xSpan = uiState.graphXMax - uiState.graphXMin
                                val tappedX = uiState.graphXMin + (offset.x / size.width) * xSpan
                                viewModel.setGraphTraceX(tappedX)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val xMin = uiState.graphXMin
                    val xMax = uiState.graphXMax
                    val yMin = uiState.graphYMin
                    val yMax = uiState.graphYMax

                    fun toScreenX(x: Float): Float = ((x - xMin) / (xMax - xMin)) * w
                    fun toScreenY(y: Float): Float = h - ((y - yMin) / (yMax - yMin)) * h

                    // Draw coordinate grid lines
                    val xStep = ((xMax - xMin) / 8f).coerceAtLeast(0.5f)
                    var gx = kotlin.math.floor(xMin / xStep) * xStep
                    while (gx <= xMax) {
                        val sx = toScreenX(gx)
                        drawLine(gridColor, Offset(sx, 0f), Offset(sx, h), strokeWidth = 1.dp.toPx())
                        gx += xStep
                    }
                    val yStep = ((yMax - yMin) / 6f).coerceAtLeast(0.5f)
                    var gy = kotlin.math.floor(yMin / yStep) * yStep
                    while (gy <= yMax) {
                        val sy = toScreenY(gy)
                        drawLine(gridColor, Offset(0f, sy), Offset(w, sy), strokeWidth = 1.dp.toPx())
                        gy += yStep
                    }

                    // Draw X=0 and Y=0 axes
                    if (0f in xMin..xMax) {
                        val sx0 = toScreenX(0f)
                        drawLine(axisColor, Offset(sx0, 0f), Offset(sx0, h), strokeWidth = 2.dp.toPx())
                    }
                    if (0f in yMin..yMax) {
                        val sy0 = toScreenY(0f)
                        drawLine(axisColor, Offset(0f, sy0), Offset(w, sy0), strokeWidth = 2.dp.toPx())
                    }

                    // Draw Shaded Definite Integral Area for primary function
                    if (uiState.showIntegralShade) {
                        val shadePath = Path()
                        val a = uiState.shadeLowerX.coerceIn(xMin, xMax)
                        val b = uiState.shadeUpperX.coerceIn(xMin, xMax)
                        val yZero = toScreenY(0f).coerceIn(0f, h)
                        shadePath.moveTo(toScreenX(a), yZero)
                        val shadeSteps = 80
                        for (i in 0..shadeSteps) {
                            val xVal = a + (b - a) * (i.toFloat() / shadeSteps)
                            val yVal = MathEngine.evaluateRealFunction(primaryFn.expression, xVal.toDouble(), AngleUnit.RAD).toFloat()
                            if (yVal.isFinite()) {
                                shadePath.lineTo(toScreenX(xVal), toScreenY(yVal).coerceIn(0f, h))
                            }
                        }
                        shadePath.lineTo(toScreenX(b), yZero)
                        shadePath.close()
                        drawPath(shadePath, color = shadeColor)
                    }

                    // Plot each enabled function f_1(x)..f_4(x)
                    uiState.graphFunctions.forEachIndexed { idx, fnItem ->
                        if (!fnItem.enabled || fnItem.expression.isBlank()) return@forEachIndexed
                        val color = curveColors[idx % curveColors.size]
                        val path = Path()
                        var penDown = false
                        val samples = 240
                        for (i in 0..samples) {
                            val xVal = xMin + (xMax - xMin) * (i.toFloat() / samples)
                            val yVal = MathEngine.evaluateRealFunction(fnItem.expression, xVal.toDouble(), AngleUnit.RAD).toFloat()
                            if (yVal.isFinite() && yVal in (yMin - 20f)..(yMax + 20f)) {
                                val sx = toScreenX(xVal)
                                val sy = toScreenY(yVal)
                                if (!penDown) {
                                    path.moveTo(sx, sy)
                                    penDown = true
                                } else {
                                    path.lineTo(sx, sy)
                                }
                            } else {
                                penDown = false
                            }
                        }
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw Tangent Line & Trace Crosshair on primary function
                    if (traceY.isFinite()) {
                        val tx = toScreenX(uiState.graphTraceX)
                        val ty = toScreenY(traceY.toFloat())

                        if (uiState.showTangentLine && slopeAtTrace.isFinite()) {
                            val dx = (xMax - xMin) * 0.25f
                            val x1 = uiState.graphTraceX - dx
                            val y1 = traceY.toFloat() - slopeAtTrace.toFloat() * dx
                            val x2 = uiState.graphTraceX + dx
                            val y2 = traceY.toFloat() + slopeAtTrace.toFloat() * dx
                            drawLine(
                                color = curveColors[1],
                                start = Offset(toScreenX(x1), toScreenY(y1)),
                                end = Offset(toScreenX(x2), toScreenY(y2)),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        drawCircle(
                            color = curveColors[0],
                            radius = 7.dp.toPx(),
                            center = Offset(tx, ty)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(tx, ty)
                        )
                    }
                }

                // Floating Viewport Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { viewModel.zoomGraph(0.75f) },
                        modifier = Modifier.size(48.dp).testTag("graph_zoom_in")
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In Graph")
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.zoomGraph(1.35f) },
                        modifier = Modifier.size(48.dp).testTag("graph_zoom_out")
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out Graph")
                    }
                    FilledTonalIconButton(
                        onClick = viewModel::resetGraphViewport,
                        modifier = Modifier.size(48.dp).testTag("graph_reset_view")
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset Graph Viewport")
                    }
                }

                // Bottom-Left Live Coordinate & Calculus Telemetry Pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = "Trace x = ${MathEngine.formatReal(traceX)}  →  ${primaryFn.label} = ${MathEngine.formatReal(traceY)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Slope f′(x) = ${MathEngine.formatReal(slopeAtTrace)}  •  ∫[${uiState.shadeLowerX}, ${uiState.shadeUpperX}] = ${MathEngine.formatReal(definiteIntegralArea)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. INTERACTIVE TRACE SLIDER & GRAPH OVERLAY TOGGLES
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crosshair Trace X (${MathEngine.formatReal(traceX)})",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExpressiveStatusChip(
                            label = "Tangent Line",
                            active = uiState.showTangentLine,
                            onClick = viewModel::toggleTangentLine
                        )
                        ExpressiveStatusChip(
                            label = "∫ Area Shade",
                            active = uiState.showIntegralShade,
                            onClick = viewModel::toggleIntegralShade
                        )
                        ExpressiveStatusChip(
                            label = if (uiState.showGraphTable) "Hide Table" else "x/y Table",
                            active = uiState.showGraphTable,
                            onClick = viewModel::toggleGraphTable,
                            tag = "toggle_graph_table_chip"
                        )
                    }
                }

                Slider(
                    value = uiState.graphTraceX.coerceIn(uiState.graphXMin, uiState.graphXMax),
                    onValueChange = viewModel::setGraphTraceX,
                    valueRange = uiState.graphXMin..uiState.graphXMax,
                    modifier = Modifier.testTag("graph_trace_slider")
                )

                Text(
                    text = detectedFeatures,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // 3. DYNAMIC (x, f₁(x), f₂(x)) TABLE GENERATOR
        if (uiState.showGraphTable) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = palette.cardAccentSoft
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Text(
                            text = "Function Value Table (x from -4 to +4)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    ExpressiveWavyDivider()
                    for (stepIdx in -4..4) {
                        val xVal = stepIdx.toDouble()
                        val y1 = MathEngine.evaluateRealFunction(uiState.graphFunctions[0].expression, xVal, AngleUnit.RAD)
                        val y2 = MathEngine.evaluateRealFunction(uiState.graphFunctions[1].expression, xVal, AngleUnit.RAD)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("x = ${MathEngine.formatReal(xVal)}", style = MaterialTheme.typography.labelMedium)
                            Text("f₁ = ${MathEngine.formatReal(y1)}", style = MaterialTheme.typography.labelMedium)
                            Text("f₂ = ${MathEngine.formatReal(y2)}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 4. MULTI-FUNCTION EDITOR CARDS (f₁(x)..f₄(x))
        Text(
            text = "Simultaneous Function Curves",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        uiState.graphFunctions.forEachIndexed { idx, item ->
            val badgeColor = palette.graphColors[idx % palette.graphColors.size]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    OutlinedTextField(
                        value = item.expression,
                        onValueChange = { viewModel.updateGraphFunction(item.id, it, item.enabled) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("graph_fn_input_${item.id}"),
                        singleLine = true,
                        label = { Text("Expression in x") }
                    )
                    Switch(
                        checked = item.enabled,
                        onCheckedChange = { viewModel.updateGraphFunction(item.id, item.expression, it) },
                        modifier = Modifier.testTag("graph_fn_switch_${item.id}")
                    )
                }
            }
        }
    }
}

private fun scanCurveFeatures(expr: String, xMin: Double, xMax: Double): String {
    val roots = mutableListOf<Double>()
    val extrema = mutableListOf<String>()
    val steps = 160
    val dx = (xMax - xMin) / steps
    var prevX = xMin
    var prevY = MathEngine.evaluateRealFunction(expr, prevX, AngleUnit.RAD)
    var prevSlope = MathEngine.numericalDerivative(expr, prevX, AngleUnit.RAD)

    for (i in 1..steps) {
        val curX = xMin + i * dx
        val curY = MathEngine.evaluateRealFunction(expr, curX, AngleUnit.RAD)
        val curSlope = MathEngine.numericalDerivative(expr, curX, AngleUnit.RAD)

        if (prevY.isFinite() && curY.isFinite() && prevY * curY <= 0.0 && abs(curY - prevY) < 20.0) {
            val rootApprox = prevX - prevY * (curX - prevX) / (curY - prevY)
            if (roots.none { abs(it - rootApprox) < dx * 1.5 }) {
                roots.add(rootApprox)
            }
        }
        if (prevSlope.isFinite() && curSlope.isFinite() && prevSlope * curSlope < 0.0 && abs(curSlope - prevSlope) < 30.0) {
            val extX = (prevX + curX) / 2.0
            val kind = if (prevSlope > 0 && curSlope < 0) "Max" else "Min"
            if (extrema.size < 3) {
                extrema.add("$kind≈${MathEngine.formatReal(extX)}")
            }
        }
        prevX = curX
        prevY = curY
        prevSlope = curSlope
    }
    val rootText = if (roots.isEmpty()) "No real roots in view" else "Roots x ≈ ${roots.take(4).joinToString(", ") { MathEngine.formatReal(it) }}"
    val extText = if (extrema.isEmpty()) "" else "  •  Extrema: ${extrema.joinToString(", ")}"
    return rootText + extText
}
