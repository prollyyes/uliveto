package it.uliveto.browser.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.NeutralLight
import it.uliveto.browser.ui.tokens.OliveGreen
import it.uliveto.browser.ui.tokens.Terracotta1
import it.uliveto.browser.ui.tokens.Terracotta2
import it.uliveto.browser.ui.tokens.Terracotta3
import it.uliveto.browser.ui.tokens.UlivetoTypography
import it.uliveto.browser.ui.tokens.WarmCream

private val TerracottaLightColorScheme = lightColorScheme(
    primary = Terracotta1,
    onPrimary = WarmCream,
    primaryContainer = Terracotta3,
    onPrimaryContainer = WarmCream,
    secondary = OliveGreen,
    onSecondary = WarmCream,
    background = NeutralLight,
    onBackground = CharcoalDark,
    surface = WarmCream,
    onSurface = CharcoalDark,
    surfaceVariant = WarmCream,
    onSurfaceVariant = Terracotta2,
)

private val TerracottaDarkColorScheme = darkColorScheme(
    primary = Terracotta1,
    onPrimary = WarmCream,
    primaryContainer = Terracotta3,
    onPrimaryContainer = WarmCream,
    secondary = OliveGreen,
    onSecondary = WarmCream,
    background = Color(0xFF1A0E08),
    onBackground = WarmCream,
    surface = Color(0xFF2A1A0E),
    onSurface = WarmCream,
    surfaceVariant = Color(0xFF2A1A0E),
    onSurfaceVariant = Terracotta2,
)

// TODO: OLED Black — functional glass overlay should use Color(0xFF1F1F22).copy(alpha = 0.48f)
private val TerracottaOledColorScheme = darkColorScheme(
    primary = Terracotta1,
    onPrimary = WarmCream,
    primaryContainer = Terracotta3,
    onPrimaryContainer = WarmCream,
    secondary = OliveGreen,
    onSecondary = WarmCream,
    background = Color.Black,
    onBackground = WarmCream,
    surface = Color.Black,
    onSurface = WarmCream,
    surfaceVariant = Color.Black,
    onSurfaceVariant = Terracotta2,
)

@Composable
fun UlivetoTheme(
    appTheme: AppTheme = AppTheme.Light,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.Light -> TerracottaLightColorScheme
        AppTheme.Dark -> TerracottaDarkColorScheme
        AppTheme.OledBlack -> TerracottaOledColorScheme
        AppTheme.FollowSystem -> if (isSystemInDarkTheme()) TerracottaDarkColorScheme else TerracottaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UlivetoTypography,
        content = content,
    )
}
