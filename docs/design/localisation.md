# Localisation

The app ships in English and twenty more languages: Spanish, Brazilian Portuguese, French, German,
Italian, Russian, Ukrainian, Polish, Dutch, Turkish, Arabic, Hindi, Indonesian, Vietnamese, Thai,
Japanese, Korean, Traditional and Simplified Chinese, and Malay. The game's settings popup has a
Language row that lists them by their own names (`AppLanguages`, `PopupLanguageView`); the choice
goes through AppCompat's per-app locales, which Android 13+ also shows under Settings > Apps >
Memory Game > Language (`res/xml/locales_config.xml`) and older versions store for the app.

## Strings

The English source is `app/src/main/res/values/strings.xml` and `phrases.xml`; number-only
formats are marked not translatable. Every other locale is generated: the translations live in
`tools/i18n/translations_*.py` (one dictionary per language: strings, the plural for a finished
round, the 100 phrases the menu animals say, the 7 daily greetings) and
`tools/i18n/generate.py` writes `values-<locale>/strings.xml` and `phrases.xml`. Edit the
Python, run the generator, never the generated XML. Animal sounds are adapted per language
("Woof" is "Ouaf" in French, "Гав" in Russian, "ワン" in Japanese).

Lint requires every locale to translate every string, so a new string needs twenty entries
before the build passes.

## Fonts

Grobold, the game's display font, has Latin letters only. `@font/game` is an alias resolved per
language: Grobold for English, Indonesian and Malay; Rubik Black for the other Latin, Cyrillic
and Arabic languages; Baloo 2 ExtraBold for Hindi and Vietnamese; Mitr Bold for Thai. Chinese,
Japanese and Korean glyphs come from the system font, emboldened through the `GameText` style
and the `game_text_fake_bold` flag so they match the weight of the rest. The added fonts are
under the SIL Open Font License (`docs/licenses`).

## Layout

`supportsRtl` is on and layouts use start/end, so Arabic mirrors the menu buttons and the popup
rows. Custom views (the road, the party scene, the bubbles) keep their left-to-right geometry,
which is right for a road and a row of characters.

## Still in English: the art

Words baked into pictures are not translated yet: the title, the "play" tooltip, the difficulty
names (Beginner … Master), the theme names on the cards, "Settings", "Level Completed", "Time",
"Score" and "BEST". They are the next step of the localisation work.

## The art (done)

The pictures carry no lettering any more. `tools/erase-lettering.py` keeps the lettered originals
in `art/original/lettered/` and writes de-lettered ones to `art/original/` for the pipeline; the
app writes the words at runtime with `LabeledDrawable`, placed by fractions of each picture
(difficulty names, theme names, "Settings", "Language", "Level completed", "Time", "Score",
"play"). Their strings are `difficulty_name_*`, `theme_*`, `settings`, `level_completed`,
`time`, `score`, `play`; keep translations short, they shrink to fit but small text is worse.

The title is the one picture drawn per language: `tools/generate-titles.py` renders
`art/original/title-<locale>.png` in the English title's style from the `title` entry of the
translation data, and the art pipeline emits `drawable-<locale>-nodpi/title.webp`. The English
title stays the hand-made original.
