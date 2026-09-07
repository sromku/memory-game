package com.snatik.matches.ui.image

import org.junit.Assert.assertEquals
import org.junit.Test

class CenterCropTest {

    @Test
    fun `a wider view scales by width and crops the top and bottom`() {
        // 2:1 image in a 2.23:1 view: scale 2410/3072, 62 px cropped top and bottom.
        val y = CenterCrop.y(0.83f, imageWidth = 3072, imageHeight = 1536, viewWidth = 2410, viewHeight = 1080)
        assertEquals(0.83f * 1536 * (2410f / 3072) - (1536 * (2410f / 3072) - 1080) / 2, y, 0.01f)
    }

    @Test
    fun `a taller view scales by height and crops the sides only`() {
        val y = CenterCrop.y(0.5f, imageWidth = 1000, imageHeight = 500, viewWidth = 800, viewHeight = 800)
        assertEquals(400f, y, 0.001f)
    }

    @Test
    fun `an image of the view's shape maps fractions straight through`() {
        assertEquals(750f, CenterCrop.y(0.75f, 200, 100, 2000, 1000), 0.001f)
    }
}
