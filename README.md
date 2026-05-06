# Drive in Car

차량을 사랑하고 와인딩(굽이굽이 산길 드라이빙)을 즐기는 사람들을 위한 네이티브 모바일 앱.

큐레이션된 와인딩 코스의 시작점에서 끝점까지의 주행 시간을 자동으로 측정하여 랭킹을 매기는 MVP.

## 기술 스택

- **Android**: Kotlin + Jetpack Compose + Hilt + Coroutines + Maps Compose
- **iOS**: Swift + SwiftUI + Google Maps SDK + CoreLocation
- **Backend**: Firebase (Auth + Firestore + Storage), 프로젝트 ID `drive-3c0fd`

## 저장소 구조

```
.
├── android/         # Android 네이티브 앱 (Kotlin)
├── ios/             # iOS 네이티브 앱 (Swift)
├── firebase/        # Firebase 보안 규칙 / 인덱스 / 시드 스크립트
├── scripts/         # 빌드/설치 자동화 스크립트
├── .github/         # CI 워크플로우
└── docs/            # 아키텍처, 스키마, 레이스 상태머신 문서
```

---

## 빠른 시작 — Android 실기기 설치

### 1회: Firebase + Maps API 키

1. [Firebase Console](https://console.firebase.google.com/project/drive-3c0fd) → **프로젝트 설정 → 내 앱 → Android 앱 추가**
   - 패키지명: `com.driveincar`
   - 다운로드한 `google-services.json` → [android/app/](android/app/) 에 저장
2. [Google Maps Platform](https://console.cloud.google.com/google/maps-apis) 에서 **Maps SDK for Android** 활성화 → API 키 발급
3. [android/local.properties](android/local.properties) 에 키 추가 (스크립트 첫 실행 시 자동 생성됨):
   ```
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   MAPS_API_KEY=AIzaSy...실제키
   ```
4. Firebase Console → **Authentication → 이메일/비밀번호 활성화**

### 매번: 빌드 + 재설치

기기를 USB로 연결하고 USB 디버깅을 켠 뒤:

```powershell
# Windows
pwsh ./scripts/install-android.ps1            # 빌드 → 기존 앱 제거 → 재설치
pwsh ./scripts/install-android.ps1 -Launch    # 설치 후 자동 실행
```

```bash
# macOS / Linux / Git-Bash
./scripts/install-android.sh
./scripts/install-android.sh --launch
```

스크립트가 자동으로 처리하는 작업:

1. JDK 17+ 자동 탐지 (Android Studio 번들 JBR 우선)
2. Android SDK / adb 위치 자동 탐지
3. `android/local.properties` 없으면 생성 (`sdk.dir` 자동 채움)
4. `google-services.json` 누락 시 명확한 안내 후 종료
5. `./gradlew :app:assembleDebug` 실행
6. 연결된 기기 확인 (없으면 USB 디버깅 안내 후 종료)
7. **`com.driveincar.debug` 가 이미 있으면 자동 uninstall**
8. 새 APK install
9. `-Launch` / `--launch` 옵션 시 앱 자동 실행

여러 기기가 연결되어 있을 때:

```powershell
pwsh ./scripts/install-android.ps1 -DeviceSerial RFXXXXXX
```

---

## Firebase 규칙/인덱스 배포 + 시드

```bash
cd firebase
npx firebase login
npx firebase deploy --only firestore:rules,firestore:indexes,storage
```

코스/메타 시드:

```bash
cd firebase/seed
npm install
# Firebase Console → 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성 → ../serviceAccountKey.json 으로 저장
npm run seed
```

---

## iOS

iOS는 Xcode 프로젝트(`.xcodeproj`)가 binary metadata를 포함하므로 자동 생성하지 않는다. [ios/README.md](ios/README.md) 참고하여 1회 셋업 후 Xcode 또는 `xcodebuild` 로 빌드/실행.

---

## CI

[.github/workflows/android-ci.yml](.github/workflows/android-ci.yml) — main 브랜치 push 또는 PR 시 자동:

- JDK 17 설정
- 더미 `google-services.json` / `local.properties` 주입 (실제 Firebase 비밀 없이도 컴파일 가능)
- `assembleDebug` 빌드
- 단위 테스트 실행
- `app-debug.apk` 를 아티팩트로 14일 보관

---

## 핵심 문서

- [docs/architecture.md](docs/architecture.md) — 전체 아키텍처
- [docs/firestore-schema.md](docs/firestore-schema.md) — 데이터베이스 스키마
- [docs/race-state-machine.md](docs/race-state-machine.md) — GPS 레이스 상태머신

## Phase 진행 현황

- [x] **Phase 0** — 기반 (저장소 구조, Firebase 규칙, 빈 앱 + 로그인)
- [x] **Phase 1** — 신원 & 프로필
- [x] **Phase 2** — 지도 & 코스 (read-only)
- [x] **Phase 3** — 레이스 & 트래킹
- [ ] **Phase 4** — 랭킹 & 마감
- [ ] **Phase 5** — 실제 OAuth (MVP 이후)
