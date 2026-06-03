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
 * Real blur/frosted-glass implementation arrives in M8.
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
 * M4 implementation: semi-transparent milky fill + top rim highlight.
 * Real backdrop blur deferred to M8.
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
