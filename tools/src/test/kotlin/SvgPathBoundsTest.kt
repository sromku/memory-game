import org.junit.Assert.assertEquals
import org.junit.Test

class SvgPathBoundsTest {

    @Test
    fun `absolute and relative commands give the same box`() {
        val absolute = SvgPathBounds.of("M10,20 L30,20 L30,60 L10,60 Z")
        val relative = SvgPathBounds.of("M10,20 l20,0 l0,40 l-20,0 z")
        assertEquals(absolute, relative)
        assertEquals(SvgPathBounds.Box(10f, 20f, 30f, 60f), absolute)
    }

    @Test
    fun `curves count their control points and implicit line-to after move`() {
        val box = SvgPathBounds.of("M0,0c10,-30 40,-30 50,0 10,10 20,10 30,0")
        assertEquals(0f, box.left, 0f)
        assertEquals(-30f, box.top, 0f)
        assertEquals(80f, box.right, 0f)
        assertEquals(10f, box.bottom, 0f)
    }

    @Test
    fun `horizontal, vertical, quadratic and arc segments are followed`() {
        val box = SvgPathBounds.of("M5,5 h10 v10 q5,5 10,0 a3,3 0 0 1 6,6 H0 V40")
        assertEquals(SvgPathBounds.Box(0f, 5f, 31f, 40f), box)
    }
}
