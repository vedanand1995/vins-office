#!/bin/sh
# Bootstrap gradlew — needs no gradle-wrapper.jar.
set -e
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# Read distributionUrl and strip the escape backslashes (https\:// -> https://)
DIST_URL=$(grep "^distributionUrl" "$PROPS" | cut -d= -f2- | tr -d "\\")

CACHE_DIR="$HOME/.gradle-bootstrap"
DIST_NAME=$(basename "$DIST_URL")
DIST_DIR="$CACHE_DIR/${DIST_NAME%.zip}"

if [ ! -x "$DIST_DIR/bin/gradle" ]; then
  mkdir -p "$CACHE_DIR"
  echo "Downloading $DIST_URL ..."
  curl -fsSL -o "$CACHE_DIR/$DIST_NAME" "$DIST_URL"
  rm -rf "$DIST_DIR"
  unzip -q "$CACHE_DIR/$DIST_NAME" -d "$CACHE_DIR"
fi

exec "$DIST_DIR/bin/gradle" "$@"
