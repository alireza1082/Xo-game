package com.example.android.xo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape

private val Navy = Color(0xFF18243D)
private val NavyDark = Color(0xFF111A2E)
private val Teal = Color(0xFF55C2B3)
private val TealDark = Color(0xFF65D1C1)
private val Coral = Color(0xFFF26B5E)
private val CoralDark = Color(0xFFFF867A)
private val Gold = Color(0xFFF2B84B)
private val GoldDark = Color(0xFFF5C15D)
private val LightBackground = Color(0xFFF5F7FB)
private val DarkBackground = Color(0xFF0D1424)
private val LightSurface = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF17233A)
private val LightText = Color(0xFF172033)
private val DarkText = Color(0xFFF4F7FC)
private val LightSecondaryText = Color(0xFF71809A)
private val DarkSecondaryText = Color(0xFFAAB7CC)

val XoLightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7F5),
    onPrimaryContainer = Navy,
    secondary = Teal,
    onSecondary = Navy,
    secondaryContainer = Color(0xFFD3F2EC),
    onSecondaryContainer = Color(0xFF0D3A35),
    tertiary = Gold,
    onTertiary = Navy,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFE8ECF3),
    onSurfaceVariant = LightSecondaryText,
    error = Coral,
    onError = Color.White
)

val XoDarkColors = darkColorScheme(
    primary = TealDark,
    onPrimary = NavyDark,
    primaryContainer = Color(0xFF28465A),
    onPrimaryContainer = Color(0xFFD7F4FF),
    secondary = TealDark,
    onSecondary = NavyDark,
    secondaryContainer = Color(0xFF1F4C48),
    onSecondaryContainer = Color(0xFFB5F4EA),
    tertiary = GoldDark,
    onTertiary = NavyDark,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF28364D),
    onSurfaceVariant = DarkSecondaryText,
    error = CoralDark,
    onError = NavyDark
)

val XoTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
)

val XoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
    val o = Teal
    val draw = Gold
    val board = Navy
    val boardCell = Color(0xFF263653)
    val boardGrid = Color(0xFF344563)
    val winCell = Color(0xFFFFF1D6)
    val winCellDark = Color(0xFF4D3B1E)
}
