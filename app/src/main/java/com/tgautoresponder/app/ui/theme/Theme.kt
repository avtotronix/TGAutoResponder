package com.tgautoresponder.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PinkBlackColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = TextWhite,
    primaryContainer = PinkDark,
    onPrimaryContainer = TextWhite,
    secondary = HotPink,
    onSecondary = TextWhite,
    background = BgBlack,
    onBackground = TextWhite,
    surface = SurfaceBlack,
    onSurface = TextWhite,
    surfaceVariant = CardBlack,
    onSurfaceVariant = TextPinkMuted,
    outline = BorderPink
)

@Composable
fun TGAutoResponderTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = PinkBlackColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgBlack.toArgb()
            window.navigationBarColor = BgBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
