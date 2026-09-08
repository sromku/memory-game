# Art brief: new worlds, stickers, a pet

What to generate (OpenAI image models or anything else) so it drops into the art pipeline without
retouching. Everything is generated on a computer and committed to `art/original`; the app never
generates or fetches anything.

## The style to match

Look at `art/original/tiles/animals_1.png`, `mosters_3.png` and `emoji_7.png` before generating
anything, and keep a few of them in the prompt as references.

- Flat, rounded, cartoon shapes; no gradients beyond a soft one, no outlines, no texture.
- One character per picture, centred, facing the viewer, standing on nothing (transparent
  background), with a small soft drop shadow under it, as the existing ones have.
- Big round eyes with a dark pupil: the tracer tags eyes and pupils by shape and the app blinks
  them, so eyes must be plain filled shapes, not sparkles or lines. A character can have no eyes
  (a fruit, a car) and then simply does not blink.
- Two to four flat colours per character, saturated, in the palette of the theme.
- Square, 400 × 400 px, PNG with alpha, the character filling about 80% of the height.

## A new world

A world is a card set, a background and a name. Deliver:

1. **Characters**: 28 or more square PNGs as above, named `<world>_1.png` … in
   `art/original/tiles/`. Generate 40 or 50 and keep the best 28 to 36: what matters is that they
   read as one family (same eye style, same roundness, same shadow), not any single picture. Each
   should be tellable from the others at 60 px, since "Who was here?" shows them small.
2. **Background**: one PNG, 1024 × 512 (2:1), the world's scenery, with a flat ground band across the
   bottom: its top edge, where feet stand, at 78 to 86% of the height, all the way across, and
   nothing important in the top 15% (the clock and prompts sit there). Named `back_<world>.png`.
   Look at `back_animals.png` for the ground band.
3. **Theme card**: not needed. The card is the existing blue panel with a ribbon; a small tool will
   place a crop of the background with three characters inside it, as the current cards look.
4. **A name** in English; the twenty translations are added in `tools/i18n`.

Worlds that suit four-year-olds and the existing families: ocean (fish, crab, octopus, whale),
farm vehicles, fruits and vegetables with faces, dinosaurs, space (planets and rockets with faces),
insects, sweets.

## Stickers

One per road, so six per world, earned when a road is finished: `sticker_<world>_<level>.png`,
512 × 512, PNG with alpha. A round badge or a rosette in the world's colours with one character or
symbol of the world in the middle, no text. The six should form a visible series (bronze to gold,
or one star to six).

## A pet that grows

One companion for the menu, in four stages, `pet_1.png` … `pet_4.png`, 400 × 400 as characters:
an egg, a hatchling, a young one, a grown one. Same character across the four, in the animals
family style. It stands on the menu grass and changes stage at star milestones.

## Generating with the tool

`tools/generate-world.py` did the ocean world: one call per character with three animal tiles as
style references and a transparent background, one call for the scenery with the animals
background as reference, at 1024 px, resized to the pipeline's sizes. `tools/compose-theme-card.py`
then made the theme's cards from the animals cards' frame with a crop of the new background and
three of the new characters inside. The key is read from a file outside the repository.

## Checking a batch before it enters the pipeline

Put the PNGs in a folder and run `./gradlew regenerateArt` with the tracer; open the traced
characters in the album (they appear as shadows first) or in a round. Reject a character whose
eyes were tagged wrong (it will not blink or will blink a spot); `art/character-overrides.txt`
can mark a character as eyeless. Reject anything that looks like a different family next to the
others on the menu grass.
