#!/usr/bin/env bash
# Lists the signing keystore and checks passwords without printing them. See scripts/KeystoreCheck.java.
#   scripts/check-keystore.sh [keystore]                 list certificates
#   scripts/check-keystore.sh [keystore] --try file.txt  test one password guess per line
#   scripts/check-keystore.sh [keystore] --props         check MEMORY_GAME_* in ~/.gradle/gradle.properties
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/java}"
[[ -x "$JAVA" ]] || JAVA="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"
[[ -x "$JAVA" ]] || JAVA=java
KS=""
if [[ $# -gt 0 && "$1" != --* ]]; then KS="$1"; shift; fi
[[ -n "$KS" ]] || KS="$(sed -n 's/^MEMORY_GAME_KEYSTORE=//p' ~/.gradle/gradle.properties 2>/dev/null)"
[[ -n "$KS" ]] || { echo "usage: $0 <keystore> [--try file | --props]" >&2; exit 2; }
if [[ "${1:-}" == "--props" ]]; then set -- --props ~/.gradle/gradle.properties; fi
exec "$JAVA" "$ROOT/scripts/KeystoreCheck.java" "$KS" "$@"
