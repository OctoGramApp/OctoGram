#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if command -v ccache >/dev/null 2>&1; then
  CCACHE_BIN="$(command -v ccache)"
else
  OS_TYPE=$(uname)
  BASE_DIR="$(cd "$SCRIPT_DIR/../ccache" && pwd)"

  case "$OS_TYPE" in
    Linux)
      CCACHE_BIN="$BASE_DIR/linux/ccache"
      ;;
    Darwin)
      CCACHE_BIN="$BASE_DIR/macOs/ccache"
      ;;
    MINGW*|MSYS*|CYGWIN*|Windows_NT)
      CCACHE_BIN="$BASE_DIR/windows/ccache.exe"
      ;;
    *)
      echo "Unsupported OS: $OS_TYPE"
      exit 1
      ;;
  esac

  if [ ! -x "$CCACHE_BIN" ]; then
    echo "Error: ccache binary not found or not executable at $CCACHE_BIN"
    exit 1
  fi
fi

echo "Using ccache binary: $CCACHE_BIN"
"$CCACHE_BIN" -c -C

PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

DIRS=(
  "$PROJECT_ROOT/.kotlin"
  "$PROJECT_ROOT/.gradle"
  "$PROJECT_ROOT/TMessagesProj_App/build"
  "$PROJECT_ROOT/build"
  "$PROJECT_ROOT/TMessagesProj/.cxx"
  "$PROJECT_ROOT/TMessagesProj/build"
)

if [ "$OS_TYPE" != "Darwin" ]; then
  DIRS+=("$PROJECT_ROOT/.idea")
fi

for dir in "${DIRS[@]}"; do
  [ -d "$dir" ] && find "$dir" -type f -exec rm -f {} + && find "$dir" -depth -type d -exec rmdir {} +
done
