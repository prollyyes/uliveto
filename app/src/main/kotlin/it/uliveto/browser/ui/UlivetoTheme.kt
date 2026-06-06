package it.uliveto.browser.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.ui.tokens.AegeanBackground
import it.uliveto.browser.ui.tokens.AegeanDark
import it.uliveto.browser.ui.tokens.AegeanDeep
import it.uliveto.browser.ui.tokens.AegeanMid
import it.uliveto.browser.ui.tokens.AegeanSurface
import it.uliveto.browser.ui.tokens.AmalfiDark
import it.uliveto.browser.ui.tokens.AmalfiDeep
import it.uliveto.browser.ui.tokens.AmalfiGold
import it.uliveto.browser.ui.tokens.AmalfiSurface
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.CreamYellow
import it.uliveto.browser.ui.tokens.NeutralLight
import it.uliveto.browser.ui.tokens.NightBackground
import it.uliveto.browser.ui.tokens.NightSurface
import it.uliveto.browser.ui.tokens.NightSurface2
import it.uliveto.browser.ui.tokens.OliveDeep
import it.uliveto.browser.ui.tokens.OliveDark
import it.uliveto.browser.ui.tokens.OliveMid
import it.uliveto.browser.ui.tokens.OliveSurface
import it.uliveto.browser.ui.tokens.OliveGreen
import it.uliveto.browser.ui.tokens.Parchment
import it.uliveto.browser.ui.tokens.SantoriniBlue
import it.uliveto.browser.ui.tokens.SantoriniDark
import it.uliveto.browser.ui.tokens.SantoriniDeep
import it.uliveto.browser.ui.tokens.SantoriniGold
import it.uliveto.browser.ui.tokens.SantoriniSurface
import it.uliveto.browser.ui.tokens.SnowWhite
import it.uliveto.browser.ui.tokens.Terracotta1
import it.uliveto.browser.ui.tokens.Terracotta2
import it.uliveto.browser.ui.tokens.Terracotta3
import it.uliveto.browser.ui.tokens.UlivetoTypography
import it.uliveto.browser.ui.tokens.WarmCream

// ── Gradient colours passed to screens that need a background ─────────
data class UlivetoColors(val gradientColors: List<Color>)

val LocalUlivetoColors = staticCompositionLocalOf {
    UlivetoColors(listOf(Color(0xFFB25737), Color(0xFF9D4626), Color(0xFF7E3415)))
}

// ── Terracotta ────────────────────────────────────────────────────────
private val TerracottaLight = lightColorScheme(
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
private val TerracottaGradient = listOf(Terracotta1, Terracotta2, Terracotta3)

// ── Aegean ────────────────────────────────────────────────────────────
private val AegeanLight = lightColorScheme(
    primary = AegeanDeep,
    onPrimary = Color.White,
    primaryContainer = AegeanMid,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF5BA4C5),
    onSecondary = Color.White,
    background = AegeanBackground,
    onBackground = CharcoalDark,
    surface = AegeanSurface,
    onSurface = CharcoalDark,
    surfaceVariant = AegeanSurface,
    onSurfaceVariant = AegeanMid,
)
private val AegeanGradient = listOf(AegeanDeep, AegeanMid, AegeanDark)

// ── Olive Grove ───────────────────────────────────────────────────────
private val OliveGroveLight = lightColorScheme(
    primary = OliveDeep,
    onPrimary = Color.White,
    primaryContainer = OliveMid,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA8925A),
    onSecondary = Color.White,
    background = Parchment,
    onBackground = CharcoalDark,
    surface = OliveSurface,
    onSurface = CharcoalDark,
    surfaceVariant = OliveSurface,
    onSurfaceVariant = OliveMid,
)
private val OliveGroveGradient = listOf(OliveDeep, OliveMid, OliveDark)

// ── Amalfi ────────────────────────────────────────────────────────────
// AmalfiGold (#C17F18) on CreamYellow (#FFFBF0) = 2.98:1 — fails WCAG AA for any text.
// AmalfiDeep (#8A5A10) on CreamYellow = 5.12:1 — passes AA for all text sizes.
// The rich dark-amber hue reads unmistakably as "Amalfi gold" while remaining legible.
private val AmalfiLight = lightColorScheme(
    primary = AmalfiDeep,
    onPrimary = Color.White,          // 5.27:1 on AmalfiDeep ✓
    primaryContainer = AmalfiDark,
    onPrimaryContainer = WarmCream,   // 7.25:1 on AmalfiDark ✓
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    background = CreamYellow,
    onBackground = CharcoalDark,
    surface = AmalfiSurface,
    onSurface = CharcoalDark,
    surfaceVariant = AmalfiSurface,
    onSurfaceVariant = AmalfiDeep,
)
private val AmalfiGradient = listOf(AmalfiGold, AmalfiDeep, AmalfiDark)

// ── Santorini ─────────────────────────────────────────────────────────
private val SantoriniLight = lightColorScheme(
    primary = SantoriniBlue,
    onPrimary = Color.White,
    primaryContainer = SantoriniDeep,
    onPrimaryContainer = Color.White,
    secondary = SantoriniGold,
    onSecondary = Color.White,
    background = SnowWhite,
    onBackground = CharcoalDark,
    surface = SantoriniSurface,
    onSurface = CharcoalDark,
    surfaceVariant = SantoriniSurface,
    onSurfaceVariant = SantoriniDeep,
)
private val SantoriniGradient = listOf(SantoriniBlue, SantoriniDeep, SantoriniDark)

// ── Night (dark Terracotta) ───────────────────────────────────────────
private val NightDark = darkColorScheme(
    primary = Terracotta1,
    onPrimary = WarmCream,
    primaryContainer = Terracotta3,
    onPrimaryContainer = WarmCream,
    secondary = OliveGreen,
    onSecondary = WarmCream,
    background = NightBackground,
    onBackground = WarmCream,
    surface = NightSurface,
    onSurface = WarmCream,
    surfaceVariant = NightSurface2,
    onSurfaceVariant = Terracotta2,
)
private val NightGradient = listOf(Color(0xFF3A1A0A), Color(0xFF200E06), Color(0xFF120808))

// ── Theme compositor ──────────────────────────────────────────────────
@Composable
fun UlivetoTheme(
    appTheme: AppTheme = AppTheme.Terracotta,
    content: @Composable () -> Unit,
) {
    val isDark = when (appTheme) {
        AppTheme.Night -> true
        AppTheme.System -> isSystemInDarkTheme()
        else -> false
    }

    val colorScheme = when (appTheme) {
        AppTheme.Terracotta -> TerracottaLight
        AppTheme.Aegean -> AegeanLight
        AppTheme.OliveGrove -> OliveGroveLight
        AppTheme.Amalfi -> AmalfiLight
        AppTheme.Santorini -> SantoriniLight
        AppTheme.Night -> NightDark
        AppTheme.System -> if (isDark) NightDark else TerracottaLight
    }

    val gradient = when (appTheme) {
        AppTheme.Terracotta -> TerracottaGradient
        AppTheme.Aegean -> AegeanGradient
        AppTheme.OliveGrove -> OliveGroveGradient
        AppTheme.Amalfi -> AmalfiGradient
        AppTheme.Santorini -> SantoriniGradient
        AppTheme.Night -> NightGradient
        AppTheme.System -> if (isDark) NightGradient else TerracottaGradient
    }

    CompositionLocalProvider(LocalUlivetoColors provides UlivetoColors(gradient)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = UlivetoTypography,
            content = content,
        )
    }
}
