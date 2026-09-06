#!/usr/bin/env bash
# Rebuilds every bitmap resource from art/original with an AI upscaler. See scripts/art/RegenerateArt.java.
#
# Needs:
#   * Real-ESRGAN ncnn (binary + models folder next to it):
#       https://github.com/xinntao/Real-ESRGAN/releases  (realesrgan-ncnn-vulkan-*-macos.zip)
#     pass its path as REALESRGAN=/path/to/realesrgan-ncnn-vulkan
#   * cwebp (brew install webp)
#   * a JDK 17+ (Android Studio's works: JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home")
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${REALESRGAN:?set REALESRGAN to the realesrgan-ncnn-vulkan binary}"
CWEBP="${CWEBP:-$(command -v cwebp)}"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/java}"; [[ -x "$JAVA" ]] || JAVA="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"; [[ -x "$JAVA" ]] || JAVA=java
exec "$JAVA" "$ROOT/scripts/art/RegenerateArt.java" "$ROOT" "$REALESRGAN" "$CWEBP"
