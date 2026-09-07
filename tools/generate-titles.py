#!/usr/bin/env python3
"""Draws the title picture ("Memory Game / for kids") for every language in the style of the
English original (art/original/title.png): heavy slanted letters, an orange gradient with
diagonal hatching, a near-black outline and a drop shadow, and a smaller white second line.

Writes art/original/title-<locale>.png, which the art pipeline turns into
drawable-<locale>-<density>/title.webp. The words come from tools/i18n/translations_*.py
(the "title" entry: big line, small line). Fonts: the app's own for Latin, Cyrillic, Arabic,
Devanagari and Thai; Noto Sans CJK Black for Chinese, Japanese and Korean, downloaded to the
directory given with --fonts (from https://github.com/google/fonts, ofl/notosans{jp,kr,sc,tc}).
Needs Pillow and numpy; run from the repository root."""
import argparse, glob, importlib.util, math, os
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
os.chdir(ROOT)
ORIGINAL = 'art/original'
W, H = 780, 235            # the English title's size; every language keeps it
S = 4                      # supersampling

FILL_TOP, FILL_BOTTOM = (253, 184, 40), (214, 96, 36)
HATCH = (216, 90, 8)
OUTLINE = (26, 5, 0)
SHADOW = (60, 20, 10)
SMALL_FILL = (255, 250, 240)

APP_FONTS = 'app/src/main/res/font'
FONT_FOR = {
    'hi': f'{APP_FONTS}/baloo2_extrabold.ttf', 'vi': f'{APP_FONTS}/baloo2_extrabold.ttf',
    'th': f'{APP_FONTS}/mitr_bold.ttf',
    'id': f'{APP_FONTS}/grobold.ttf', 'ms': f'{APP_FONTS}/grobold.ttf',
}
CJK = {'ja': 'NotoSansJP.ttf', 'ko': 'NotoSansKR.ttf', 'zh-rCN': 'NotoSansSC.ttf', 'zh-rTW': 'NotoSansTC.ttf'}


def font_path(locale, fonts_dir):
    if locale in CJK:
        return os.path.join(fonts_dir, CJK[locale])
    return FONT_FOR.get(locale, f'{APP_FONTS}/rubik_black.ttf')


def load_font(path, size):
    font = ImageFont.truetype(path, size)
    try:
        font.set_variation_by_axes([900])   # the variable Noto fonts: heaviest weight
    except Exception:
        pass
    return font


def text_mask(text, font, skew, rise):
    """A mask of the text, sheared to lean like the original and rotated slightly upward."""
    bbox = font.getbbox(text)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    pad = th
    canvas = Image.new('L', (tw + 2 * pad, th + 2 * pad), 0)
    ImageDraw.Draw(canvas).text((pad - bbox[0], pad - bbox[1]), text, font=font, fill=255)
    sheared = canvas.transform(canvas.size, Image.AFFINE, (1, skew, -skew * canvas.height / 2, 0, 1, 0), resample=Image.BICUBIC)
    return sheared.rotate(rise, resample=Image.BICUBIC, expand=True)


def fit_mask(text, font_file, max_w, max_h, skew, rise):
    """The largest font size whose sheared letters fit the box (the small upward tilt is applied after)."""
    size = max_h
    while size > 8:
        flat = text_mask(text, load_font(font_file, size), skew, 0)
        box = flat.getbbox()
        if box and box[2] - box[0] <= max_w and box[3] - box[1] <= max_h:
            tilted = text_mask(text, load_font(font_file, size), skew, rise)
            return tilted.crop(tilted.getbbox())
        size = int(size * 0.96)
    tilted = text_mask(text, load_font(font_file, size), skew, rise)
    return tilted.crop(tilted.getbbox())


