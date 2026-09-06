import java.awt.AlphaComposite
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * Regenerates every bitmap resource from the originals in art/original.
 *
 * 1. Real-ESRGAN (anime model) upscales each original 4x into art/master (git-ignored, cached).
 * 2. Each UI asset is rendered at the pixel size it is displayed at for every density bucket,
 *    phones (dp from values/dimens.xml) and tablets (values-sw720dp/dimens.xml) separately.
 * 3. Card pictures and backgrounds go to drawable-nodpi at 2x / 3x their original size; the app
 *    decodes them sub-sampled to the size actually shown.
 * 4. Everything is written as WebP (lossy, quality 92, lossless alpha), a third of PNG's size for
 *    this flat artwork.
 * 5. Launcher icons (legacy 48dp and adaptive 108dp foreground, every density) and the 512 px
 *    Play Store icon come from art/original/app_icon.png the same way.
 *
 * Outputs newer than their master are left alone, so a rerun only redoes what changed.
 */
class ArtPipeline(
    private val root: File,
    private val realesrgan: File,
    private val cwebp: File,
    private val log: (String) -> Unit,
) {
    /** A UI asset and the dp size it is displayed at on phones and on tablets. */
    private data class Asset(val name: String, val phoneDp: Int, val tabletDp: Int, val byWidth: Boolean = true)

    private val original = root.resolve("art/original")
    private val master = root.resolve("art/master")
    private val res = root.resolve("app/src/main/res")

    private val uiAssets: List<Asset> = buildList {
        add(Asset("title", 400, 800))
        add(Asset("button_start", 100, 200))
        add(Asset("button_start_lights", 150, 300))
        add(Asset("button_settings", 80, 160))
        add(Asset("tooltip_play", 80, 160))
        add(Asset("time_bar", 120, 140))
        add(Asset("level_complete", 240, 400))
        add(Asset("level_complete_star", 57, 95))
        add(Asset("settings_popup", 300, 400))
        add(Asset("button_back", 75, 120))
        add(Asset("button_again", 75, 120))
        add(Asset("button_music_on", 50, 65, byWidth = false))
        add(Asset("button_music_off", 50, 65, byWidth = false))
        add(Asset("button_rate", 50, 65, byWidth = false))
        add(Asset("tile_back_star", 90, 90))
        for (theme in listOf("animals", "monsters", "emoji")) for (stars in 0..3) add(Asset("${theme}_theme_star_$stars", 300, 360))
        for (level in 1..6) for (stars in 0..3) add(Asset("button_difficulty_${level}_star_$stars", 260, 340))
    }

    private val phoneBuckets = mapOf("drawable-mdpi" to 1.0, "drawable-hdpi" to 1.5, "drawable-xhdpi" to 2.0, "drawable-xxhdpi" to 3.0, "drawable-xxxhdpi" to 4.0)
    private val tabletBuckets = mapOf("drawable-sw600dp-mdpi" to 1.0, "drawable-sw600dp-hdpi" to 1.5, "drawable-sw600dp-xhdpi" to 2.0, "drawable-sw600dp-xxhdpi" to 3.0)

    fun run() {
        master.mkdirs()
        for (asset in uiAssets) {
            val m = upscaled(original.resolve("${asset.name}.png"))
            emit(asset, m, phoneBuckets, asset.phoneDp)
            emit(asset, m, tabletBuckets, asset.tabletDp)
        }
        // The largest screens are 2560 px wide: 3x of 1024 covers them, and so does 2x of 1880.
        background("background", 3760)
        background("back_animals", 3072)
        background("back_horror", 3072)
        // Card pictures show at up to ~660 px on a 10-inch tablet; 800 px keeps them sharp there.
        for (tile in original.resolve("tiles").listFiles { f -> f.extension == "png" }.orEmpty().sorted()) {
            val target = res.resolve("drawable-nodpi/${tile.nameWithoutExtension}.webp")
            if (upToDate(target, master.resolve("tiles/${tile.name}"))) continue
            writeWebp(scaled(upscaled(tile), 800, 800), target)
        }
        icons()
    }

    private fun emit(asset: Asset, m: BufferedImage, buckets: Map<String, Double>, dp: Int) {
        for ((bucket, scale) in buckets) {
            val target = res.resolve("$bucket/${asset.name}.webp")
            if (upToDate(target, master.resolve("${asset.name}.png"))) continue
            val px = (dp * scale).roundToInt()
            writeWebp(if (asset.byWidth) scaled(m, px, -1) else scaled(m, -1, px), target)
        }
    }

    private fun background(name: String, width: Int) {
        val target = res.resolve("drawable-nodpi/$name.webp")
        if (upToDate(target, master.resolve("$name.png"))) return
        writeWebp(scaled(upscaled(original.resolve("$name.png")), width, -1), target)
    }

    private val iconDp = intArrayOf(48, 72, 96, 144, 192)
    private val foregroundDp = intArrayOf(108, 162, 216, 324, 432)
    private val innerDp = intArrayOf(72, 108, 144, 216, 288)
    private val mipmaps = listOf("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")

    /** Launcher icons for every density plus the Play Store listing icon. */
    private fun icons() {
        val m = upscaled(original.resolve("app_icon.png"))
        mipmaps.forEachIndexed { i, dir ->
            writePng(scaled(m, iconDp[i], iconDp[i]), res.resolve("$dir/ic_launcher.png"))
            // Adaptive foreground: the icon covers the 72dp safe zone of the 108dp canvas.
            val fg = BufferedImage(foregroundDp[i], foregroundDp[i], BufferedImage.TYPE_INT_ARGB)
            val g = fg.createGraphics()
            val offset = (foregroundDp[i] - innerDp[i]) / 2
            g.drawImage(scaled(m, innerDp[i], innerDp[i]), offset, offset, null)
            g.dispose()
            writePng(fg, res.resolve("$dir/ic_launcher_foreground.png"))
        }
        writePng(scaled(m, 512, 512), root.resolve("art/store/play-icon-512.png")) // Play Console: 512 x 512 32-bit PNG
    }

    /** 4x Real-ESRGAN result, cached in art/master. */
    private fun upscaled(source: File): BufferedImage {
        val out = master.resolve(source.relativeTo(original).path)
        if (!out.exists()) {
            out.parentFile.mkdirs()
            run(realesrgan.path, "-i", source.path, "-o", out.path, "-n", "realesrgan-x4plus-anime", "-s", "4", "-m", realesrgan.parentFile.resolve("models").path)
            log("upscaled ${source.relativeTo(original)}")
        }
        return ImageIO.read(out)
    }

    /** High-quality downscale: halve until close, then one bicubic step. A negative side keeps the aspect ratio. */
    private fun scaled(src: BufferedImage, width: Int, height: Int): BufferedImage {
        val w = if (width < 0) (src.width.toDouble() * height / src.height).roundToInt() else width
        val h = if (height < 0) (src.height.toDouble() * width / src.width).roundToInt() else height
        var current = src
        while (current.width >= w * 2 && current.height >= h * 2) current = step(current, current.width / 2, current.height / 2)
        return if (current.width == w && current.height == h) current else step(current, w, h)
    }

    private fun step(src: BufferedImage, w: Int, h: Int): BufferedImage {
        val dst = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = dst.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.composite = AlphaComposite.Src
        g.drawImage(src, 0, 0, w, h, null)
        g.dispose()
        return dst
    }

    private fun writeWebp(image: BufferedImage, target: File) {
        target.parentFile.mkdirs()
        val tmp = File.createTempFile("art", ".png")
        try {
            ImageIO.write(image, "png", tmp)
            run(cwebp.path, "-quiet", "-q", "92", "-alpha_q", "100", "-m", "6", "-exact", tmp.path, "-o", target.path)
        } finally {
            tmp.delete()
        }
    }

    private fun writePng(image: BufferedImage, target: File) {
        target.parentFile.mkdirs()
        ImageIO.write(image, "png", target)
    }

    private fun upToDate(target: File, source: File) = target.exists() && target.lastModified() > source.lastModified()

    private fun run(vararg command: String) {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "${command.joinToString(" ")}\n$output" }
    }
}
