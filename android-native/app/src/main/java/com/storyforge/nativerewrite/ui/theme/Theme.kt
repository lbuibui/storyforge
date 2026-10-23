package com.storyforge.nativerewrite.ui.theme

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storyforge.nativerewrite.ui.ExpressiveShapeScale

private val Teal = Color(0xFF1F6F64)
private val TealDark = Color(0xFF8FDBCF)
private val Paper = Color(0xFFF7F1E8)
private val Ink = Color(0xFF1C1B19)
private val Clay = Color(0xFF8C5A3C)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = Clay,
    background = Paper,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = TealDark,
    secondary = Color(0xFFE2B08A),
    background = Color(0xFF121412),
    surface = Color(0xFF1C1F1C),
)

private val StoryShapes = Shapes(
    extraSmall = RoundedCornerShape(ExpressiveShapeScale.EXTRA_SMALL_DP.dp),
    small = RoundedCornerShape(ExpressiveShapeScale.SMALL_DP.dp),
    medium = RoundedCornerShape(ExpressiveShapeScale.MEDIUM_DP.dp),
    large = RoundedCornerShape(ExpressiveShapeScale.LARGE_DP.dp),
    extraLarge = RoundedCornerShape(ExpressiveShapeScale.EXTRA_LARGE_DP.dp),
)

private val StoryTypography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.merge(
            TextStyle(fontSize = 18.sp, lineHeight = 30.sp),
        ),
    )
}

@Composable
fun StoryForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = StoryTypography,
        shapes = StoryShapes,
        content = content,
    )
}
