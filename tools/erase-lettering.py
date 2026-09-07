#!/usr/bin/env python3
"""Takes the English words out of the UI art so the app can draw them in any language.

The lettered originals are kept in art/original/lettered/; the de-lettered pictures are written
to art/original/ for the art pipeline. The words are filled in by diffusion from their
surroundings, which keeps the panel gradients and the ribbon shading intact. Needs numpy and
Pillow; run from the repository root."""
import os, shutil
import numpy as np
from PIL import Image, ImageFilter

ORIGINAL = 'art/original'
LETTERED = os.path.join(ORIGINAL, 'lettered')

# Boxes that hold words, as fractions of width and height: (left, top, right, bottom), and what
# lies behind the letters there: a blue panel or a red ribbon. Everything in the box that is not
# that background is lettering.
BOXES = {
    'tooltip_play': [((0.2, 0.12, 0.8, 0.56), 'blue')],
    'settings_popup': [((0.25, 0.04, 0.75, 0.18), 'red')],
    'level_complete': [((0.12, 0.02, 0.88, 0.115), 'red'), ((0.08, 0.42, 0.395, 0.53), 'flat'), ((0.07, 0.545, 0.385, 0.67), 'flat')],
}
for theme in ('animals', 'monsters', 'emoji'):
    for stars in range(4):
        BOXES[f'{theme}_theme_star_{stars}'] = [((0.2, 0.03, 0.8, 0.135), 'red')]
for level in range(1, 7):
    for stars in range(4):
        BOXES[f'button_difficulty_{level}_star_{stars}'] = [((0.1, 0.14, 0.9, 0.43), 'blue')]


def background(rgb, kind, inside=None):
    r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    if kind == 'blue':
        return (b > 195) & (r < 195)
    if kind == 'flat':
        # a flat area: the background is the colour of the box's edge, letters are anything far from it
        edge = inside & ~np.array(Image.fromarray((inside * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(7))).astype(bool)
        ref = np.median(rgb[edge], axis=0)
        return np.sqrt(((rgb - ref) ** 2).sum(axis=-1)) < 40
    return (r > 195) & (g < 140) & (b < 100)


def erase(name):
    src = os.path.join(LETTERED, f'{name}.png')
    if not os.path.exists(src):
        shutil.move(os.path.join(ORIGINAL, f'{name}.png'), src)
    im = Image.open(src).convert('RGBA')
    a = np.array(im).astype(np.float32)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    box = np.zeros((h, w), bool)
    mask = np.zeros((h, w), bool)
    for (l, t, r, b), kind in BOXES[name]:
        inside = (xx >= l * w) & (xx < r * w) & (yy >= t * h) & (yy < b * h)
        box |= inside
        mask |= inside & ~background(a[..., :3], kind, inside)
    grown = np.array(Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(7))) > 0
    mask = grown & box & (a[..., 3] > 0)
    rgb = a[..., :3].copy()
    for _ in range(3000):
        nb = (np.roll(rgb, 1, 0) + np.roll(rgb, -1, 0) + np.roll(rgb, 1, 1) + np.roll(rgb, -1, 1)) / 4
        rgb[mask] = nb[mask]
    a[..., :3] = rgb
    Image.fromarray(a.astype(np.uint8), 'RGBA').save(os.path.join(ORIGINAL, f'{name}.png'))
    print(f'{name}: erased {int(mask.sum())} px')


if __name__ == '__main__':
    os.makedirs(LETTERED, exist_ok=True)
    for name in BOXES:
        erase(name)
