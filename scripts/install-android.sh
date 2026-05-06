#!/usr/bin/env bash
# Drive in Car — Android 빌드 + 실기기 재설치 스크립트 (Linux/macOS/Git-Bash)
#
# 동작은 install-android.ps1 와 동일.
#
# 사용:
#   ./scripts/install-android.sh             # 기본 (debug)
#   ./scripts/install-android.sh --launch    # 설치 후 자동 실행
#   ./scripts/install-android.sh --release   # release 빌드
#   ./scripts/install-android.sh -s <serial> # 특정 기기 지정

set -euo pipefail

LAUNCH=0
RELEASE=0
DEVICE_SERIAL=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --launch) LAUNCH=1; shift ;;
        --release) RELEASE=1; shift ;;
        -s|--serial) DEVICE_SERIAL="$2"; shift 2 ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
done

step() { printf '\033[36m==> %s\033[0m\n' "$*"; }
warn() { printf '\033[33mWARN: %s\033[0m\n' "$*"; }
fail() { printf '\033[31mERROR: %s\033[0m\n' "$*"; exit 1; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="$REPO_ROOT/android"
APP_DIR="$ANDROID_DIR/app"

# 1. JDK
find_jdk() {
    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then echo "$JAVA_HOME"; return; fi
    for c in \
        "/c/Program Files/Android/Android Studio/jbr" \
        "$HOME/Library/Application Support/JetBrains/Toolbox/apps/AndroidStudio/ch-0/*/Android Studio.app/Contents/jbr/Contents/Home" \
        "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
        "/usr/lib/jvm/java-17-openjdk-amd64"; do
        if [[ -x "$c/bin/java" ]]; then echo "$c"; return; fi
    done
}
JDK="$(find_jdk || true)"
if [[ -z "$JDK" ]]; then fail "JDK 17+ not found. Install Android Studio or set JAVA_HOME."; fi
export JAVA_HOME="$JDK"
step "JAVA_HOME=$JAVA_HOME"

# 2. SDK + adb
find_sdk() {
    for c in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" \
             "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" \
             "$LOCALAPPDATA/Android/Sdk" \
             "/c/Users/$USER/AppData/Local/Android/Sdk"; do
        if [[ -n "$c" && -d "$c" ]]; then echo "$c"; return; fi
    done
}
SDK="$(find_sdk || true)"
if [[ -z "$SDK" ]]; then fail "Android SDK not found. Install Android Studio."; fi
export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
ADB="$SDK/platform-tools/adb"
[[ -x "$ADB" ]] || ADB="$SDK/platform-tools/adb.exe"
[[ -x "$ADB" ]] || fail "adb not found in $SDK/platform-tools"
step "ANDROID_HOME=$ANDROID_HOME"

# 3. local.properties
if [[ ! -f "$ANDROID_DIR/local.properties" ]]; then
    step "Creating android/local.properties"
    {
        echo "sdk.dir=$(echo "$SDK" | sed 's,\\,\\\\,g; s,:,\\:,g')"
        echo "MAPS_API_KEY="
    } > "$ANDROID_DIR/local.properties"
    warn "local.properties 생성됨. MAPS_API_KEY 를 설정해주세요."
fi

# 4. google-services.json
if [[ ! -f "$APP_DIR/google-services.json" ]]; then
    cat <<EOF >&2

google-services.json 이 android/app/ 에 없습니다.
Firebase Console (drive-3c0fd) → Android 앱 등록 (패키지명: com.driveincar) → 다운로드 후
  $APP_DIR/google-services.json
에 저장한 뒤 다시 실행하세요.
EOF
    exit 1
fi

# 5. 빌드
if [[ $RELEASE -eq 1 ]]; then VARIANT="Release"; else VARIANT="Debug"; fi
TASK=":app:assemble$VARIANT"
APK="$APP_DIR/build/outputs/apk/$(echo "$VARIANT" | tr '[:upper:]' '[:lower:]')/app-$(echo "$VARIANT" | tr '[:upper:]' '[:lower:]').apk"

step "Gradle: $TASK"
( cd "$ANDROID_DIR" && ./gradlew "$TASK" )
[[ -f "$APK" ]] || fail "APK not found at $APK"
step "Built: $APK"

# 6. 기기 확인
DEVICES=$("$ADB" devices | awk '/\tdevice$/ { print $1 }')
if [[ -z "$DEVICES" ]]; then
    cat <<EOF >&2

연결된 기기가 없습니다.
  - USB 케이블 연결
  - [개발자 옵션 → USB 디버깅] 활성화
  - [USB 디버깅 허용] 다이얼로그 수락
을 확인하세요.
EOF
    exit 1
fi

if [[ -n "$DEVICE_SERIAL" ]]; then
    if ! grep -qx "$DEVICE_SERIAL" <<<"$DEVICES"; then fail "Device $DEVICE_SERIAL not connected"; fi
    TARGET="$DEVICE_SERIAL"
else
    COUNT=$(wc -l <<<"$DEVICES")
    if [[ "$COUNT" -gt 1 ]]; then
        echo "여러 기기 연결됨:"
        echo "$DEVICES" | sed 's/^/  - /'
        fail "--serial <serial> 로 명시해주세요"
    fi
    TARGET="$DEVICES"
fi
step "Target device: $TARGET"

# 7. Uninstall + install
PKG=$([[ $RELEASE -eq 1 ]] && echo "com.driveincar" || echo "com.driveincar.debug")
if "$ADB" -s "$TARGET" shell pm list packages "$PKG" | grep -q "$PKG"; then
    step "기존 앱($PKG) 발견 → uninstall"
    "$ADB" -s "$TARGET" uninstall "$PKG" > /dev/null
else
    step "기존 앱 없음 — 신규 설치"
fi

step "Installing: $APK"
"$ADB" -s "$TARGET" install -r "$APK"

if [[ $LAUNCH -eq 1 ]]; then
    step "Launching $PKG/.MainActivity"
    "$ADB" -s "$TARGET" shell am start -n "$PKG/com.driveincar.MainActivity" > /dev/null
fi

printf '\033[32m✓ 설치 완료\033[0m\n'
