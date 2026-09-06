import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

/**
 * Turns a UI picture (button, popup, star...) into an Android vector drawable, res/drawable/<name>.xml,
 * from its 4x master. One file replaces nine density buckets and stays crisp at any size.
 *
 * UI art has anti-aliased edges and soft drop shadows, which a flat-colour tracer cannot express,
 * so the picture is taken apart first:
 *  1. Colours are bled outwards into the transparent area, so the tracer never sees a fringe.
 *  2. The silhouette (alpha above one half) is traced on its own and becomes a clip path; the
 *     renderer anti-aliases that edge itself.
 *  3. The soft region (faint alpha outside the silhouette, i.e. the drop shadow) is traced as one
 *     shape and drawn underneath with the region's average colour and opacity.
 *
 * The intrinsic size is the largest size the asset is laid out at (tablets), so wrap_content
 * views never upscale it; layouts scale it down where they give less room.
 */
class VectorArtTracer(private val vtracer: File, private val log: (String) -> Unit) {

    private val pathTag = Regex("<path[^>]*/>", RegexOption.DOT_MATCHES_ALL)
    private val fillAttr = Regex("fill=\"#([0-9A-Fa-f]{6})\"")
    private val dAttr = Regex("d=\"([^\"]+)\"")

    fun trace(master: File, target: File, widthDp: Int, heightDp: Int): Int {
        val image = ImageIO.read(master)
        val w = image.width; val h = image.height
        val argb = IntArray(w * h).also { image.getRGB(0, 0, w, h, it, 0, w) }
        val alpha = FloatArray(w * h) { ((argb[it] ushr 24) and 0xFF) / 255f }

        val bled = bleed(argb, alpha, w, h)
        val silhouette = mask(w, h) { alpha[it] > 0.5f }
        val soft = mask(w, h) { alpha[it] > 0.06f && alpha[it] <= 0.5f }
        val softPixels = (0 until w * h).filter { alpha[it] > 0.06f && alpha[it] <= 0.5f }

        val colourPaths = traceSvg(bled, "--clustering", "color-cluster", "--hierarchical", "stacked", "-f", "4", "-p", "8", "-g", "16", "--simplify", "1.2", "--optimize", "1")
        val hasSilhouette = (0 until w * h).any { alpha[it] > 0.5f }
        val silhouettePaths = if (hasSilhouette) traceSvg(silhouette, "--clustering", "bw", "-f", "8", "--simplify", "1.0") else emptyList()
        val shadowPaths = if (softPixels.isEmpty()) emptyList() else traceSvg(soft, "--clustering", "bw", "-f", "16", "--simplify", "2.0")

        target.parentFile.mkdirs()
        target.writeText(buildString {
            appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
            appendLine("<!-- Generated from art/original/${master.name} by ./gradlew regenerateArt; do not edit. -->")
            appendLine("""<vector xmlns:android="http://schemas.android.com/apk/res/android"""")
            appendLine("""    android:width="${widthDp}dp"""")
            appendLine("""    android:height="${heightDp}dp"""")
            appendLine("""    android:viewportWidth="$w"""")
            appendLine("""    android:viewportHeight="$h">""")
            if (shadowPaths.isNotEmpty()) {
                val shadowColor = averageColor(argb, softPixels)
                val shadowAlpha = softPixels.map { alpha[it] }.average()
                appendLine("""    <path android:fillColor="#%06X" android:fillAlpha="%.2f" android:pathData="%s" />""".format(shadowColor, shadowAlpha, shadowPaths.joinToString(" ") { it.second }))
            }
            // A fully translucent picture (the card-back star) has no silhouette: its soft layer is the whole
            // drawing. Anything else is clipped to its silhouette so the edge is anti-aliased by the renderer.
            if (silhouettePaths.isNotEmpty() && colourPaths.isNotEmpty()) {
                appendLine("    <group>")
                appendLine("""        <clip-path android:pathData="${silhouettePaths.joinToString(" ") { it.second }}" />""")
                for ((fill, d) in colourPaths) appendLine("""        <path android:fillColor="#$fill" android:pathData="$d" />""")
                appendLine("    </group>")
            }
            appendLine("</vector>")
        })
        log("vectorised ${master.nameWithoutExtension}: ${colourPaths.size} paths, ${target.length() / 1024} KB")
        return colourPaths.size
    }

    /** Fills transparent pixels with the colour of nearby opaque ones: blur(rgb * a) / blur(a), at growing radii. */
    private fun bleed(argb: IntArray, alpha: FloatArray, w: Int, h: Int): BufferedImage {
        val r = FloatArray(w * h) { ((argb[it] shr 16) and 0xFF) * alpha[it] }
        val g = FloatArray(w * h) { ((argb[it] shr 8) and 0xFF) * alpha[it] }
        val b = FloatArray(w * h) { (argb[it] and 0xFF) * alpha[it] }
        val outR = FloatArray(w * h) { ((argb[it] shr 16) and 0xFF).toFloat() }
        val outG = FloatArray(w * h) { ((argb[it] shr 8) and 0xFF).toFloat() }
        val outB = FloatArray(w * h) { (argb[it] and 0xFF).toFloat() }
        val done = BooleanArray(w * h) { alpha[it] > 0.98f }
        for (radius in intArrayOf(2, 6, 16, 40, 100)) {
            val br = boxBlur(r, w, h, radius); val bg = boxBlur(g, w, h, radius); val bb = boxBlur(b, w, h, radius); val ba = boxBlur(alpha, w, h, radius)
            for (i in 0 until w * h) if (!done[i] && ba[i] > 0.02f) {
                outR[i] = br[i] / ba[i]; outG[i] = bg[i] / ba[i]; outB[i] = bb[i] / ba[i]; done[i] = true
            }
        }
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        out.setRGB(0, 0, w, h, IntArray(w * h) { (clamp(outR[it]) shl 16) or (clamp(outG[it]) shl 8) or clamp(outB[it]) }, 0, w)
        return out
    }

    private fun clamp(v: Float) = max(0, min(255, v.toInt()))

    /** Separable box blur, three passes approximate a Gaussian well enough for colour bleeding. */
    private fun boxBlur(src: FloatArray, w: Int, h: Int, radius: Int): FloatArray {
        var cur = src
        repeat(3) { cur = blurAxis(blurAxis(cur, w, h, radius, true), w, h, radius, false) }
        return cur
    }

    private fun blurAxis(src: FloatArray, w: Int, h: Int, radius: Int, horizontal: Boolean): FloatArray {
        val out = FloatArray(w * h)
        val lines = if (horizontal) h else w; val len = if (horizontal) w else h
        for (line in 0 until lines) {
            fun idx(i: Int) = if (horizontal) line * w + i else i * w + line
            var sum = 0f
            for (i in 0 until min(radius, len)) sum += src[idx(i)]
            for (i in 0 until len) {
                if (i + radius < len) sum += src[idx(i + radius)]
                if (i - radius - 1 >= 0) sum -= src[idx(i - radius - 1)]
                val count = min(i + radius, len - 1) - max(i - radius, 0) + 1
                out[idx(i)] = sum / count
            }
        }
        return out
    }

    private fun mask(w: Int, h: Int, inside: (Int) -> Boolean): BufferedImage {
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        out.setRGB(0, 0, w, h, IntArray(w * h) { if (inside(it)) 0x000000 else 0xFFFFFF }, 0, w)
        return out
    }

    private fun averageColor(argb: IntArray, pixels: List<Int>): Int {
        val r = pixels.map { (argb[it] shr 16) and 0xFF }.average(); val g = pixels.map { (argb[it] shr 8) and 0xFF }.average(); val b = pixels.map { argb[it] and 0xFF }.average()
        return (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }

    private fun traceSvg(image: BufferedImage, vararg options: String): List<Pair<String, String>> {
        val png = File.createTempFile("art", ".png"); val svg = File.createTempFile("art", ".svg")
        try {
            ImageIO.write(image, "png", png)
            run(vtracer.path, "--mode", "spline", "--path-precision", "1", *options, "-i", png.path, "-o", svg.path)
            return pathTag.findAll(svg.readText()).mapNotNull { m ->
                val fill = fillAttr.find(m.value)?.groupValues?.get(1) ?: return@mapNotNull null
                val d = dAttr.find(m.value)?.groupValues?.get(1) ?: return@mapNotNull null
                fill to d
            }.toList()
        } finally {
            png.delete(); svg.delete()
        }
    }

    private fun run(vararg command: String) {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "${command.joinToString(" ")}\n$output" }
    }
}
