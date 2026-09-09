package com.example.android.xo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MidnightBlue = Color(0xFF172554)
private val NeonTeal = Color(0xFF2DD4BF)
private val NeonTealDark = Color(0xFF5EEAD4)
private val Coral = Color(0xFFFF6B6B)
private val CoralDark = Color(0xFFFF8A8A)
private val Gold = Color(0xFFF6B73C)
private val GoldDark = Color(0xFFFFD166)
private val LightBackground = Color(0xFFF6F8FC)
private val DarkBackground = Color(0xFF0B132B)
private val LightSurface = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF141E35)
private val LightText = Color(0xFF111827)
private val DarkText = Color(0xFFF8FAFC)
private val LightSecondaryText = Color(0xFF64748B)
private val DarkSecondaryText = Color(0xFFA8B3C7)

val XoLightColors = lightColorScheme(
    primary = MidnightBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5EBF8),
    onPrimaryContainer = MidnightBlue,
    secondary = NeonTeal,
    onSecondary = Color(0xFF073B36),
    secondaryContainer = Color(0xFFD2F8F1),
    onSecondaryContainer = Color(0xFF073B36),
    tertiary = Gold,
    onTertiary = Color(0xFF3D2800),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFE9EEF6),
    onSurfaceVariant = LightSecondaryText,
    error = Coral,
    onError = Color.White
)

val XoDarkColors = darkColorScheme(
    primary = NeonTealDark,
    onPrimary = Color(0xFF073B36),
    primaryContainer = Color(0xFF164B50),
    onPrimaryContainer = Color(0xFFB8FFF4),
    secondary = NeonTealDark,
    onSecondary = Color(0xFF073B36),
    secondaryContainer = Color(0xFF164B50),
    onSecondaryContainer = Color(0xFFB8FFF4),
    tertiary = GoldDark,
    onTertiary = Color(0xFF3D2800),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF202D49),
    onSurfaceVariant = DarkSecondaryText,
    error = CoralDark,
    onError = Color(0xFF4A1010)
)

val XoTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

val XoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun XoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> XoDarkColors
        else -> XoLightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = XoTypography,
        shapes = XoShapes,
        content = content
    )
}

object XoGameColors {
    val x = Coral
    val o = NeonTeal
    val draw = Gold
    val board = Color(0xFF121F3D)
    val boardCell = Color(0xFF1D3157)
    val boardGrid = Color(0xFF2C4770)
    val winCell = Color(0xFFFFF4D6)
    val winCellDark = Color(0xFF4A381B)
}
