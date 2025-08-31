#!/bin/bash
set -e

RED="\033[0;31m"
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
BLUE="\033[0;34m"
NC="\033[0m"

COMPILE_SDK_VERSION="36"
BUILD_TOOLS_VERSION="36.0.0"
CMAKE_VERSION="3.22.1"
ANDROID_NDK_VERSION="28.2.13676358"
CMDLINE_VERSION="13114758"

PLATFORM="$(uname -s)"
case "$PLATFORM" in
  Darwin*)  PLATFORM_NAME="darwin"; ANDROID_HOME="$HOME/Library/Android/sdk" ;;
  Linux*)   PLATFORM_NAME="linux";  ANDROID_HOME="$HOME/Android/Sdk" ;;
  MINGW*|MSYS*|CYGWIN*) PLATFORM_NAME="windows"; ANDROID_HOME="$HOME/AppData/Local/Android/Sdk" ;;
  *) echo -e "${RED}[✗] Unsupported platform: $PLATFORM${NC}"; exit 1 ;;
esac

TOOLS_DIR="$ANDROID_HOME/cmdline-tools"
TOOLS_BIN="$TOOLS_DIR/latest/bin"

log()    { echo -e "${BLUE}[*]${NC} $1"; }
success(){ echo -e "${GREEN}[✓]${NC} $1"; }
warn()   { echo -e "${YELLOW}[!]${NC} $1"; }
error()  { echo -e "${RED}[✗]${NC} $1"; exit 1; }

PURGE=false
INSTALL=false
UNINSTALL=false
CLEAR_MAIN_CCACHE=false
CLEAR_LOCAL_CCACHE=false
CLEAR_GRADLE=false
CLEAR_ANDROID=false

confirm_purge() {
    echo ""
    echo -ne "${YELLOW}[!] Script interrupted. Do you want to purge the SDK at $ANDROID_HOME? (y/N): ${NC}"
    read -r answer
    case "$answer" in
        [yY]|[yY][eE][sS])
            log "Purging SDK..."
            if [ -d "$ANDROID_HOME" ]; then
                log "Purging existing Android SDK at $ANDROID_HOME..."
                if rm -rf "$ANDROID_HOME"; then
                    success "SDK purged."
                else
                    error "Failed to purge SDK!"
                fi
            else
                warn "No SDK found to purge."
            fi
            ;;
        *)
            warn "SDK not purged."
            ;;
    esac
    exit 1
}

trap confirm_purge INT

show_help() {
    echo -e "${BLUE}Android SDK Setup Script${NC}"
    echo
    echo -e "Usage: $0 [OPTIONS]"
    echo
    echo -e "${YELLOW}Options:${NC}"
    echo -e "  --help                 Show this help message and exit"
    echo -e "  --install              Install Command Line Tools, Build Tools, NDK and CMake"
    echo -e "  --uninstall            Delete the Android SDK completely"
    echo -e "  --purge                Delete SDK before installation (combine with --install)"
    echo -e "  --clear-main-ccache    Clear main ccache"
    echo -e "  --clear-local-ccache   Clear local ccache"
    echo -e "  --clear-gradle         Clear ~/.gradle"
    echo -e "  --clear-android        Clear ~/.android"
    echo
    echo -e "Examples:"
    echo -e "  $0 --install"
    echo -e "  $0 --purge --install"
    echo -e "  $0 --uninstall"
    echo -e "  $0 --clear-main-ccache --clear-local-ccache"
    exit 0
}

if [ $# -eq 0 ]; then
    echo -e "${RED}[✗] Error: at least one argument is required.${NC}"
    echo -e "Use '--help' to see available options."
    exit 1
fi

for arg in "$@"; do
    case $arg in
        --help) show_help ;;
        --purge) PURGE=true ;;
        --install) INSTALL=true ;;
        --uninstall) UNINSTALL=true ;;
        --clear-main-ccache) CLEAR_MAIN_CCACHE=true ;;
        --clear-local-ccache) CLEAR_LOCAL_CCACHE=true ;;
        --clear-gradle) CLEAR_GRADLE=true ;;
        --clear-android) CLEAR_ANDROID=true ;;
    esac
done

log "Creating SDK directories..."
mkdir -p "$TOOLS_DIR"

