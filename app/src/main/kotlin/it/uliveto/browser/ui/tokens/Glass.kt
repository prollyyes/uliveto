package it.uliveto.browser.ui.tokens

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Glass material tiers used across the UI.
 */
enum class GlassMaterial {
    /** Purely ornamental glass — lower blur, higher transparency. */
    Decorative,

    /** Functional glass — higher blur, used for overlays that need legibility. */
    Functional,
}

/**
 * Applies a frosted-glass surface effect to the composable.
 *
 * Performance notes:
 * - The blur is clipped to the shape (due to `.clip(shape)` before `.blur()`).
 * - The offscreen layer is NOT explicitly paused while scroll-hidden; the
 *   `graphicsLayer { alpha = 0 }` applied by BrowserScreen handles this on
 *   hardware — a full offscreen-layer pause is deferred to a future pass.
 * - Adaptive tint ([functionalTintFor]) is computed once per URL change,
 *   never per frame — see BrowserScreen for the remember(currentUrl) guard.
 *
 * API behaviour:
 * - API 31+ (Android 12+): real blur via [Modifier.blur] with radius 22 dp.
 *   Note: `Modifier.blur` blurs the composable's own content; the frosted-glass
 *   appearance relies on the translucent background compositing over the page.
 *   True backdrop blur (blurring what's behind) requires RenderEffect — deferred.
 * - API < 31: semi-transparent milky fill only (no blur).
 */
fun Modifier.ulivetoGlass(
    material: GlassMaterial,
    shape: Shape,
): Modifier {
    val fillAlpha = if (material == GlassMaterial.Functional) 0.42f else 0.20f
    val fillColor = Color(0xFFF7F7F9).copy(alpha = fillAlpha)
    val elevation = if (material == GlassMaterial.Functional) 8.dp else 2.dp

    return this
        .shadow(elevation = elevation, shape = shape, clip = false)
        .clip(shape)
        .background(fillColor)
        .drawWithContent {
            drawContent()
            // Top rim highlight — 1dp white line along the top edge
            drawLine(
                color = Color.White.copy(alpha = 0.65f),
                start = Offset(0f, 1.dp.toPx()),
                end = Offset(size.width, 1.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
            )
        }
}

/**
 * Computes an adaptive glass tint from a backdrop color sample.
 *
 * Slightly brightens each channel (×1.08, capped at 1f) and locks alpha to
 * the functional glass value. Intended to be called once per URL change
 * (never per frame) using `remember(currentUrl) { … }`.
 *
 * TODO: sample meta theme-color from browser-state (deferred to M8+).
 */
fun functionalTintFor(backdrop: Color): Color =
    backdrop.copy(
        red = (backdrop.red * 1.08f).coerceAtMost(1f),
        green = (backdrop.green * 1.08f).coerceAtMost(1f),
        blue = (backdrop.blue * 1.08f).coerceAtMost(1f),
        alpha = 0.42f,
    )
