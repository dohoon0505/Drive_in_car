# Architecture

본 문서는 Drive in Car MVP의 전체 아키텍처를 정리한다. 자세한 결정 배경은 `/.claude/plans/elegant-seeking-sprout.md`를 참고.

## 시스템 다이어그램

```
┌──────────────────┐         ┌──────────────────┐
│  Android (Kotlin)│         │   iOS (Swift)    │
│   Compose + Hilt │         │ SwiftUI + @Observ│
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         │   Firebase SDK             │   Firebase SDK
         │   Google Maps SDK          │   Google Maps SDK
         ▼                            ▼
┌──────────────────────────────────────────────────┐
│              Firebase (drive-3c0fd)              │
│   ┌──────┐  ┌──────────┐  ┌─────────┐  ┌──────┐ │
│   │ Auth │  │Firestore │  │ Storage │  │ Rules│ │
│   └──────┘  └──────────┘  └─────────┘  └──────┘ │
└──────────────────────────────────────────────────┘
```

## 레이어드 아키텍처 (양 플랫폼 공통)

```
UI (Compose / SwiftUI)
    ↓
ViewModel / @Observable
    ↓
Use Case (선택적, 핵심 도메인 로직)
    ↓
Repository (인터페이스)
    ↓
Repository 구현체 (Firebase SDK 호출)
    ↓
Firebase
```

- **UI**: 상태 표시와 사용자 입력에만 집중. 비즈니스 로직 없음.
- **ViewModel/@Observable**: UI 상태(`UiState`) 노출, 사이드 이펙트(`UiEvent`) 발행. Use Case 또는 Repository 호출.
- **Use Case**: 레이스 상태머신처럼 여러 Repository에 걸친 도메인 로직. 단순 CRUD에는 불필요.
- **Repository**: 인터페이스로 추상화. 구현체만 Firebase SDK를 직접 사용.

## 컴포넌트 매핑

| 도메인 | Android | iOS |
|---|---|---|
| Splash | `ui/splash/SplashScreen.kt` | `Features/Splash/SplashScreen.swift` |
| Login | `ui/login/LoginScreen.kt` | `Features/Login/LoginScreen.swift` |
| Profile Setup | `ui/profile/ProfileSetupScreen.kt` | `Features/Profile/ProfileSetupScreen.swift` |
| Map | `ui/map/MapScreen.kt` | `Features/Map/MapScreen.swift` |
| Course Detail | `ui/map/CourseDetailSheet.kt` | `Features/Map/CourseDetailSheet.swift` |
| Race | `ui/race/RaceScreen.kt` + `service/RaceTrackingService.kt` | `Features/Race/RaceScreen.swift` |
| Result | `ui/result/ResultScreen.kt` | `Features/Result/ResultScreen.swift` |
| Ranking | `ui/ranking/RankingListScreen.kt` | `Features/Ranking/RankingListScreen.swift` |

## 인증 / 자동 로그인

1. `SplashScreen` 진입 시 `FirebaseAuth.currentUser` 확인.
2. `null` → `LoginScreen`.
3. 존재하지만 `users/{uid}` 문서 없음 → `ProfileSetupScreen` (강제).
4. 존재하고 프로필도 있음 → `MapScreen`.

Firebase Auth는 기본 세션 영속화를 제공하므로 별도 토큰 저장 불필요.

## 데이터 흐름 (코스 → 레이스)

1. `MapScreen`이 `CourseRepository.observeCourses()`를 구독 → `Flow<List<Course>>`.
2. 사용자가 마커 탭 → `CourseDetailSheet`가 해당 `Course`로 렌더.
3. [참여] → 위치 권한 요청 → `RaceScreen` 진입.
4. `RaceViewModel`이 `LocationProvider`를 1Hz로 구독, 상태머신 진행.
5. 종료 시 `SubmitTimeUseCase`가 `rankings` + `users/{uid}/records`에 동시 쓰기.
6. `ResultScreen`으로 네비게이트.

## 네트워크 / 오프라인

- Firestore의 기본 오프라인 캐시 사용. 클라이언트는 별도 캐시 레이어를 두지 않음.
- 코스 목록은 한 번 받으면 Firestore SDK가 자동 캐시.
- 권한 / 위치 / Maps API 키 누락 같은 환경 오류는 별도 `EnvCheck` 화면 없이 각 화면의 에러 상태로 표현.

## 보안 경계

- 클라이언트는 본인의 `uid`로만 `users/{uid}`와 `rankings`에 쓸 수 있음.
- `courses`와 `meta`는 클라이언트 read-only — 시드 스크립트 또는 콘솔로만 쓰기.
- `rankings`는 한 번 쓰면 update/delete 불가 (`firestore.rules`).
- `timeMs >= 30000` 등 안티치트 검증은 클라이언트뿐 아니라 보안 규칙에서도 강제.