if [ "$UNINSTALL" = true ]; then
    if [ -d "$ANDROID_HOME" ]; then
        log "Uninstalling Android SDK at $ANDROID_HOME..."
        if rm -rf "$ANDROID_HOME"; then
            success "SDK uninstalled."
        else
            error "Failed to uninstall SDK!"
        fi
    else
        warn "No SDK found to uninstall."
    fi
    exit 0
fi

if [ "$CLEAR_MAIN_CCACHE" = true ]; then
    log "Clearing main ccache..."
    if ccache -C; then
        success "Main ccache cleared."
    else
        warn "Failed to clear main ccache."
    fi
fi

if [ "$CLEAR_LOCAL_CCACHE" = true ]; then
    log "Clearing local ccache..."
    if ccache -c; then
        success "Local ccache cleared."
    else
        warn "Failed to clear local ccache."
    fi
fi

if [ "$CLEAR_GRADLE" = true ]; then
    log "Clearing Gradle config..."
    if rm -rf "$HOME/.gradle"; then
        success "Gradle config cleared."
    else
        warn "Failed to clear Gradle config."
    fi
fi

if [ "$CLEAR_ANDROID" = true ]; then
    log "Clearing Android config..."
    if rm -rf "$HOME/.android"; then
        success "Android config cleared."
    else
        warn "Failed to clear Android config."
    fi
fi


if [ "$INSTALL" = false ]; then
    log "No install action taken. Use '--install' to install Android SDK components."
    exit 0
fi

FORCE_INSTALL=true
if [ -d "$TOOLS_DIR/latest" ]; then
    echo -ne "${YELLOW}[!] Command Line Tools already present. Do you want to reinstall? (y/N): ${NC}"
    read -r answer
    case "$answer" in
        [yY]|[yY][eE][sS]) FORCE_INSTALL=true ;;
        *) FORCE_INSTALL=false ;;
    esac
fi

[ "$PURGE" = true ] && [ -d "$ANDROID_HOME" ] && { log "Purging existing Android SDK at $ANDROID_HOME..."; rm -rf "$ANDROID_HOME" || error "Failed to purge SDK!"; success "SDK purged successfully."; }

if [ "$FORCE_INSTALL" = true ]; then
    log "Downloading Command Line Tools for $PLATFORM_NAME..."
    ZIPFILE="/tmp/cmdline-tools.zip"
    URL="https://dl.google.com/android/repository/commandlinetools-${PLATFORM_NAME}-${CMDLINE_VERSION}_latest.zip"

    if command -v curl >/dev/null 2>&1; then
        curl -L "$URL" -o "$ZIPFILE" || error "Download with curl failed!"
    else
        wget -O "$ZIPFILE" "$URL" || error "Download with wget failed!"
    fi

    log "Recreating tools directory..."
    mkdir -p "$TOOLS_DIR"

    log "Extracting to $TOOLS_DIR..."
    unzip -qo "$ZIPFILE" -d "$TOOLS_DIR" || error "Extraction failed!"
    rm -f "$ZIPFILE"

    [ -d "$TOOLS_DIR/cmdline-tools" ] && mv "$TOOLS_DIR/cmdline-tools" "$TOOLS_DIR/latest"
    success "Command Line Tools installed successfully!"
else
    log "Skipping Command Line Tools installation."
fi

log "Accepting licenses..."
yes | "$TOOLS_BIN/sdkmanager" --licenses > /dev/null

log "Updating SDK components..."
"$TOOLS_BIN/sdkmanager" --update

log "Installing SDK, Build Tools, NDK, and CMake..."
"$TOOLS_BIN/sdkmanager" --install \
    "platforms;android-$COMPILE_SDK_VERSION" \
    "build-tools;$BUILD_TOOLS_VERSION" \
    "ndk;$ANDROID_NDK_VERSION" \
    "cmake;$CMAKE_VERSION"

test -d "$ANDROID_HOME/ndk/$ANDROID_NDK_VERSION" || warn "NDK not found."

success "Setup complete! Android SDK ready at $ANDROID_HOME"

warn "Remember to add to your shell profile:"
echo -e "  export ANDROID_HOME=$ANDROID_HOME"
echo -e "  export PATH=\$PATH:$TOOLS_BIN"
