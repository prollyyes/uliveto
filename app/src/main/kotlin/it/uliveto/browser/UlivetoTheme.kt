package it.uliveto.browser

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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

// Placeholder dark scheme — full dark palette arrives in a later milestone.
private val TerracottaDarkColorScheme = darkColorScheme(
    primary = Terracotta1,
    onPrimary = WarmCream,
    background = CharcoalDark,
    onBackground = WarmCream,
    surface = CharcoalDark,
    onSurface = WarmCream,
)

@Composable
fun UlivetoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) TerracottaDarkColorScheme else TerracottaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UlivetoTypography,
        content = content,
    )
}
