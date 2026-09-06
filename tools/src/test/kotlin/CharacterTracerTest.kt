import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterTracerTest {

    private val tracer = CharacterTracer(File("/nonexistent/vtracer"), null) {}

    private fun part(color: Int, left: Float, top: Float, right: Float, bottom: Float) =
        CharacterTracer.Part(color, "M$left,$top L$right,$top L$right,$bottom L$left,$bottom Z", SvgPathBounds.of("M$left,$top L$right,$top L$right,$bottom L$left,$bottom Z"))

    @Test
    fun `parses fills and path data from traced svg`() {
        val parts = tracer.parse("""<svg><path d="M0,0 L10,0 L10,10 Z" fill="#AA5500"/><path d="M1,1 L2,2" fill="#000000"/></svg>""")
        assertEquals(listOf(0xAA5500, 0), parts.map { it.color })
        assertEquals(SvgPathBounds.Box(0f, 0f, 10f, 10f), parts[0].box)
    }

    @Test
    fun `a mirrored pair of dark dots in the upper half becomes pupils`() {
        val body = part(0xA96400, 100f, 100f, 900f, 950f)
        val left = part(0x1E2D2D, 300f, 300f, 340f, 340f)
        val right = part(0x1E2D2D, 660f, 300f, 700f, 340f)
        val nostril = part(0x1E2D2D, 490f, 520f, 510f, 540f)
        val shadow = part(0x4FA9CC, 200f, 900f, 800f, 960f)
        val parts = listOf(body, left, right, nostril, shadow)
        tracer.classify(parts, 1000f, 1000f)
        assertEquals(CharacterTracer.Group.BODY, body.group)
        assertEquals(CharacterTracer.Group.PUPIL, left.group)
        assertEquals(CharacterTracer.Group.PUPIL, right.group)
        assertEquals(CharacterTracer.Group.BODY, nostril.group)
        assertEquals(CharacterTracer.Group.SHADOW, shadow.group)
    }

    @Test
    fun `eye whites take their pupils with them`() {
        val whiteL = part(0xFFFFFF, 300f, 280f, 400f, 380f)
        val whiteR = part(0xFFFFFF, 600f, 280f, 700f, 380f)
        val pupilL = part(0x111111, 340f, 320f, 360f, 340f)
        val pupilR = part(0x111111, 640f, 320f, 660f, 340f)
        val tooth = part(0xFFFFFF, 480f, 600f, 520f, 640f)
        val parts = listOf(whiteL, whiteR, pupilL, pupilR, tooth)
        tracer.classify(parts, 1000f, 1000f)
        assertEquals(listOf(CharacterTracer.Group.EYE, CharacterTracer.Group.EYE, CharacterTracer.Group.PUPIL, CharacterTracer.Group.PUPIL, CharacterTracer.Group.BODY), parts.map { it.group })
    }
}
