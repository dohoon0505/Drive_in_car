# Drive in Car — Android 빌드 + 실기기 재설치 스크립트 (Windows PowerShell)
#
# 동작:
#   1. JDK / Android SDK / adb 위치 자동 탐지
#   2. android/local.properties 가 없으면 생성 (sdk.dir 자동 채움)
#   3. google-services.json 존재 확인 (없으면 안내 후 종료)
#   4. ./gradlew :app:assembleDebug 실행
#   5. 연결된 기기 목록 확인 (없으면 종료)
#   6. com.driveincar.debug 가 이미 있으면 uninstall
#   7. 새 APK install -r
#   8. (옵션) 앱 자동 실행
#
# 사용:
#   pwsh ./scripts/install-android.ps1            # 기본
#   pwsh ./scripts/install-android.ps1 -Launch    # 설치 후 자동 실행
#   pwsh ./scripts/install-android.ps1 -Release   # release 빌드 (서명 필요)

[CmdletBinding()]
param(
    [switch]$Launch,
    [switch]$Release,
    [string]$DeviceSerial
)

$ErrorActionPreference = 'Stop'

# 화면에 단계마다 잘 보이도록
function Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }
function Warn($msg) { Write-Host "WARN: $msg" -ForegroundColor Yellow }
function Fail($msg) { Write-Host "ERROR: $msg" -ForegroundColor Red; exit 1 }

# 1. 위치 결정 ----------------------------------------------------------------
$RepoRoot = Resolve-Path "$PSScriptRoot\.."
$AndroidDir = Join-Path $RepoRoot 'android'
$AppDir = Join-Path $AndroidDir 'app'

if (-not (Test-Path $AndroidDir)) { Fail "android/ not found at $AndroidDir" }

# 2. JDK ----------------------------------------------------------------------
function Find-Jdk {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        return $env:JAVA_HOME
    }
    $candidates = @(
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files\Android\Android Studio1\jbr",
        "$env:LOCALAPPDATA\Android\Sdk\jbr",
        "C:\Program Files\Eclipse Adoptium\jdk-17"
    )
    foreach ($c in $candidates) {
        if (Test-Path "$c\bin\java.exe") { return $c }
    }
    return $null
}

$jdk = Find-Jdk
if (-not $jdk) { Fail "JDK 17+ not found. Install Android Studio (bundled JBR) or set JAVA_HOME." }
$env:JAVA_HOME = $jdk
Step "JAVA_HOME = $jdk"

# 3. Android SDK + adb --------------------------------------------------------
function Find-Sdk {
    $candidates = @(
        $env:ANDROID_HOME,
        $env:ANDROID_SDK_ROOT,
        "$env:LOCALAPPDATA\Android\Sdk",
        "C:\Android\Sdk"
    )
    foreach ($c in $candidates) {
        if ($c -and (Test-Path $c)) { return $c }
    }
    return $null
}

$sdkDir = Find-Sdk
if (-not $sdkDir) { Fail "Android SDK not found. Install Android Studio." }
$env:ANDROID_HOME = $sdkDir
$env:ANDROID_SDK_ROOT = $sdkDir
$adb = Join-Path $sdkDir 'platform-tools\adb.exe'
if (-not (Test-Path $adb)) { Fail "adb not found at $adb. Install platform-tools via Android Studio SDK Manager." }
Step "ANDROID_HOME = $sdkDir"
Step "adb = $adb"

