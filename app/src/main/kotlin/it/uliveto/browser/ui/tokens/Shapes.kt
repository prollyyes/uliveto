package it.uliveto.browser.ui.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/** Full pill shape — 50% corner radius. */
val PillShape = RoundedCornerShape(50)

/**
 * Hourglass shape — two circular end caps connected by a concave waist transition.
 *
 * Geometry (all in dp):
 *  - End circles: Ø44dp (radius 22dp)
 *  - Center section: 44dp tall
 *  - Waist: 30dp horizontal span, 12dp concave pinch
 */
object HourglassShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(hourglassPath(size, density))

    private fun hourglassPath(
        size: androidx.compose.ui.geometry.Size,
        density: Density,
    ): Path {
        val dp = density.density
        val r = 22 * dp          // circle radius
        val waistSpan = 30 * dp  // horizontal width of waist transition
        val pinch = 12 * dp      // concave depth of waist
        val h = size.height      // total height = 44dp
        val w = size.width       // total width

        return Path().apply {
            // Start at top of left circle (rightmost top point before waist)
            moveTo(r, 0f)

            // Top-left arc (left circle, top half going counter-clockwise to bottom)
            arcTo(
                rect = Rect(0f, 0f, 2 * r, h),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )
            // Now at bottom of left circle (r, h)

            // Bottom-left waist (concave cubic from bottom of left circle to bottom-left of center)
            cubicTo(
                r + waistSpan * 0.3f, h,
                r + waistSpan * 0.7f, h - pinch,
                r + waistSpan, h - pinch,
            )

            // Bottom of center rectangle
            lineTo(w - r - waistSpan, h - pinch)

            // Bottom-right waist — true mirror of bottom-left (0.7, 0.3 from start)
            cubicTo(
                w - r - waistSpan * 0.7f, h - pinch,
                w - r - waistSpan * 0.3f, h,
                w - r, h,
            )

            // Right circle: bottom → top arc (counter-clockwise)
            arcTo(
                rect = Rect(w - 2 * r, 0f, w, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )
            // Now at top of right circle (w - r, 0f)

            // Top-right waist — true mirror of top-left (0.7, 0.3 from start)
            cubicTo(
                w - r - waistSpan * 0.7f, 0f,
                w - r - waistSpan * 0.3f, pinch,
                w - r - waistSpan, pinch,
            )

            // Top of center rectangle
            lineTo(r + waistSpan, pinch)

            // Top-left waist (mirror of bottom-left)
            cubicTo(
                r + waistSpan * 0.7f, pinch,
                r + waistSpan * 0.3f, 0f,
                r, 0f,
            )

            close()
        }
    }
}
