package com.snatik.matches.ui.image

import org.junit.Assert.assertEquals
import org.junit.Test

class BitmapLoaderTest {

    @Test
    fun `no subsampling when the image already fits`() {
        assertEquals(1, BitmapLoader.sampleSize(400, 400, 400, 400))
        assertEquals(1, BitmapLoader.sampleSize(400, 400, 300, 300))
    }

    @Test
    fun `largest power of two that keeps both sides at least the required size`() {
        assertEquals(4, BitmapLoader.sampleSize(400, 400, 100, 100))
        assertEquals(2, BitmapLoader.sampleSize(400, 400, 101, 101))
        assertEquals(2, BitmapLoader.sampleSize(1880, 1200, 800, 400))
        assertEquals(1, BitmapLoader.sampleSize(1880, 1200, 800, 700))
    }

    @Test
    fun `nonsense required sizes decode at full resolution`() {
        assertEquals(1, BitmapLoader.sampleSize(400, 400, 0, 0))
    }
}
