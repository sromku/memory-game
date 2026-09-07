package com.snatik.matches.ui.road

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadGeometryTest {

    private val road = RoadGeometry(roundCount = 40, spacing = 100f, margin = 120f, centerY = 300f, amplitude = 80f)

    @Test
    fun `rounds are evenly spaced and the road waves around the centre line`() {
        assertEquals(120f, road.x(1))
        assertEquals(4020f, road.x(40))
        assertEquals(2 * 120f + 39 * 100f, road.contentWidth)
        assertEquals(300f, road.y(1), 0.001f)
        assertEquals(380f, road.y(3), 0.001f) // a quarter wave in
        assertEquals(220f, road.y(7), 0.001f)
        assertEquals(300f, road.y(9), 0.001f) // one full wave later
        assertTrue((1..40).all { road.y(it) in 220f..380f })
    }

    @Test
    fun `hit testing finds the round under a finger`() {
        assertEquals(1, road.roundAt(125f, 310f, radius = 30f))
        assertEquals(3, road.roundAt(road.x(3) + 20f, road.y(3) - 20f, radius = 30f))
        assertNull(road.roundAt(170f, 300f, radius = 30f)) // between rounds
        assertNull(road.roundAt(0f, 0f, radius = 30f))
    }

    @Test
    fun `visible rounds cover the window plus their reach`() {
        assertEquals(1..5, road.roundsWithin(0f, 500f, reach = 50f))
        assertEquals(10..15, road.roundsWithin(1000f, 1500f, reach = 50f))
        assertEquals(36..40, road.roundsWithin(3600f, 9000f, reach = 50f))
        assertEquals(1..1, road.roundsWithin(-5000f, -4000f, reach = 50f))
    }

    @Test
    fun `scrolling centres a round and stays within the content`() {
        assertEquals(0, road.scrollToCenter(1, viewWidth = 800))
        assertEquals(1520 - 400, road.scrollToCenter(15, viewWidth = 800))
        assertEquals(road.maxScroll(800), road.scrollToCenter(40, viewWidth = 800))
        assertEquals(0, road.maxScroll(10_000))
    }
}
