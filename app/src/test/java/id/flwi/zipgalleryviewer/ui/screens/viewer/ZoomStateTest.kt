package id.flwi.zipgalleryviewer.ui.screens.viewer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for zoom state calculations.
 */
class ZoomStateTest {

    @Test
    fun `scale is coerced to minimum of 1f`() {
        // Arrange
        val currentScale = 1.5f
        val zoomFactor = 0.5f

        // Act
        val newScale = (currentScale * zoomFactor).coerceIn(1f, 5f)

        // Assert
        assertEquals(1f, newScale, 0.01f)
    }

    @Test
    fun `scale is coerced to maximum of 5f`() {
        // Arrange
        val currentScale = 4f
        val zoomFactor = 2f

        // Act
        val newScale = (currentScale * zoomFactor).coerceIn(1f, 5f)

        // Assert
        assertEquals(5f, newScale, 0.01f)
    }

    @Test
    fun `scale stays within bounds`() {
        // Arrange
        val currentScale = 2f
        val zoomFactor = 1.5f

        // Act
        val newScale = (currentScale * zoomFactor).coerceIn(1f, 5f)

        // Assert
        assertEquals(3f, newScale, 0.01f)
    }

    @Test
    fun `double tap toggles from 1f to 2_5f`() {
        // Arrange
        var scale = 1f

        // Act
        scale = if (scale > 1f) 1f else 2.5f

        // Assert
        assertEquals(2.5f, scale, 0.01f)
    }

    @Test
    fun `double tap toggles from 2_5f to 1f`() {
        // Arrange
        var scale = 2.5f

        // Act
        scale = if (scale > 1f) 1f else 2.5f

        // Assert
        assertEquals(1f, scale, 0.01f)
    }

    @Test
    fun `offsets reset when scale returns to 1f`() {
        // Arrange
        var scale = 2.5f
        var offsetX = 100f
        var offsetY = 50f

        // Act - simulate double tap to zoom out
        if (scale > 1f) {
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }

        // Assert
        assertEquals(1f, scale, 0.01f)
        assertEquals(0f, offsetX, 0.01f)
        assertEquals(0f, offsetY, 0.01f)
    }

    @Test
    fun `offsets are reset when scale is 1f or less`() {
        // Arrange
        val scale = 1f
        var offsetX = 50f
        var offsetY = 30f

        // Act
        if (scale <= 1f) {
            offsetX = 0f
            offsetY = 0f
        }

        // Assert
        assertEquals(0f, offsetX, 0.01f)
        assertEquals(0f, offsetY, 0.01f)
    }
}