# 4. local.properties ---------------------------------------------------------
$localProps = Join-Path $AndroidDir 'local.properties'
if (-not (Test-Path $localProps)) {
    Step "Creating android/local.properties"
    $sdkEsc = $sdkDir.Replace('\', '\\').Replace(':', '\:')
    @"
sdk.dir=$sdkEsc
# Maps API key (필수): https://console.cloud.google.com/google/maps-apis 에서 발급
# 비워두면 지도가 회색 빈 화면으로 나옴.
MAPS_API_KEY=
"@ | Set-Content -Path $localProps -Encoding utf8
    Warn "local.properties 생성됨. MAPS_API_KEY 를 설정해주세요. 비어있으면 지도가 회색으로 보입니다."
}

# 5. google-services.json -----------------------------------------------------
$gs = Join-Path $AppDir 'google-services.json'
if (-not (Test-Path $gs)) {
    Write-Host ""
    Write-Host "google-services.json 이 android/app/ 에 없습니다." -ForegroundColor Red
    Write-Host "Firebase Console (drive-3c0fd) → Android 앱 등록 (패키지명: com.driveincar) → 다운로드 후" -ForegroundColor Red
    Write-Host "  $gs" -ForegroundColor Red
    Write-Host "에 저장한 뒤 이 스크립트를 다시 실행하세요." -ForegroundColor Red
    exit 1
}

# 6. 빌드 ---------------------------------------------------------------------
$variant = if ($Release) { 'Release' } else { 'Debug' }
$task = ":app:assemble$variant"
$apkPath = Join-Path $AppDir "build\outputs\apk\$($variant.ToLower())\app-$($variant.ToLower()).apk"

Step "Gradle build: $task"
Push-Location $AndroidDir
try {
    if ($IsWindows -or $env:OS -eq 'Windows_NT') {
        & cmd /c ".\gradlew.bat $task"
    } else {
        & ./gradlew $task
    }
    if ($LASTEXITCODE -ne 0) { Fail "Gradle build failed (exit $LASTEXITCODE)" }
} finally {
    Pop-Location
}

if (-not (Test-Path $apkPath)) { Fail "APK not found at $apkPath" }
Step "Built: $apkPath"

# 7. 기기 확인 ----------------------------------------------------------------
# adb devices 출력에서 시리얼만 추출. @(...) 로 강제 배열화 — 1개일 때 string으로
# 떨어지면 [0]이 첫 글자가 되어버린다 (시리얼 "R5KL..." → "R" 버그).
$rawDevices = & $adb devices
$devices = @(
    $rawDevices |
        Select-String -Pattern '^([A-Za-z0-9._:-]+)\s+device$' |
        ForEach-Object { $_.Matches[0].Groups[1].Value }
)

if ($devices.Count -eq 0) {
    Write-Host ""
    Write-Host "연결된 기기가 없습니다." -ForegroundColor Red
    Write-Host "  - USB 케이블이 연결되어 있는지" -ForegroundColor Red
    Write-Host "  - 기기에서 [개발자 옵션 → USB 디버깅] 이 켜져 있는지" -ForegroundColor Red
    Write-Host "  - 처음 연결한 기기라면 화면에 뜬 [USB 디버깅 허용] 다이얼로그를 수락했는지" -ForegroundColor Red
    Write-Host '확인하세요. (직접 확인: adb devices)' -ForegroundColor Red
    exit 1
}

if ($DeviceSerial) {
    if ($devices -notcontains $DeviceSerial) { Fail "Device $DeviceSerial 가 연결 목록에 없습니다." }
    $target = $DeviceSerial
} elseif ($devices.Count -eq 1) {
    $target = $devices[0]
} else {
    Write-Host "여러 기기 연결됨:"
    $devices | ForEach-Object { Write-Host "  - $_" }
    Fail "-DeviceSerial <serial> 로 명시해주세요."
}

Step "Target device: $target"

# 8. Uninstall (이미 있으면) --------------------------------------------------
# debug/release 모두 동일 패키지(com.driveincar)를 사용한다.
# 추후 dev/prod 분리 시 build.gradle.kts 의 applicationIdSuffix 를 켜고 여기도 분기.
#
# `pm path` 가 빈 출력이면 미설치, APK 경로가 나오면 기존 설치됨.
# (Samsung Knox 등 일부 기기는 `pm list packages` 가 권한 오류를 내므로 path 사용.)
$pkg = 'com.driveincar'
$pmPath = & $adb -s $target shell pm path $pkg 2>$null
if ($pmPath -and ($pmPath -match 'package:')) {
    Step "기존 앱($pkg) 발견 → uninstall"
    $uninstallOut = & $adb -s $target uninstall $pkg 2>&1
    if ($LASTEXITCODE -ne 0 -or $uninstallOut -notmatch 'Success') {
        Warn "uninstall 결과: $uninstallOut (install -r 로 덮어쓰기 시도)"
    }
} else {
    Step "기존 앱 없음 — 신규 설치"
}

# 9. Install ------------------------------------------------------------------
Step "Installing: $apkPath"
& $adb -s $target install -r $apkPath
if ($LASTEXITCODE -ne 0) { Fail "Install failed (exit $LASTEXITCODE)" }

# 10. (옵션) 자동 실행 --------------------------------------------------------
if ($Launch) {
    Step "Launching: $pkg/.MainActivity"
    & $adb -s $target shell am start -n "$pkg/com.driveincar.MainActivity" | Out-Null
}

Write-Host ""
Write-Host "✓ 설치 완료" -ForegroundColor Green
