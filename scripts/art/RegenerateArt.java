import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Regenerates every bitmap resource from the originals in art/original:
 *
 *   1. Real-ESRGAN (anime model) upscales each original 4x into art/master (git-ignored).
 *   2. Each UI asset is rendered at the pixel size it is displayed at, for every density bucket,
 *      for phones (from its dp size in values/dimens.xml) and tablets (values-sw720dp/dimens.xml).
 *   3. Card pictures and backgrounds go to drawable-nodpi at 2x / 3x their original size; the app
 *      decodes them sub-sampled to the size actually shown.
 *   4. Everything is written as WebP (lossy, quality 92, lossless alpha), which is a third the
 *      size of PNG for this flat artwork.
 *
 * Usage (see scripts/regenerate-art.sh): java RegenerateArt.java <repo root> <realesrgan binary> <cwebp>
 */
public class RegenerateArt {

    /** name, phone dp, tablet dp, whether the dp is the width or the height. */
    record Asset(String name, int phoneDp, int tabletDp, boolean byWidth) {}

    static final List<Asset> UI = new ArrayList<>(List.of(
            new Asset("title", 400, 800, true),
            new Asset("button_start", 100, 200, true),
            new Asset("button_start_lights", 150, 300, true),
            new Asset("button_settings", 80, 160, true),
            new Asset("tooltip_play", 80, 160, true),
            new Asset("time_bar", 120, 140, true),
            new Asset("level_complete", 240, 400, true),
            new Asset("level_complete_star", 57, 95, true),
            new Asset("settings_popup", 300, 400, true),
            new Asset("button_back", 75, 120, true),
            new Asset("button_again", 75, 120, true),
            new Asset("button_music_on", 50, 65, false),
            new Asset("button_music_off", 50, 65, false),
            new Asset("button_rate", 50, 65, false),
            new Asset("tile_back_star", 90, 90, true)));

    static {
        for (String theme : List.of("animals", "monsters", "emoji"))
            for (int s = 0; s <= 3; s++) UI.add(new Asset(theme + "_theme_star_" + s, 300, 360, true));
        for (int d = 1; d <= 6; d++)
            for (int s = 0; s <= 3; s++) UI.add(new Asset("button_difficulty_" + d + "_star_" + s, 260, 340, true));
    }

    static final Map<String, Double> PHONE = new LinkedHashMap<>();
    static final Map<String, Double> TABLET = new LinkedHashMap<>();
    static {
        PHONE.put("drawable-mdpi", 1.0); PHONE.put("drawable-hdpi", 1.5); PHONE.put("drawable-xhdpi", 2.0);
        PHONE.put("drawable-xxhdpi", 3.0); PHONE.put("drawable-xxxhdpi", 4.0);
        TABLET.put("drawable-sw600dp-mdpi", 1.0); TABLET.put("drawable-sw600dp-hdpi", 1.5);
        TABLET.put("drawable-sw600dp-xhdpi", 2.0); TABLET.put("drawable-sw600dp-xxhdpi", 3.0);
    }

    static Path root, original, master, res;
    static String esrgan, cwebp;

    public static void main(String[] args) throws Exception {
        root = Paths.get(args[0]).toAbsolutePath();
        esrgan = args[1];
        cwebp = args[2];
        original = root.resolve("art/original");
        master = root.resolve("art/master");
        res = root.resolve("app/src/main/res");
        Files.createDirectories(master);

        for (Asset asset : UI) {
            BufferedImage m = upscaled(original.resolve(asset.name + ".png"));
            emit(asset, m, PHONE, asset.phoneDp);
            emit(asset, m, TABLET, asset.tabletDp);
        }

        // Backgrounds: the largest screens are 2560 px wide; 3x of 1024 covers them, 2x of 1880 too.
        writeWebp(scaled(upscaled(original.resolve("background.png")), 3760, -1), res.resolve("drawable-nodpi/background.webp"));
        writeWebp(scaled(upscaled(original.resolve("back_animals.png")), 3072, -1), res.resolve("drawable-nodpi/back_animals.webp"));
        writeWebp(scaled(upscaled(original.resolve("back_horror.png")), 3072, -1), res.resolve("drawable-nodpi/back_horror.webp"));

        // Card pictures: shown up to ~660 px on a 10-inch tablet; 800 px keeps them sharp there.
        try (DirectoryStream<Path> tiles = Files.newDirectoryStream(original.resolve("tiles"), "*.png")) {
            for (Path tile : tiles) {
                String name = tile.getFileName().toString().replace(".png", "");
                writeWebp(scaled(upscaled(tile), 800, 800), res.resolve("drawable-nodpi/" + name + ".webp"));
            }
        }
        System.out.println("done");
    }

    static void emit(Asset asset, BufferedImage m, Map<String, Double> buckets, int dp) throws Exception {
        for (Map.Entry<String, Double> bucket : buckets.entrySet()) {
            int px = (int) Math.round(dp * bucket.getValue());
            BufferedImage out = asset.byWidth ? scaled(m, px, -1) : scaled(m, -1, px);
            writeWebp(out, res.resolve(bucket.getKey() + "/" + asset.name + ".webp"));
        }
    }

    /** 4x Real-ESRGAN result, cached in art/master. */
    static BufferedImage upscaled(Path source) throws Exception {
        Path out = master.resolve(original.relativize(source));
        if (!Files.exists(out)) {
            Files.createDirectories(out.getParent());
            Path modelDir = Paths.get(esrgan).toAbsolutePath().getParent().resolve("models");
            run(esrgan, "-i", source.toString(), "-o", out.toString(), "-n", "realesrgan-x4plus-anime", "-s", "4", "-m", modelDir.toString());
            System.out.println("upscaled " + original.relativize(source));
        }
        return ImageIO.read(out.toFile());
    }

    /** High-quality downscale: halve with area averaging until close, then one bicubic step. */
    static BufferedImage scaled(BufferedImage src, int width, int height) {
        if (width < 0) width = (int) Math.round((double) src.getWidth() * height / src.getHeight());
        if (height < 0) height = (int) Math.round((double) src.getHeight() * width / src.getWidth());
        BufferedImage cur = src;
        while (cur.getWidth() >= width * 2 && cur.getHeight() >= height * 2) {
            cur = step(cur, cur.getWidth() / 2, cur.getHeight() / 2);
        }
        return cur.getWidth() == width && cur.getHeight() == height ? cur : step(cur, width, height);
    }

    static BufferedImage step(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    static void writeWebp(BufferedImage image, Path target) throws Exception {
        Files.createDirectories(target.getParent());
        Path tmp = Files.createTempFile("art", ".png");
        ImageIO.write(image, "png", tmp.toFile());
        run(cwebp, "-quiet", "-q", "92", "-alpha_q", "100", "-m", "6", "-exact", tmp.toString(), "-o", target.toString());
        Files.delete(tmp);
    }

    static void run(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String log = new String(p.getInputStream().readAllBytes());
        if (p.waitFor() != 0) throw new IllegalStateException(String.join(" ", cmd) + "\n" + log);
    }
}
