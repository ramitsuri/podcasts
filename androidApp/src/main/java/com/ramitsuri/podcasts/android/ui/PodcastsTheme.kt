package com.ramitsuri.podcasts.android.ui

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

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors =
        when {
            dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
            dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
            darkTheme ->
                darkColorScheme(
                    primary = primaryDark,
                    secondary = secondaryDark,
                    tertiary = tertiaryDark,
                )
            else ->
                lightColorScheme(
                    primary = primaryLight,
                    secondary = secondaryLight,
                    tertiary = tertiaryLight,
                )
        }
    val typography =
        Typography(
            bodyMedium =
                TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
        )
    val shapes =
        Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
        )

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}

private val primaryDark = Color(0xFFBB86FC)
private val secondaryDark = Color(0xFF03DAC5)
private val tertiaryDark = Color(0xFF3700B3)
private val greenDark = Color(0xFF64a880)

private val primaryLight = Color(0xFF6200EE)
private val secondaryLight = Color(0xFF03DAC5)
private val tertiaryLight = Color(0xFF3700B3)
private val greenLight = Color(0xFF578a4f)

val greenColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) greenDark else greenLight
