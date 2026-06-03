package it.uliveto.browser

import androidx.compose.ui.graphics.Color
import it.uliveto.browser.ui.tokens.functionalTintFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassTintMathTest {

    @Test
    fun `functional tint lightens and caps at 1f`() {
        val base = Color(0.5f, 0.5f, 0.5f, 1f)
        val tint = functionalTintFor(base)
        assertEquals(0.42f, tint.alpha, 0.001f)
        assertTrue(tint.red >= base.red)
        assertTrue(tint.green >= base.green)
        assertTrue(tint.blue >= base.blue)
    }

    @Test
    fun `functional tint does not exceed 1f on saturated color`() {
        val saturated = Color(1f, 0f, 0f, 1f)
        val tint = functionalTintFor(saturated)
        assertTrue(tint.red <= 1f)
    }
}
