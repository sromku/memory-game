# Memory Game

A simple memory game for kids aged 4+. No ads, no accounts, no complicated screens: pick a theme,
pick a difficulty, find the pairs before the clock runs out.

<p align="center">
  <img src="docs/screenshots/menu.png" width="49%" alt="Main menu" />
  <img src="docs/screenshots/themes.png" width="49%" alt="Theme selection" />
  <img src="docs/screenshots/game.png" width="49%" alt="Playing the animals theme" />
  <img src="docs/screenshots/level-complete.png" width="49%" alt="Level completed with three stars" />
</p>

- 3 themes: Animals, Monsters and Emojis
- 6 difficulties, from 3x2 up to 10x5 cards
- Stars and best times per level, sounds on winning

## The story

**2014 to 2019.** The game was written in Java, released on Google Play as
[Memory Game (Free)](https://play.google.com/store/apps/details?id=com.snatik.matches) and published
here as open source. The last release was version 1.01.001007 in February 2019.

**2019 to 2026.** Maintenance stopped. Google Play removed the app in November 2019 for not keeping
up with its developer policies, and the code sat still while Android moved on by twelve API levels.

**2026.** The revival. The whole app was converted from Java to Kotlin, restructured, debugged,
tested and prepared for release purely with AI (Claude Code), with the original artwork and gameplay
kept intact. It is on its way back to the store.

## Building

Standard Android Studio project: Kotlin, AndroidX, Gradle Kotlin DSL, target SDK 36, min SDK 24.

```
./gradlew :app:assembleDebug        # debug build, installs next to the store version
./gradlew :app:testDebugUnitTest    # game-logic unit tests
./gradlew :app:lintDebug            # lint, warnings are errors
./gradlew checkKeystore             # verify the signing keystore configuration
scripts/verify-release.sh           # signed release APK + AAB with pre-upload checks
```

Signing and the Play Store checklist are in [docs/RELEASE.md](docs/RELEASE.md). Signing material
never lives in this repository.

## Artwork

The originals live in `art/original`. Every bitmap under `app/src/main/res`, the launcher icon and
the Play Store icon (`art/store`) are generated from them by a Gradle task: a Real-ESRGAN pass (the
anime model, which suits this flat cartoon style) upscales each original 4x, then each asset is
rendered at the exact pixel size it is shown at for every density bucket, phones and tablets
separately, and saved as WebP. Edit or add an original, rerun, commit the result.

```
brew install webp
# download realesrgan-ncnn-vulkan for macOS from https://github.com/xinntao/Real-ESRGAN/releases
./gradlew regenerateArt -Prealesrgan=/path/to/realesrgan-ncnn-vulkan
```

## Code layout

- `game/` pure Kotlin rules: `Difficulty`, `Board` (shuffled pairs), `GameEngine` (flip state machine), `GameResult` (stars and score)
- `data/GamePreferences` best stars and times, key-compatible with the 2019 release
- `ui/GameViewModel` the round in progress, its clock, and the timing of every effect
- `ui/...` one fragment per screen, the board and tile views, the popups
- `audio/SoundPlayer` sound effects through `SoundPool`

## License

- Code: [Apache License 2.0](./LICENSE)
- Artwork is licensed separately from its authors:
  - http://graphicriver.net/item/animals-collection-farm-and-domestic-set/7177721
  - http://graphicriver.net/item/monster-creation-kit-and-large-pack/8851390
  - http://graphicriver.net/item/10-fresh-game-backgrounds/9137937
  - http://graphicriver.net/item/cartoon-games-gui-pack-11-/6056785
