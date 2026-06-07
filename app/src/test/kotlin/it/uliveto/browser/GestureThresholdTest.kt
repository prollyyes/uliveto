package it.uliveto.browser

import it.uliveto.browser.ui.screens.start.shouldCommitGesture
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class GestureThresholdTest {

    @Test
    fun `commits when offset exceeds threshold`() {
        assertTrue(shouldCommitGesture(offsetPx = 400f, velocityPx = 0f, thresholdPx = 360f))
    }

    @Test
    fun `commits when velocity exceeds threshold`() {
        assertTrue(shouldCommitGesture(offsetPx = 50f, velocityPx = 950f, thresholdPx = 360f))
    }

    @Test
    fun `cancels when both below threshold`() {
        assertFalse(shouldCommitGesture(offsetPx = 100f, velocityPx = 200f, thresholdPx = 360f))
    }

    @Test
    fun `commits exactly at offset threshold`() {
        assertTrue(shouldCommitGesture(offsetPx = 360f, velocityPx = 0f, thresholdPx = 360f))
    }
}
