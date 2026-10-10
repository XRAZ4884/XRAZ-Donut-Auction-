#!/usr/bin/env sh
# Lightweight Gradle bootstrapper for environments where the binary wrapper JAR is unavailable.
set -eu
VERSION="8.14.3"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}/donut-auction-bootstrap/gradle-$VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/donut-auction-bootstrap"
  mkdir -p "$BASE"
  ARCHIVE="$BASE/gradle-$VERSION-bin.zip"
  if [ ! -f "$ARCHIVE" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
      wget "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -O "$ARCHIVE"
    else
      echo "Install curl or wget to download Gradle $VERSION." >&2; exit 1
    fi
  fi
  unzip -q -o "$ARCHIVE" -d "$BASE"
fi
exec "$GRADLE_BIN" "$@"
