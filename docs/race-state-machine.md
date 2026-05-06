# 레이스 / GPS 상태머신

양 플랫폼이 동일한 사양으로 구현해야 한다. 동일 픽스처(좌표 시퀀스)로 단위 테스트 가능.

## 상태 다이어그램

```
       [Idle]
          │  [참여] 탭
          ▼
       [Arming]                     출발점 30m 밖, "출발 지점으로 이동" 안내
          │
          │  loc within ARM_RADIUS_M (30m)
          ▼
       [Armed]                      카운트다운 3-2-1, 고정밀 GPS 워밍업
          │
          │  (1) 직전 샘플 반경 밖
          │  (2) 현재 샘플 반경 안 (≤ START_TRIGGER_RADIUS_M = 15m)
          │  (3) 진행 베어링이 출발→첫 웨이포인트 베어링과 90° 이내
          ▼
       [InRace]                     1Hz 샘플링, 모노토닉 클럭 타이머
          │
          │  loc within END_TRIGGER_RADIUS_M (15m) of endCoord
          ▼
       [Finished]
          │  서브밋
          ├─→ [Submitted]           rankings + records 동시 쓰기 성공
          └─→ [SubmitFailed]        재시도 버튼
                                    → [Submitted] 또는 [Cancelled]
```

### 취소 트리거 (모든 상태에서 → `Cancelled`)

- 사용자가 Cancel 버튼 탭
- 위치 권한 박탈
- 코스 코리도 이탈 누적이 `MAX_OUT_OF_CORRIDOR_MS` 초과
- 강제 종료 후 재실행 (스냅샷 복구 시 사용자에게 폐기 확인)

## 튜닝 상수

`meta/config` 문서에서 동적으로 오버라이드 가능. 클라이언트는 앱 시작 시 `meta/config`을 1회 fetch하여 메모리에 보관.

| 상수 | 기본값 | 설명 |
|---|---|---|
| `ARM_RADIUS_M` | 30 | Armed 상태 진입을 위한 출발점 반경 |
| `START_TRIGGER_RADIUS_M` | 15 | 출발선 통과 판정 반경 |
| `END_TRIGGER_RADIUS_M` | 15 | 도착선 판정 반경 |
| `SAMPLE_INTERVAL_MS` | 1000 | GPS 샘플링 주기 |
| `MIN_RACE_TIME_MS` | 30000 | 이 미만은 보안 규칙에서 거절 |
| `MAX_AVERAGE_KMH` | 200 | 초과 시 `flagged: true` |
| `MAX_OUT_OF_CORRIDOR_M` | 200 | 폴리라인에서의 거리 임계 |
| `MAX_OUT_OF_CORRIDOR_MS` | 15000 | 코리도 이탈 누적 임계 |

## GPS 파이프라인

1. **요청**: Android `Priority.PRIORITY_HIGH_ACCURACY`, iOS `kCLLocationAccuracyBest`. 1초 간격.
2. **필터**: `horizontalAccuracy > 30m` 샘플 폐기.
3. **스무딩**: 5샘플 슬라이딩 윈도우에서 정확도가 가장 나쁜 1샘플 trim.
4. **거리**: 인접 샘플 간 Haversine 누적.
5. **타이머**: 모노토닉 클럭 사용 (Android `SystemClock.elapsedRealtime()`, iOS `CACurrentMediaTime()`). NTP 보정/타임존 변경에 무관.

## 출발선 통과 판정

단순 반경 진입은 정차 중 GPS 지터로 오트리거를 일으킨다. 다음 3조건을 동시 충족할 때만 출발 트리거.

- 직전 샘플은 `START_TRIGGER_RADIUS_M` 밖
- 현재 샘플은 `START_TRIGGER_RADIUS_M` 안
- 직전→현재 이동 베어링이 출발→첫 웨이포인트 베어링과 ≤ 90° 차이

## 백그라운드 / 우발 종료

- **Android**: `RaceTrackingService` (foregroundServiceType `location`), 알림 "레이스 진행 중". 5초마다 `cacheDir/race_in_progress.json`에 스냅샷 (timeMs, samples, courseId) 저장. 프로세스 사망 후 다음 실행 시 스냅샷 발견 → "이전 레이스가 중단됨, 폐기하시겠습니까?" 다이얼로그.
- **iOS**: Background Modes `location`, `CLLocationManager.allowsBackgroundLocationUpdates = true`. 동일한 스냅샷 전략 (Documents/race_in_progress.json).
- **공통**: 강제 종료 후 복구는 MVP 범위에서 **폐기 + 확인**이 기본. 재개는 추후.

## 안티치트

| 케이스 | 처리 |
|---|---|
| `timeMs < MIN_RACE_TIME_MS` | 보안 규칙에서 거절. 클라이언트도 사전 거절. |
| `averageKmh > MAX_AVERAGE_KMH` | `flagged: true`로 기록만 됨. 리더보드 쿼리는 `where('flagged','==',false)`로 필터. |
| 코리도 누적 이탈 | 무효 처리 → 미제출 + Cancelled 결과 화면. |

샘플 트랙은 MVP에서 업로드하지 않음 (서버 부담↓). 추후 Cloud Function 검증 시 도입.

## 테스트 픽스처

`docs/race-fixtures/` 아래에 다음 시나리오의 좌표 시퀀스 JSON을 둔다 (양 플랫폼이 동일 픽스처로 단위 테스트):

- `happy_path.json` — Idle → Finished 정상 흐름
- `gps_jitter_at_start.json` — 정차 중 지터로 인한 오트리거 방지 검증
- `out_of_corridor.json` — 코리도 이탈 → Cancelled
- `accuracy_burst.json` — 한두 샘플의 정확도 폭주, 필터링 검증
- `near_min_time.json` — 30초 직전/직후 경계
