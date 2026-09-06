# Releasing to Google Play

## What Play expects

The app on the store is `com.snatik.matches`, last uploaded as versionCode **1007** (versionName
1.01.001007) in February 2019. The Play Console export shows the app is **not enrolled in Play App
Signing**: the APK on the store is signed directly with the developer's own key. That has two
consequences for every update:

1. The new build must be signed with **exactly that key**. A different key is rejected by Play and,
   even if it slipped through, would fail to install on users' devices.
2. Because the app is not enrolled, uploads are **APKs**, not app bundles. (Enrolling in Play App
   Signing is possible by uploading the existing key; after that the AAB can be used. Both artifacts
   are built by the script below.)

`versionCode` in `app/build.gradle.kts` is 1008 and must go up with every upload.

## Signing configuration

`app/build.gradle.kts` reads four Gradle properties. Leave them empty in the repository's
`gradle.properties` and set the real values in `~/.gradle/gradle.properties`, which is never
committed:

```
MEMORY_GAME_KEYSTORE=/absolute/path/to/your.keystore
MEMORY_GAME_STORE_PASSWORD=...
MEMORY_GAME_KEY_ALIAS=...
MEMORY_GAME_KEY_PASSWORD=...
```

They can also be passed as environment variables named `ORG_GRADLE_PROJECT_MEMORY_GAME_KEYSTORE`
and so on. When the keystore property is empty the release build is produced unsigned.

The names are deliberately prefixed with `MEMORY_GAME_` so that the generic `KEYSTORE` /
`KEY_ALIAS` properties used by other projects on the same machine can never sign this app by accident.

## Which key?

The release keystore is the one whose only entry is alias `memory game` (note the space), created on
15 November 2014, the day before the first upload of this app. Its certificate:

```
owner:   CN=Roman Kushnarenko, OU=sromku
SHA-256: DA:AB:54:AB:2D:10:CF:C8:81:F0:65:C7:7C:00:76:B5:BF:80:A0:14:BE:57:34:6E:D5:E2:7B:87:BF:C0:B8:D0
SHA-1:   22:37:7C:54:8A:59:26:70:6E:67:FD:0B:D1:B5:25:34:A1:DE:C0:DA
```

Keep it (and a backup) outside the repository or in `keystore/`, which git ignores. Losing it means
Play's key reset process, which requires enrolling in Play App Signing.

`./gradlew checkKeystore` lists a keystore's certificates without a password, checks the configured
`MEMORY_GAME_*` properties, and with `-Pguesses=/path/to/guesses.txt` tests one password guess per
line without ever echoing them.

## Verifying a release

```
scripts/verify-release.sh                 # build, check manifest, print signer fingerprint
scripts/verify-release.sh --old v1007.apk # also compare against the previous release and
                                          # install it, then upgrade in place on the connected device
```

The previous APK can be pulled from any device that still has the store version installed:

```
adb shell pm path com.snatik.matches
adb pull /data/app/.../base.apk v1007.apk
```

The script copies the artifacts to `release/` (ignored by git) named after the version.

## Player data

Stars and best times are stored in `SharedPreferences` file `com.snatik.matches` under the keys
`theme_<themeId>_difficulty_<level>` and `themetime_<themeId>_difficultytime_<level>`, exactly as
in version 1007, so players keep their progress. `GamePreferencesKeysTest` guards these formats.
Backup to Google Drive and device-to-device transfer are enabled for that file.

## Play Console: getting the listing back

The app was removed on 13 November 2019 for "not adhering to Google Play Developer Program
policies" (no more specific reason is shown), and it is additionally flagged for targeting API 28
instead of the required API 35 / 36. The build in this repository targets API 36, which clears the
two target-API items as soon as a new version is published. The removal itself is cleared by
publishing a compliant version; if the console still refuses, use "Submit an appeal" on the same
page (5 to 8 days).

Complete every section under **Policy** > **App content** before creating the release. What to
answer for this app:

- **Privacy policy**: `https://sromku.github.io/memory-game/privacy.html`, served by GitHub Pages
  from the `docs/` folder (repository Settings > Pages > Deploy from branch, `master`, `/docs`).
  Required because the audience includes children; the app links to the same page from its
  settings popup, as the Families policy asks.
- **Ads**: no, the app contains no ads.
- **App access**: all functionality is available without special access.
- **Content rating**: fill the IARC questionnaire as a game with no violence, no user interaction,
  no purchases; expect "Everyone" / PEGI 3.
- **Target audience and content**: the app is for children; select the age groups including under 13
  and older. This enrols the app in the Families policy: the app has no ads, no third-party SDKs,
  no data collection and no external links except the Play Store, which complies. Do not tick
  "appeals to children" as an accident of the store listing text; declare it as intended.
- **Data safety**: no data collected, no data shared. The game progress stays on the device.
- **Government apps, Financial features, Health, News**: no.
- **App category**: Games > Educational (as before).

Then **Test and release** > **Production** > **Create new release**, upload
`release/memory-game-<version>-<code>.apk` produced by `scripts/verify-release.sh`, add release
notes, and submit for review. Review of a previously removed app can take longer than usual.
