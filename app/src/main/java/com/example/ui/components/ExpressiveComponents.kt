package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Material 3 Expressive Scalloped Cookie / Flower Shape.
 * Renders a smooth sinusoidal multi-lobed badge (8, 10, or 12 lobes) inspired by Android 16 / Material You widgets.
 */
class ScallopedCookieShape(
    private val lobes: Int = 12,
    private val amplitudeRatio: Float = 0.085f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseRadius = min(cx, cy) * (1f - amplitudeRatio)
        val amp = min(cx, cy) * amplitudeRatio
        val steps = 180
        for (i in 0..steps) {
            val theta = (2.0 * PI * i) / steps
            val r = baseRadius + amp * cos(lobes * theta).toFloat()
            val x = cx + r * cos(theta).toFloat()
            val y = cy + r * sin(theta).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

enum class CalcKeyShapeStyle {
    CIRCLE_TO_SQUIRCLE,
    SQUIRCLE_TO_PILL,
    SCALLOPED_BADGE,
    SOFT_PILL
}

/**
 * Material 3 Expressive Morphing Calculator Key.
 * Morphs corner radius and scale with a bouncy spring animation on press and triggers haptics.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MorphingCalcButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shapeStyle: CalcKeyShapeStyle = CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE,
    fontSize: TextUnit = 22.sp,
    hapticsEnabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    tag: String = "key_$label",
    accessibilityLabel: String = label
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val targetCorner: Dp = when (shapeStyle) {
        CalcKeyShapeStyle.CIRCLE_TO_SQUIRCLE -> if (isPressed) 14.dp else 36.dp
        CalcKeyShapeStyle.SQUIRCLE_TO_PILL -> if (isPressed) 28.dp else 16.dp
        CalcKeyShapeStyle.SOFT_PILL -> if (isPressed) 10.dp else 24.dp
        CalcKeyShapeStyle.SCALLOPED_BADGE -> 24.dp
    }

    val animatedCorner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyCornerMorph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "keyScaleMorph"
    )

    val buttonShape: Shape = if (shapeStyle == CalcKeyShapeStyle.SCALLOPED_BADGE) {
        remember(isPressed) {
            ScallopedCookieShape(
                lobes = 12,
                amplitudeRatio = if (isPressed) 0.04f else 0.095f
            )
        }
    } else {
        RoundedCornerShape(animatedCorner)
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .sizeIn(minWidth = 44.dp, minHeight = 36.dp)
            .clip(buttonShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                role = Role.Button,
                onClick = {
                    if (hapticsEnabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onClick()
                },
                onLongClick = {
                    if (onLongClick != null) {
                        if (hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLongClick()
                    }
                }
            )
            .semantics { contentDescription = accessibilityLabel }
            .testTag(tag),
        shape = buttonShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!subLabel.isNullOrEmpty()) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = contentColor.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Material 3 Expressive Wavy Progress Indicator inspired by the Leafit and Material You widgets.
 * Draws a sinusoidal wave for the active progress portion and a flat track for the remainder.
 */
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
    waveAmplitude: Dp = 3.5.dp,
    waveLength: Dp = 20.dp,
    strokeWidth: Dp = 3.5.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val cy = size.height / 2f
        val ampPx = waveAmplitude.toPx()
        val waveLenPx = waveLength.toPx().coerceAtLeast(8f)
        val strokePx = strokeWidth.toPx()
        val splitX = size.width * clamped

        // Draw active sinusoidal wave from 0 to splitX
        if (splitX > 2f) {
            val wavePath = Path()
            var x = 0f
            val step = 2f
            while (x <= splitX) {
                val y = cy + ampPx * sin((2.0 * PI * x) / waveLenPx).toFloat()
                if (x == 0f) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                x += step
            }
            drawPath(
                path = wavePath,
                color = activeColor,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Draw remaining flat track
        if (splitX < size.width) {
            drawLine(
                color = trackColor,
                start = Offset(splitX, cy),
                end = Offset(size.width, cy),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }

        // Draw expressive thumb dot at splitX
        drawCircle(
            color = activeColor,
            radius = strokePx * 1.45f,
            center = Offset(splitX, cy)
        )
    }
}

/**
 * Expressive Wavy Divider inspired by the Material You weather & sunset cards.
 */
@Composable
fun ExpressiveWavyDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
    amplitude: Dp = 2.5.dp,
    waveLength: Dp = 18.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val cy = size.height / 2f
        val ampPx = amplitude.toPx()
        val waveLenPx = waveLength.toPx().coerceAtLeast(8f)
        val path = Path()
        var x = 0f
        while (x <= size.width) {
            val y = cy + ampPx * sin((2.0 * PI * x) / waveLenPx).toFloat()
            if (x == 0f) path.moveTo(x, y) else path.lineTo(x, y)
            x += 2f
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ExpressiveStatusChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    tag: String = "chip_$label"
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(tag),
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
        border = if (!active) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
