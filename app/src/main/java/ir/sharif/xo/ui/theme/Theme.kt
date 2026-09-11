package ir.sharif.xo.ui.theme

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

private val MidnightBlue = Color(0xFF102A56)
private val NeonTeal = Color(0xFF0F766E)
private val NeonTealDark = Color(0xFF5EEAD4)
private val Coral = Color(0xFFB42318)
private val CoralDark = Color(0xFFFF8A80)
private val Gold = Color(0xFF946200)
private val GoldDark = Color(0xFFFFD166)
private val LightBackground = Color(0xFFF8FAFC)
private val DarkBackground = Color(0xFF020617)
private val LightSurface = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF0B1220)
private val LightText = Color(0xFF0F172A)
private val DarkText = Color(0xFFF8FAFC)
private val LightSecondaryText = Color(0xFF475569)
private val DarkSecondaryText = Color(0xFFCBD5E1)

val XoLightColors = lightColorScheme(
    primary = MidnightBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5ECFA),
    onPrimaryContainer = Color(0xFF0B1F43),
    secondary = NeonTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F5F0),
    onSecondaryContainer = Color(0xFF064E49),
    tertiary = Gold,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFE8EEF6),
    onSurfaceVariant = LightSecondaryText,
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFFCBD5E1),
    error = Coral,
    onError = Color.White
)

val XoDarkColors = darkColorScheme(
    primary = NeonTealDark,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFA7FFF2),
    secondary = NeonTealDark,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFA7FFF2),
    tertiary = GoldDark,
    onTertiary = Color(0xFF332000),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF17233A),
    onSurfaceVariant = DarkSecondaryText,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFF475569),
    error = CoralDark,
    onError = Color(0xFF3B0909)
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
    // These darker accents remain readable on light surfaces.
    val x = Coral
    val o = NeonTeal
    val draw = Gold

    // These brighter variants are reserved for the dark game board and dark result badge.
    val xOnBoard = CoralDark
    val oOnBoard = NeonTealDark
    val drawOnDark = GoldDark
    val winLine = Color(0xFF6D28D9)
    val winLineOnBoard = Color(0xFFC4B5FD)
    val board = Color(0xFF0B1733)
    val boardCell = Color(0xFF172A4D)
    val boardGrid = Color(0xFF304A73)
    val winCell = Color(0xFFFFF4D6)
    val winCellDark = Color(0xFF4A381B)
}
