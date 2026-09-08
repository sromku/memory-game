#!/usr/bin/env python3
"""Makes a theme's card pictures from the animals cards' frame: the scene inside the frame is
replaced by a crop of the theme's background with three of its characters standing on the ground.
Writes art/original/<theme>_theme_star_<0..3>.png. Usage, from the repository root:

    python3 tools/compose-theme-card.py ocean 0.78 ocean_1 ocean_3 ocean_6

The second argument is the background's ground line (fraction of its height); then three tiles."""
import sys
from PIL import Image

SCENE = (0.087, 0.18, 0.908, 0.566)   # the picture inside the card frame, as fractions of the card


def main():
    theme, ground = sys.argv[1], float(sys.argv[2])
    tiles = sys.argv[3:6]
    background = Image.open(f'art/original/back_{theme}.png').convert('RGBA')
    for stars in range(4):
        card = Image.open(f'art/original/animals_theme_star_{stars}.png').convert('RGBA')
        W, H = card.size
        left, top, right, bottom = int(SCENE[0] * W), int(SCENE[1] * H), int(SCENE[2] * W), int(SCENE[3] * H)
        sw, sh = right - left, bottom - top
        # a crop of the background with the scene's shape, its bottom at the picture's bottom
        bw, bh = background.size
        crop_h = int(bw * sh / sw)
        scene = background.crop((0, bh - crop_h, bw, bh)).resize((sw, sh), Image.LANCZOS)
        # three characters on the ground, feet on the ground line
        ground_y = int((ground * bh - (bh - crop_h)) * sh / crop_h)
        size = int(sh * 0.62)
        for i, name in enumerate(tiles):
            tile = Image.open(f'art/original/tiles/{name}.png').convert('RGBA').resize((size, size), Image.LANCZOS)
            x = int(sw * (0.2 + 0.3 * i)) - size // 2
            scene.alpha_composite(tile, (x, ground_y - int(size * 0.9)))
        card.paste(scene, (left, top))
        card.save(f'art/original/{theme}_theme_star_{stars}.png')
        print(f'{theme}_theme_star_{stars}.png')


if __name__ == '__main__':
    main()
