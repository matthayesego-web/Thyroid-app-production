package com.thyroidtracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EchoIndigo = Color(0xFF4A3A78)
private val EchoIndigoDark = Color(0xFFD0BCFF)
private val EchoTeal = Color(0xFF2D7F79)
private val EchoTealDark = Color(0xFF89D8D0)
private val EchoLavender = Color(0xFF755C9E)

private val LightColors = lightColorScheme(
    primary = EchoIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAE2F8),
    onPrimaryContainer = Color(0xFF271B45),
    secondary = EchoTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6F2EE),
    onSecondaryContainer = Color(0xFF0E3532),
    tertiary = EchoLavender,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEBDDF7),
    onTertiaryContainer = Color(0xFF2E2040),
    background = Color(0xFFFAF9FC),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFAF9FC),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFE8E4EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A757F),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColors = darkColorScheme(
    primary = EchoIndigoDark,
    onPrimary = Color(0xFF35235F),
    primaryContainer = Color(0xFF4B3A75),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = EchoTealDark,
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF17504B),
    onSecondaryContainer = Color(0xFFA5F2E9),
    tertiary = Color(0xFFD7BAF4),
    onTertiary = Color(0xFF3B2750),
    tertiaryContainer = Color(0xFF523D67),
    onTertiaryContainer = Color(0xFFF0DBFF),
    background = Color(0xFF121116),
    onBackground = Color(0xFFE7E1E9),
    surface = Color(0xFF121116),
    onSurface = Color(0xFFE7E1E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF948F99),
    outlineVariant = Color(0xFF49454F)
)

private val EchoTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 31.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

private val EchoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ThyroidTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = EchoTypography,
        shapes = EchoShapes,
        content = content
    )
}
