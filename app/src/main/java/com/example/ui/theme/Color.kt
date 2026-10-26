package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ExpressiveThemePreset(val displayName: String, val tagline: String) {
    SAGE_LEAFIT("Sage Leafit", "Organic cream, forest sage & warm amber"),
    VIOLET_PLAYGROUND("Violet Playground", "Electric lilac, deep plum & coral pop"),
    WARM_TERRACOTTA("Warm Terracotta", "Espresso clay, sand & pistachio lime"),
    DEEP_MINT("Deep Mint & Teal", "Glacier cyan, slate & neon coral")
}

enum class ThemeMode(val label: String) {
    SYSTEM("Auto"),
    LIGHT("Light"),
    DARK("Dark")
}

@Immutable
data class ExpressivePalette(
    val resultPillContainer: Color,
    val resultPillContent: Color,
    val scallopedBadgeContainer: Color,
    val scallopedBadgeContent: Color,
    val operatorClusterSurface: Color,
    val operatorKeyContainer: Color,
    val operatorKeyContent: Color,
    val backspaceKeyContainer: Color,
    val backspaceKeyContent: Color,
    val numberKeyContainer: Color,
    val numberKeyContent: Color,
    val fnKeyContainer: Color,
    val fnKeyContent: Color,
    val cardElevated: Color,
    val cardAccentSoft: Color,
    val graphColors: List<Color>
)

val LocalExpressivePalette = staticCompositionLocalOf {
    SageLeafitLightExpressive
}

// 1. SAGE LEAFIT (Inspired by Leafit & Budget Calculator references)
val SageLeafitLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF1F543F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E8DD),
    onPrimaryContainer = Color(0xFF0B291D),
    secondary = Color(0xFF56635A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6DFEC),
    onSecondaryContainer = Color(0xFF231B2E),
    tertiary = Color(0xFF7D5728),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8DFB3),
    onTertiaryContainer = Color(0xFF2D1A00),
    background = Color(0xFFF5F6EE),
    onBackground = Color(0xFF171D19),
    surface = Color(0xFFF5F6EE),
    onSurface = Color(0xFF171D19),
    surfaceVariant = Color(0xFFE2E8E1),
    onSurfaceVariant = Color(0xFF414943),
    outline = Color(0xFF717972)
)

val SageLeafitDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF8CD6B5),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF1A4332),
    onPrimaryContainer = Color(0xFFBDF2D7),
    secondary = Color(0xFFC9BEFF),
    onSecondary = Color(0xFF2E2252),
    secondaryContainer = Color(0xFF2A362F),
    onSecondaryContainer = Color(0xFFDCE6DF),
    tertiary = Color(0xFFF3D59B),
    onTertiary = Color(0xFF422C05),
    tertiaryContainer = Color(0xFF5A3F12),
    onTertiaryContainer = Color(0xFFFFE4B5),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFE1E6E1),
    surface = Color(0xFF0F1512),
    onSurface = Color(0xFFE1E6E1),
    surfaceVariant = Color(0xFF1C2621),
    onSurfaceVariant = Color(0xFFBFC9C2),
    outline = Color(0xFF89938C)
)

val SageLeafitLightExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFFF7D59C),
    resultPillContent = Color(0xFF7A4B0A),
    scallopedBadgeContainer = Color(0xFF95C949),
    scallopedBadgeContent = Color(0xFF132A00),
    operatorClusterSurface = Color(0xFFFAFBF6),
    operatorKeyContainer = Color(0xFF232722),
    operatorKeyContent = Color(0xFFF6F8F3),
    backspaceKeyContainer = Color(0xFFE6DF95),
    backspaceKeyContent = Color(0xFF2C2A09),
    numberKeyContainer = Color(0xFFECEFE6),
    numberKeyContent = Color(0xFF171D19),
    fnKeyContainer = Color(0xFFDCE6DF),
    fnKeyContent = Color(0xFF1B3A2C),
    cardElevated = Color(0xFFEAEFE6),
    cardAccentSoft = Color(0xFFE8DFF5),
    graphColors = listOf(Color(0xFF1F543F), Color(0xFFD96B27), Color(0xFF7B4B94), Color(0xFF1E7898))
)

val SageLeafitDarkExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFF4B3816),
    resultPillContent = Color(0xFFFFD89B),
    scallopedBadgeContainer = Color(0xFF95C949),
    scallopedBadgeContent = Color(0xFF102400),
    operatorClusterSurface = Color(0xFF18221D),
    operatorKeyContainer = Color(0xFF2B3B33),
    operatorKeyContent = Color(0xFFEAF2EC),
    backspaceKeyContainer = Color(0xFF5E5920),
    backspaceKeyContent = Color(0xFFF7F2B2),
    numberKeyContainer = Color(0xFF1A231E),
    numberKeyContent = Color(0xFFE4EBE6),
    fnKeyContainer = Color(0xFF23322B),
    fnKeyContent = Color(0xFFA8E6C9),
    cardElevated = Color(0xFF17201B),
    cardAccentSoft = Color(0xFF2D263B),
    graphColors = listOf(Color(0xFF8CD6B5), Color(0xFFFFB469), Color(0xFFD4ADFF), Color(0xFF63D8F2))
)

// 2. VIOLET PLAYGROUND (Inspired by Playground & Alarm purple/coral shots)
val VioletPlaygroundLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF673AB7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF7D4E5D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF31101B),
    tertiary = Color(0xFFE56B55),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBD3),
    onTertiaryContainer = Color(0xFF3A0A02),
    background = Color(0xFFFBF6FF),
    onBackground = Color(0xFF1D1A22),
    surface = Color(0xFFFBF6FF),
    onSurface = Color(0xFF1D1A22),
    surfaceVariant = Color(0xFFEEE4F8),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A757F)
)

val VioletPlaygroundDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFFFB2C5),
    onSecondary = Color(0xFF4B2531),
    secondaryContainer = Color(0xFF352146),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = Color(0xFFFF8A75),
    onTertiary = Color(0xFF561E14),
    tertiaryContainer = Color(0xFF733327),
    onTertiaryContainer = Color(0xFFFFDAD4),
    background = Color(0xFF140E1D),
    onBackground = Color(0xFFECE4F6),
    surface = Color(0xFF140E1D),
    onSurface = Color(0xFFECE4F6),
    surfaceVariant = Color(0xFF251A34),
    onSurfaceVariant = Color(0xFFCEC3DC),
    outline = Color(0xFF978EA5)
)

val VioletPlaygroundLightExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFFC49BFF),
    resultPillContent = Color(0xFF22074A),
    scallopedBadgeContainer = Color(0xFFFF7D6B),
    scallopedBadgeContent = Color(0xFF2B0600),
    operatorClusterSurface = Color(0xFFF4ECFF),
    operatorKeyContainer = Color(0xFF2A1447),
    operatorKeyContent = Color(0xFFF8F1FF),
    backspaceKeyContainer = Color(0xFFFFC1B8),
    backspaceKeyContent = Color(0xFF3E0A04),
    numberKeyContainer = Color(0xFFF1E8FC),
    numberKeyContent = Color(0xFF22182E),
    fnKeyContainer = Color(0xFFE3D0FF),
    fnKeyContent = Color(0xFF32125E),
    cardElevated = Color(0xFFF2E9FE),
    cardAccentSoft = Color(0xFFFFE2DC),
    graphColors = listOf(Color(0xFF7B3FE4), Color(0xFFFF6E59), Color(0xFF009688), Color(0xFFE91E63))
)

val VioletPlaygroundDarkExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFF533483),
    resultPillContent = Color(0xFFEADAFF),
    scallopedBadgeContainer = Color(0xFFFF8573),
    scallopedBadgeContent = Color(0xFF2D0702),
    operatorClusterSurface = Color(0xFF221730),
    operatorKeyContainer = Color(0xFF3C2859),
    operatorKeyContent = Color(0xFFF4EBFF),
    backspaceKeyContainer = Color(0xFF6B2D28),
    backspaceKeyContent = Color(0xFFFFDAD5),
    numberKeyContainer = Color(0xFF1F162B),
    numberKeyContent = Color(0xFFEDE4F7),
    fnKeyContainer = Color(0xFF2E2040),
    fnKeyContent = Color(0xFFD8BFFF),
    cardElevated = Color(0xFF1C1327),
    cardAccentSoft = Color(0xFF361F2D),
    graphColors = listOf(Color(0xFFC9A7FF), Color(0xFFFF8A75), Color(0xFF5EEAD4), Color(0xFFF472B6))
)

// 3. WARM TERRACOTTA (Inspired by Material You Coral/Espresso widget showcase)
val WarmTerracottaLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFFB4462C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBD2),
    onPrimaryContainer = Color(0xFF3B0900),
    secondary = Color(0xFF6E5C3E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7DFBB),
    onSecondaryContainer = Color(0xFF261904),
    tertiary = Color(0xFF586431),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCEAA9),
    onTertiaryContainer = Color(0xFF171E00),
    background = Color(0xFFFCF7F1),
    onBackground = Color(0xFF221A15),
    surface = Color(0xFFFCF7F1),
    onSurface = Color(0xFF221A15),
    surfaceVariant = Color(0xFFF2E5DA),
    onSurfaceVariant = Color(0xFF52443C),
    outline = Color(0xFF85736B)
)

val WarmTerracottaDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4A3),
    onPrimary = Color(0xFF621200),
    primaryContainer = Color(0xFF7D2B16),
    onPrimaryContainer = Color(0xFFFFDBD2),
    secondary = Color(0xFFDCC3A0),
    onSecondary = Color(0xFF3D2E15),
    secondaryContainer = Color(0xFF422C21),
    onSecondaryContainer = Color(0xFFF8DFD2),
    tertiary = Color(0xFFC0CE8F),
    onTertiary = Color(0xFF2B3407),
    tertiaryContainer = Color(0xFF414B1C),
    onTertiaryContainer = Color(0xFFDCEAA9),
    background = Color(0xFF19120E),
    onBackground = Color(0xFFF0DFD6),
    surface = Color(0xFF19120E),
    onSurface = Color(0xFFF0DFD6),
    surfaceVariant = Color(0xFF2E2019),
    onSurfaceVariant = Color(0xFFD7C2B8),
    outline = Color(0xFFA08D84)
)

val WarmTerracottaLightExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFFFFCBAE),
    resultPillContent = Color(0xFF4E1B08),
    scallopedBadgeContainer = Color(0xFFB5C468),
    scallopedBadgeContent = Color(0xFF212900),
    operatorClusterSurface = Color(0xFFF7ECE3),
    operatorKeyContainer = Color(0xFF3E2418),
    operatorKeyContent = Color(0xFFFFEDE5),
    backspaceKeyContainer = Color(0xFFE9957C),
    backspaceKeyContent = Color(0xFF360B00),
    numberKeyContainer = Color(0xFFF4E7DC),
    numberKeyContent = Color(0xFF2B1B13),
    fnKeyContainer = Color(0xFFE5D5B5),
    fnKeyContent = Color(0xFF3B2C11),
    cardElevated = Color(0xFFF5EAE0),
    cardAccentSoft = Color(0xFFE2E8C5),
    graphColors = listOf(Color(0xFFD65437), Color(0xFF6A8D27), Color(0xFF2B6CB0), Color(0xFFB7791F))
)

val WarmTerracottaDarkExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFF5D2B18),
    resultPillContent = Color(0xFFFFCEB8),
    scallopedBadgeContainer = Color(0xFFB5C468),
    scallopedBadgeContent = Color(0xFF1D2400),
    operatorClusterSurface = Color(0xFF281B14),
    operatorKeyContainer = Color(0xFF492F22),
    operatorKeyContent = Color(0xFFFFE9DF),
    backspaceKeyContainer = Color(0xFF7D3320),
    backspaceKeyContent = Color(0xFFFFDAD2),
    numberKeyContainer = Color(0xFF241812),
    numberKeyContent = Color(0xFFF2E0D7),
    fnKeyContainer = Color(0xFF38291E),
    fnKeyContent = Color(0xFFF3D59B),
    cardElevated = Color(0xFF221610),
    cardAccentSoft = Color(0xFF2F341B),
    graphColors = listOf(Color(0xFFFF8F73), Color(0xFFC4DA72), Color(0xFF63B3ED), Color(0xFFF6AD55))
)

