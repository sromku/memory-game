# Memory Game

Android game for children (Google Play target audience: 5 and under, 6-8, 9-12). Kotlin, AndroidX,
Gradle Kotlin DSL. See README.md for layout and docs/RELEASE.md for signing and Play Console.

## Children's-app compliance: non-negotiable

The Play listing declares the app for children and certifies COPPA and GDPR compliance under the
Families policy. Every change must keep the following true. The `checkChildSafety` task fails the
build when it can detect a violation; the rest is judgement.

- No personal data is collected, stored off-device, or shared. The only persisted data is game
  progress and the sound switch, in SharedPreferences.
- No network access. The manifest declares no permissions at all; adding one (INTERNET included)
  is a product decision that needs a policy review first.
- No third-party SDKs: no analytics, crash reporting, advertising, attribution, social login, or
  "engagement" libraries. AndroidX and Kotlin libraries only.
- No ads, no in-app purchases, no links except the Play Store page and the privacy policy.
- No user-generated content, chat, or account of any kind.
- Any feature that would change the above also changes the Play Console declarations, the Data
  safety form, and the privacy policy at https://sromku.com/memory-game/privacy.html, and must be
  done together with them.

## Working notes

- Signing material lives only in ~/.gradle/gradle.properties as MEMORY_GAME_* properties. Never
  print or commit it. `./gradlew checkKeystore` verifies it.
- Bitmaps are generated from art/original by `./gradlew regenerateArt`; do not edit files under
  app/src/main/res/drawable-* or mipmap-* by hand.
- Preference keys and file name in GamePreferences are a compatibility contract with the 2019
  release; a test guards them.
- Run the game on the MemoryGame_* emulators only, on a fixed port with ANDROID_SERIAL set.
