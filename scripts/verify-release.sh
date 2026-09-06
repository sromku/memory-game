#!/usr/bin/env bash
#
# Builds the signed release artifacts and checks everything Google Play needs to accept them as an
# update of the published app (versionCode 1007, signed with the developer's own key):
#
#   * package name unchanged and versionCode higher than the last upload
#   * the APK/AAB are signed, and the signer certificate is printed for comparison with Play Console
#   * with --old <apk>: the previous release's certificate must match, and, if a device or emulator is
#     connected, the old APK is installed and the new one installed over it (the same check the
#     Play Store performs on users' devices)
#
# Signing material comes from Gradle properties MEMORY_GAME_KEYSTORE, MEMORY_GAME_STORE_PASSWORD,
# MEMORY_GAME_KEY_ALIAS and MEMORY_GAME_KEY_PASSWORD. Put them in ~/.gradle/gradle.properties, or
# export them as ORG_GRADLE_PROJECT_<name> environment variables. See docs/RELEASE.md.
set -euo pipefail
# The build tools run on whichever java is on PATH; keep JDK 24+ native-access warnings quiet.
export JAVA_OPTS="${JAVA_OPTS:-} --enable-native-access=ALL-UNNAMED"

LAST_PUBLISHED_VERSION_CODE=1007
PACKAGE="com.snatik.matches"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$(sed -n 's/^sdk.dir=//p' "$ROOT/local.properties" 2>/dev/null || true)}}"
[[ -d "$SDK" ]] || { echo "Android SDK not found; set ANDROID_HOME or sdk.dir in local.properties" >&2; exit 1; }
BUILD_TOOLS="$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)"
APKSIGNER="$BUILD_TOOLS/apksigner"
AAPT2="$BUILD_TOOLS/aapt2"
ADB="$SDK/platform-tools/adb"

OLD_APK=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --old) OLD_APK="$2"; shift 2 ;;
    *) echo "usage: $0 [--old previous-release.apk]" >&2; exit 2 ;;
  esac
done

pass() { echo "PASS  $*"; }
fail() { echo "FAIL  $*" >&2; FAILED=1; }
FAILED=0

cd "$ROOT"
echo "== Building signed release APK and AAB"
./gradlew --quiet :app:assembleRelease :app:bundleRelease

APK="app/build/outputs/apk/release/app-release.apk"
AAB="app/build/outputs/bundle/release/app-release.aab"
[[ -f "$APK" ]] || { echo "No signed APK at $APK. Is MEMORY_GAME_KEYSTORE configured?" >&2; exit 1; }

echo "== Manifest"
BADGING="$("$AAPT2" dump badging "$APK")"
NAME="$(sed -n "s/^package: name='\([^']*\)'.*/\1/p" <<<"$BADGING")"
CODE="$(sed -n "s/^package: .*versionCode='\([0-9]*\)'.*/\1/p" <<<"$BADGING")"
VERSION="$(sed -n "s/^package: .*versionName='\([^']*\)'.*/\1/p" <<<"$BADGING")"
TARGET="$(sed -n "s/^targetSdkVersion:'\([0-9]*\)'.*/\1/p" <<<"$BADGING")"
echo "package=$NAME versionCode=$CODE versionName=$VERSION targetSdk=$TARGET"
[[ "$NAME" == "$PACKAGE" ]] && pass "package name is $PACKAGE" || fail "package name is $NAME, expected $PACKAGE"
[[ "$CODE" -gt "$LAST_PUBLISHED_VERSION_CODE" ]] && pass "versionCode $CODE > $LAST_PUBLISHED_VERSION_CODE" || fail "versionCode $CODE must exceed $LAST_PUBLISHED_VERSION_CODE"

echo "== Signature"
NEW_CERT="$("$APKSIGNER" verify --print-certs "$APK" | sed -n 's/.*SHA-256 digest: //p' | head -1)"
"$APKSIGNER" verify "$APK" && pass "APK signature verifies" || fail "APK signature does not verify"
echo "signer SHA-256: $NEW_CERT"
echo "Compare with Play Console > Test and release > Setup > App signing (or App integrity):"
echo "the 'App signing key certificate' SHA-256 must be identical, otherwise Play rejects the upload."

if [[ -n "$OLD_APK" ]]; then
  echo "== Previous release: $OLD_APK"
  OLD_BADGING="$("$AAPT2" dump badging "$OLD_APK")"
  OLD_CODE="$(sed -n "s/^package: .*versionCode='\([0-9]*\)'.*/\1/p" <<<"$OLD_BADGING")"
  OLD_CERT="$("$APKSIGNER" verify --print-certs "$OLD_APK" | sed -n 's/.*SHA-256 digest: //p' | head -1)"
  echo "old versionCode=$OLD_CODE signer SHA-256: $OLD_CERT"
  [[ "$OLD_CERT" == "$NEW_CERT" ]] && pass "signed with the same key as the previous release" || fail "signing key differs from the previous release"
  [[ "$CODE" -gt "$OLD_CODE" ]] && pass "versionCode $CODE > previous $OLD_CODE" || fail "versionCode $CODE is not above previous $OLD_CODE"

  if "$ADB" get-state >/dev/null 2>&1; then
    echo "== Upgrade on connected device"
    "$ADB" uninstall "$PACKAGE" >/dev/null 2>&1 || true
    "$ADB" install "$OLD_APK" >/dev/null && pass "previous release installed"
    if "$ADB" install -r "$APK" >/dev/null; then
      INSTALLED="$("$ADB" shell dumpsys package "$PACKAGE" | sed -n 's/.*versionCode=\([0-9]*\).*/\1/p' | head -1)"
      [[ "$INSTALLED" == "$CODE" ]] && pass "upgraded in place to versionCode $INSTALLED" || fail "device reports versionCode $INSTALLED after upgrade"
    else
      fail "installing the new APK over the previous release failed (see adb output above)"
    fi
  else
    echo "(no device connected; skipping the on-device upgrade)"
  fi
fi

echo "== Artifacts"
OUT="release"
mkdir -p "$OUT"
cp "$APK" "$OUT/memory-game-$VERSION-$CODE.apk"
cp "$AAB" "$OUT/memory-game-$VERSION-$CODE.aab"
ls -la "$OUT"

[[ "$FAILED" -eq 0 ]] && echo "All checks passed." || { echo "Some checks failed." >&2; exit 1; }