def paint(mask, top, bottom, hatch, outline_px, shadow_px, hatch_lines):
    """Fills a mask with a vertical gradient (and hatching), outlines it and drops a shadow."""
    w, h = mask.size
    pad = outline_px + shadow_px + 4
    layer = Image.new('RGBA', (w + 2 * pad, h + 2 * pad), (0, 0, 0, 0))
    m = Image.new('L', layer.size, 0)
    m.paste(mask, (pad, pad))
    outline = m.filter(ImageFilter.MaxFilter(outline_px * 2 + 1))
    shadow = Image.new('RGBA', layer.size, SHADOW + (255,))
    shadow.putalpha(outline)
    layer.alpha_composite(shadow, (shadow_px, shadow_px))
    dark = Image.new('RGBA', layer.size, OUTLINE + (255,))
    dark.putalpha(outline)
    layer.alpha_composite(dark)
    grad = np.zeros((layer.size[1], layer.size[0], 4), np.uint8)
    t = np.linspace(0, 1, layer.size[1])[:, None]
    for c in range(3):
        grad[..., c] = (top[c] * (1 - t) + bottom[c] * t).astype(np.uint8)
    grad[..., 3] = 255
    fill = Image.fromarray(grad, 'RGBA')
    if hatch_lines:
        d = ImageDraw.Draw(fill)
        step = max(6, h // 14)
        for x in range(-layer.size[1], layer.size[0] + layer.size[1], step):
            d.line([(x, 0), (x + layer.size[1], layer.size[1])], fill=hatch + (110,), width=max(2, step // 5))
    fill.putalpha(m)
    layer.alpha_composite(fill)
    return layer


RTL = {'ar'}


def render(locale, big, small, fonts_dir):
    file = font_path(locale, fonts_dir)
    canvas = Image.new('RGBA', (W * S, H * S), (0, 0, 0, 0))
    rtl = locale in RTL
    skew, rise = (0.16, -3.0) if rtl else (-0.16, 3.0)   # the lean and the tilt follow the reading direction
    # The English original: the big line fills the height and about three quarters of the width, the
    # small line sits under its right end. Long translations get narrower letters, never a third line.
    big_mask = fit_mask(big.upper() if locale not in CJK else big, file, int(W * S * 0.96), int(H * S * 0.62), skew, rise)
    big_layer = paint(big_mask, FILL_TOP, FILL_BOTTOM, HATCH, outline_px=max(4, big_mask.height // 22), shadow_px=max(5, big_mask.height // 16), hatch_lines=True)
    small_mask = fit_mask(small.upper() if locale not in CJK else small, file, int(W * S * 0.5), int(H * S * 0.25), skew, rise)
    small_layer = paint(small_mask, SMALL_FILL, SMALL_FILL, HATCH, outline_px=max(2, small_mask.height // 20), shadow_px=max(3, small_mask.height // 14), hatch_lines=False)
    # The small line tucks under the big one (overlapping a little, as in the original). The pair is
    # composed on a roomy canvas, then the whole is scaled to fill the title box, so every language
    # is as large as it can be and nothing is ever cut off.
    overlap = int(small_layer.height * 0.22)
    room = Image.new('RGBA', (W * S * 2, H * S * 3), (0, 0, 0, 0))
    big_x = room.width - big_layer.width - W * S // 2 if rtl else W * S // 2
    room.alpha_composite(big_layer, (big_x, H * S))
    small_x = (big_x + int(big_layer.width * (0.38 if rtl else 0.62))) - small_layer.width // 2
    room.alpha_composite(small_layer, (small_x, H * S + big_layer.height - overlap))
    block = room.crop(room.getbbox())
    scale = min(W * S * 0.98 / block.width, H * S * 0.98 / block.height)
    block = block.resize((max(1, int(block.width * scale)), max(1, int(block.height * scale))), Image.LANCZOS)
    canvas.alpha_composite(block, ((W * S - block.width) // 2, (H * S - block.height) // 2))
    out = canvas.resize((W, H), Image.LANCZOS)
    out.save(os.path.join(ORIGINAL, f'title-{locale}.png'))
    print('title', locale, big, '/', small)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--fonts', default=os.path.expanduser('~/.cache/memory-game-fonts'), help='directory with the Noto Sans CJK fonts')
    parser.add_argument('locales', nargs='*')
    args = parser.parse_args()
    data = {}
    for path in sorted(glob.glob('tools/i18n/translations_*.py')):
        spec = importlib.util.spec_from_file_location('t', path)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        data.update(mod.LANGS)
    for locale, t in data.items():
        if locale == 'en' or (args.locales and locale not in args.locales):
            continue  # English keeps the hand-made original
        big, small = t['title']
        render(locale, big, small, args.fonts)


if __name__ == '__main__':
    main()
