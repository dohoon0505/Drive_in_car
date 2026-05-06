# Firestore 스키마

프로젝트: `drive-3c0fd`. 모든 timestamp는 서버 시간 (`FieldValue.serverTimestamp()`)으로 설정.

## 컬렉션 구조

```
users/{uid}                          # 1 문서 = 1 유저
  └── records/{recordId}             # 본인의 전체 레이스 기록 히스토리

courses/{courseId}                   # 큐레이션된 와인딩 코스 (read-only for clients)

rankings/{rankingId}                 # 모든 레이스 기록 (최상위, denormalized)

meta/avatars                         # 아바타 ID 목록
meta/config                          # 안티치트 임계값 등 동적 설정
```

## `users/{uid}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `nickname` | string | 2~16자, 표시명 |
| `carBrand` | string | 예: "BMW", "Hyundai" |
| `carModel` | string | 예: "X5", "Avante N" |
| `profileImageId` | string | `avatar_01` ~ `avatar_08` |
| `createdAt` | timestamp | 가입 시각 |
| `updatedAt` | timestamp | 마지막 프로필 수정 시각 |

## `users/{uid}/records/{recordId}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `courseId` | string | 코스 참조 |
| `courseName` | string | denormalized, 히스토리 빠른 렌더용 |
| `timeMs` | number | 레이스 시간 (밀리초) |
| `averageKmh` | number | 평균 속도 |
| `finishedAt` | timestamp | 완주 시각 |
| `rankingDocId` | string | `rankings` 컬렉션 역참조 |

## `courses/{courseId}`

| 필드 | 타입 | 설명 |
|---|---|---|
| `name` | string | 예: "한계령 코스" |
| `description` | string | 평문 설명 (markdown 미지원) |
| `regionName` | string | 예: "강원도 인제군" |
| `startCoord` | geopoint | 시작 좌표 |
| `endCoord` | geopoint | 종료 좌표 |
| `waypoints` | array<map> | `[{lat, lng, order}]`, ≤80포인트 |
| `distanceMeters` | number | 코스 길이 (미터) |
| `difficulty` | number | 난이도 1~5 |
| `isActive` | boolean | 비활성 시 지도에서 숨김 |
| `createdAt` | timestamp | |

## `rankings/{rankingId}` — **최상위 컬렉션**

| 필드 | 타입 | 설명 |
|---|---|---|
| `courseId` | string | 코스 참조 (인덱스) |
| `uid` | string | 등록자 |
| `nickname` | string | denormalized |
| `carDisplay` | string | denormalized, "BMW X5" 형태 |
| `profileImageId` | string | denormalized |
| `timeMs` | number | 시간 (오름차순 정렬용) |
| `averageKmh` | number | |
| `finishedAt` | timestamp | |
| `clientSampleCount` | number | 안티치트 감사용 |
| `flagged` | boolean | true면 리더보드 노출 제외 |

### 복합 인덱스

```
rankings: courseId ASC, timeMs ASC          → 코스별 리더보드
rankings: uid ASC, finishedAt DESC          → 유저별 최근 기록
```

## `meta/`

```
meta/avatars
  ids: ["avatar_01", "avatar_02", ..., "avatar_08"]

meta/config
  minRaceTimeMs:        30000
  maxAverageKmh:        200
  armRadiusM:           30
  startTriggerRadiusM:  15
  endTriggerRadiusM:    15
```

## 보안 규칙 요약

- 모든 read는 로그인 필요.
- `users/{uid}`: 본인만 create/update, delete 영구 금지.
- `users/{uid}/records`: 본인만 create/read, update/delete 금지.
- `courses`: 모두 read, write 영구 금지 (콘솔/시드 전용).
- `rankings`: 본인 uid로만 create, `timeMs >= 30000` 검증, update/delete 금지.
- `meta`: 모두 read, write 금지.