// 4. DEEP MINT & TEAL (Inspired by Teal/Cyan Material You widget showcase)
val DeepMintLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4A6267),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE7EC),
    onSecondaryContainer = Color(0xFF051F23),
    tertiary = Color(0xFF9C4058),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E0),
    onTertiaryContainer = Color(0xFF3F0016),
    background = Color(0xFFF0FAF9),
    onBackground = Color(0xFF121E20),
    surface = Color(0xFFF0FAF9),
    onSurface = Color(0xFF121E20),
    surfaceVariant = Color(0xFFD9ECEB),
    onSurfaceVariant = Color(0xFF3F484A),
    outline = Color(0xFF6F797A)
)

val DeepMintDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF66E3F4),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFFB1CBD0),
    onSecondary = Color(0xFF1C3438),
    secondaryContainer = Color(0xFF1B353B),
    onSecondaryContainer = Color(0xFFCDE7EC),
    tertiary = Color(0xFFFFB1C3),
    onTertiary = Color(0xFF5F112B),
    tertiaryContainer = Color(0xFF7D2941),
    onTertiaryContainer = Color(0xFFFFD9E0),
    background = Color(0xFF0B171A),
    onBackground = Color(0xFFDCE6E8),
    surface = Color(0xFF0B171A),
    onSurface = Color(0xFFDCE6E8),
    surfaceVariant = Color(0xFF15292E),
    onSurfaceVariant = Color(0xFFBFC8CA),
    outline = Color(0xFF899294)
)

val DeepMintLightExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFF84F2EE),
    resultPillContent = Color(0xFF003836),
    scallopedBadgeContainer = Color(0xFFFF6E91),
    scallopedBadgeContent = Color(0xFF33000E),
    operatorClusterSurface = Color(0xFFE3F5F4),
    operatorKeyContainer = Color(0xFF102E34),
    operatorKeyContent = Color(0xFFE3FAFC),
    backspaceKeyContainer = Color(0xFFB8C4FF),
    backspaceKeyContent = Color(0xFF121D52),
    numberKeyContainer = Color(0xFFE2F2F1),
    numberKeyContent = Color(0xFF102427),
    fnKeyContainer = Color(0xFFC6EAE8),
    fnKeyContent = Color(0xFF06383A),
    cardElevated = Color(0xFFE4F4F3),
    cardAccentSoft = Color(0xFFFFE0E8),
    graphColors = listOf(Color(0xFF00838F), Color(0xFFE91E63), Color(0xFF5E35B1), Color(0xFF2E7D32))
)

val DeepMintDarkExpressive = ExpressivePalette(
    resultPillContainer = Color(0xFF12464D),
    resultPillContent = Color(0xFF7AF2FF),
    scallopedBadgeContainer = Color(0xFFFF6E91),
    scallopedBadgeContent = Color(0xFF2B000B),
    operatorClusterSurface = Color(0xFF13262B),
    operatorKeyContainer = Color(0xFF1E3E46),
    operatorKeyContent = Color(0xFF9CF4FF),
    backspaceKeyContainer = Color(0xFF393E6E),
    backspaceKeyContent = Color(0xFFDCE1FF),
    numberKeyContainer = Color(0xFF122327),
    numberKeyContent = Color(0xFFDCECEE),
    fnKeyContainer = Color(0xFF193339),
    fnKeyContent = Color(0xFF80EAFF),
    cardElevated = Color(0xFF102024),
    cardAccentSoft = Color(0xFF381A26),
    graphColors = listOf(Color(0xFF66E3F4), Color(0xFFFF7597), Color(0xFFB39DDB), Color(0xFF81C784))
)
