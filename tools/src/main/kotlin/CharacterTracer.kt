import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Turns a card picture into vector paths the game can draw and animate (assets/characters/<name>.chr).
 *
 * The master (4x upscaled original) is composited onto the card face colour, traced with vtracer,
 * the card-coloured paths are dropped again, and the remaining parts are tagged by geometry:
 * the drop shadow is the wide bluish shape at the bottom; eyes are a left/right pair of small
 * white or dark blobs in the upper half; pupils are dark blobs inside eye whites.
 *
 * File format, one part per line after the header:
 *   <viewport width> <viewport height>
 *   <group> <#RRGGBB> <svg path data>
 */
class CharacterTracer(private val vtracer: File, private val overrides: File?, private val log: (String) -> Unit) {

    /** Hand corrections from art/character-overrides.txt: character name to rule. */
    private val rules: Map<String, String> = overrides?.takeIf { it.exists() }?.readLines().orEmpty()
        .map { it.substringBefore('#').trim() }.filter { it.isNotEmpty() }
        .associate { line -> line.substringBefore(' ') to line.substringAfter(' ').trim() }

    enum class Group { BODY, EYE, PUPIL, SHADOW }

    class Part(val color: Int, val pathData: String, val box: SvgPathBounds.Box) {
        var group = Group.BODY
    }

    fun trace(master: File, target: File) {
        val image = ImageIO.read(master)
        val flat = File.createTempFile("card", ".png")
        val svg = File.createTempFile("card", ".svg")
        try {
            ImageIO.write(composited(image, CARD_FACE), "png", flat)
            run(
                vtracer.path, "--clustering", "color-cluster", "--hierarchical", "stacked", "--mode", "spline",
                "--path-precision", "1", "-f", "8", "-p", "8", "-g", "16", "--simplify", "1.5", "--optimize", "1",
                "-i", flat.path, "-o", svg.path,
            )
            val parts = parse(svg.readText()).filterNot { isCardFace(it.color) }
            classify(parts, image.width.toFloat(), image.height.toFloat())
            if (rules[master.nameWithoutExtension] == "no-eyes") {
                parts.filter { it.group == Group.EYE || it.group == Group.PUPIL }.forEach { it.group = Group.BODY }
            }
            target.parentFile.mkdirs()
            target.writeText(buildString {
                appendLine("${image.width} ${image.height}")
                for (p in parts) appendLine("${p.group.name.lowercase()} #%06X ${p.pathData}".format(p.color and 0xFFFFFF))
            })
            val counts = parts.groupingBy { it.group }.eachCount()
            log("traced ${master.nameWithoutExtension}: ${parts.size} parts $counts")
        } finally {
            flat.delete(); svg.delete()
        }
    }

    private fun composited(image: BufferedImage, background: Color): BufferedImage {
        val out = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val g = out.createGraphics()
        g.color = background; g.fillRect(0, 0, out.width, out.height)
        g.drawImage(image, 0, 0, null); g.dispose()
        return out
    }

    private val pathTag = Regex("<path[^>]*/>", RegexOption.DOT_MATCHES_ALL)
    private val fillAttr = Regex("fill=\"#([0-9A-Fa-f]{6})\"")
    private val dAttr = Regex("d=\"([^\"]+)\"")

    fun parse(svg: String): List<Part> = pathTag.findAll(svg).mapNotNull { m ->
        val fill = fillAttr.find(m.value)?.groupValues?.get(1) ?: return@mapNotNull null
        val d = dAttr.find(m.value)?.groupValues?.get(1) ?: return@mapNotNull null
        Part(fill.toInt(16), d, SvgPathBounds.of(d))
    }.toList()

    private fun isCardFace(color: Int) =
        abs(r(color) - CARD_FACE.red) < 14 && abs(g(color) - CARD_FACE.green) < 14 && abs(b(color) - CARD_FACE.blue) < 14

    fun classify(parts: List<Part>, width: Float, height: Float) {
        val full = width * height
        for (p in parts) {
            val bx = p.box
            if (bx.top > height * 0.6f && bx.width > width * 0.25f && bx.height < height * 0.2f && b(p.color) > r(p.color) && b(p.color) > g(p.color)) p.group = Group.SHADOW
        }
        val body = parts.filter { it.group == Group.BODY }
        val whites = body.filter { minOf(r(it.color), g(it.color), b(it.color)) > 200 && it.box.area < full * 0.06f && it.box.bottom < height * 0.7f }
        val darks = body.filter { maxOf(r(it.color), g(it.color), b(it.color)) < 90 && it.box.area < full * 0.03f && it.box.bottom < height * 0.65f }
        val eyeWhites = symmetricPair(whites, width, height) ?: whites.takeIf { it.size in 2..6 && sameSize(it) }.orEmpty()
        eyeWhites.forEach { it.group = Group.EYE }
        if (eyeWhites.isNotEmpty()) {
            darks.filter { d -> eyeWhites.any { e -> e.box.contains(d.box.centerX, d.box.centerY) } }.forEach { it.group = Group.PUPIL }
        } else {
            symmetricPair(darks.filter { it.box.area > full * 0.0005f }, width, height)?.forEach { it.group = Group.PUPIL }
        }
    }

    /** Two blobs mirrored about the vertical centre line, level with each other and of similar size. */
    private fun symmetricPair(candidates: List<Part>, width: Float, height: Float): List<Part>? {
        var best: Pair<Part, Part>? = null
        for (i in candidates.indices) for (j in i + 1 until candidates.size) {
            val a = candidates[i].box; val b = candidates[j].box
            val mirrored = abs((a.centerX + b.centerX) / 2 - width / 2) < width * 0.1f
            val level = abs(a.centerY - b.centerY) < height * 0.05f
            val apart = abs(a.centerX - b.centerX) > width * 0.06f
            val similar = a.width / b.width in 0.6f..1.6f && a.height / b.height in 0.6f..1.6f
            if (mirrored && level && apart && similar && (best == null || a.area > best.first.box.area)) best = candidates[i] to candidates[j]
        }
        return best?.toList()
    }

    private fun sameSize(parts: List<Part>) = parts.maxOf { it.box.area } / parts.minOf { it.box.area } < 3f
    private fun r(c: Int) = (c shr 16) and 0xFF
    private fun g(c: Int) = (c shr 8) and 0xFF
    private fun b(c: Int) = c and 0xFF

    private fun run(vararg command: String) {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "${command.joinToString(" ")}\n$output" }
    }

    companion object {
        /** The card face colour the pictures are shown on (drawable/tile.xml). */
        val CARD_FACE = Color(0x66, 0xCF, 0xF2)
    }
}
