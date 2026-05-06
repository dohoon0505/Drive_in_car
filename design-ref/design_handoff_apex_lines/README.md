# Handoff: APEX Lines — Drive Course Ranking App

> 다른 AI(개발 에이전트, 디자인 에이전트, LLM)에게 이 디자인을 전달하기 위한 핸드오프 패키지예요. 이 한 문서만으로도 다시 구현할 수 있도록 작성됐어요.

---

## 1. Overview

**APEX Lines**는 드라이버들이 정해진 와인딩 코스를 달리고, 랩 타임으로 경쟁하는 모바일 커뮤니티 앱이에요.

- **타깃**: 한국 드라이빙 애호가 (BMW M, 포르쉐 911, 아이오닉 5 N 등)
- **핵심 가치**: 전국의 가장 아름다운 드라이빙 코스를 큐레이션하고, 같은 코스에서 다른 드라이버들과 시간으로 경쟁
- **톤**: 프리미엄 자동차 매거진 (《Top Gear》, 《Road & Track》) 느낌 — 차분하고 권위 있고, 게임성이 있음
- **핵심 플로우**: 가입 → 지도에서 코스 선택 → 코스 상세/랭킹 확인 → 랭킹전 참여 → 주행 측정 → 결과 + 메달

---

## 2. About the Design Files

이 번들의 모든 파일은 **HTML로 만든 디자인 레퍼런스**예요 — 의도한 룩앤필과 인터랙션을 보여주는 프로토타입일 뿐이고, 그대로 프로덕션에 사용하라는 코드가 아니에요.

**할 일**: 이 디자인을 타겟 코드베이스의 기존 환경(React Native, Flutter, SwiftUI, Kotlin Compose, 또는 웹 React 등)에서 그 환경의 컨벤션·라이브러리·디자인 시스템을 따라 **다시 구현**해주세요. 기존 환경이 없다면, 모바일 앱이라는 점을 고려해 가장 적합한 프레임워크(React Native + Expo 또는 SwiftUI 권장)를 선택해 구현하면 돼요.

---

## 3. Fidelity

**High-fidelity (hifi)** — 최종 색상·타이포·간격·인터랙션이 모두 결정된 픽셀-정확 목업이에요. 가능한 한 픽셀 단위로 재현하되, 시스템 라이브러리에 더 적합한 형태가 있다면(예: SwiftUI의 `NavigationStack`, RN의 `BottomSheet` 컴포넌트) 그쪽을 우선해요.

---

## 4. Design System Foundations

이 앱은 **UIUX-DH Design System** 위에 만들어졌어요. 핵심 토큰만 발췌:

### Colors (Dark theme — 이 앱은 다크 모드 기본)

| 역할 | 토큰 | Hex | 비고 |
|---|---|---|---|
| Background | `bg` | `#0B0D12` | 메인 배경, 거의 검정 |
| Background raised | `bgRaised` | `#14171F` | 카드, 인풋 |
| Background elevated | `bgElevated` | `#1B1F29` | 호버, 활성 카드 |
| Border subtle | `border` | `#2A2D36` | 기본 디바이더 |
| Border strong | `borderStrong` | `#3C404B` | 강조 보더 |
| Text primary | `text` | `#FFFFFF` | 본문 |
| Text secondary | `textSec` | `#B1B6C4` | 부가 정보 |
| Text tertiary | `textTer` | `#818797` | 라벨, 캡션 |
| **Brand (Indigo)** | `brand` | `#4F46E5` | 시그니처 |
| Brand light | `brandLight` | `#7968EE` | 다크 모드 텍스트 |
| Accent · gold | `amber` | `#FFD60A` | 1등, 트로피 |
| Status · success | `green` | `#22C55E` | PB, 1등 대비 단축 |
| Status · error | `red` | `#F87171` | 1등 대비 +시간 |

### Course accent colors (각 코스마다 시그니처 액센트)
- 미시령 옛길: `#FFD60A` (gold) — Editor's pick
- 팔공산 스카이라인: `#7968EE` (indigo light)
- 지리산 정령치: `#22C55E` (green)
- 남해 1024: `#F87171` (rose)

### Typography — Pretendard

| 역할 | weight | size / line-height | letter-spacing |
|---|---|---|---|
| Display lg | 800 | 56px / 1.05 | -0.035em |
| Display md | 800 | 44px / 1.05 | -0.035em |
| Display sm (랩 타임) | 900 | 84px / 1 | -0.04em |
| Heading lg | 800 | 32px / 1.15 | -0.030em |
| Heading md | 700 | 22-26px / 1.2 | -0.025em |
| Heading sm | 700 | 18-20px / 1.2 | -0.020em |
| Body lg | 400 | 17px / 1.6 | 0 |
| Body md (default) | 400 | 15px / 1.6 | 0 |
| Body sm | 400 | 13px / 1.55 | 0 |
| Label | 600 | 13px / 1.4 | 0 |
| Overline | 500-600 | 11px / 1 | 0.16-0.32em, UPPERCASE |
| Tabular nums (랩 타임 등) | 700-900 | 변동 | `font-variant-numeric: tabular-nums` |

**가이드**: 한국어가 기본이지만 라벨·메타 라인은 영문 대문자 + 와이드 트래킹(`0.16em+`)을 자주 써서 매거진 느낌을 줘요. 모든 숫자는 tabular-nums.

### Spacing (4px grid)
`2, 4, 6, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96`px — 토큰 이름은 `--size-50` ~ `--size-1200`

### Border radius
- `xs` 4 (마이크로 배지)
- `sm` 6 (인라인)
- `md` 10 (인풋)
- `lg` 14 (기본 버튼/카드)
- `xl` 18-22 (피처 카드)
- `2xl` 28 (바텀 시트, 모달)
- `full` 9999 (칩, 아바타)

### Elevation (dark)
- 1: `0 1px 2px rgba(0,0,0,0.32)`
- 3: `0 8px 24px rgba(0,0,0,0.48), 0 2px 6px rgba(0,0,0,0.32)`
- 4 (모달, 시트): `0 20px 48px rgba(0,0,0,0.56), 0 6px 12px rgba(0,0,0,0.40)`

### Motion
- `fast` 120ms (호버)
- `base` 200ms (기본 트랜지션)
- `slow` 320ms (시트, 페이지)
- Easing: `cubic-bezier(0.2, 0, 0, 1)` (standard), `cubic-bezier(0.3, 0, 0, 1)` (emphasized)

---

## 5. UX Writing Rules (한국어)

**필수 — 모든 카피에 적용**:

1. **해요체** 만 사용 — 합쇼체(되었습니다) ❌, 반말 ❌
   - ✅ `저장했어요` ❌ `저장되었습니다`
2. **능동태**, 사용자가 주어
   - ✅ `이메일을 보냈어요` ❌ `이메일이 전송되었습니다`
3. **버튼은 결과를 명명**, 4-6자 동사형
   - ✅ `랭킹전 참여`, `시작하기`, `보내기`
   - ❌ `확인`, `완료`, `OK`
4. **에러는 무엇/왜/어떻게 셋 다**: `네트워크가 불안정해요. Wi-Fi를 확인해주세요`
5. **숫자는 정확히**: `잠시`, `많은` 같은 모호어 금지

---

## 6. Iconography

**Lucide-style outline 아이콘**, stroke 1.7-1.8, round line-cap, `currentColor` 사용. 24×24 viewBox.

이 앱에서 쓴 아이콘들 (대응 Lucide 이름):
- 네비게이션: `arrow-left`, `arrow-right`, `chevron-right`, `chevron-up`
- 액션: `check`, `close`, `search`, `lock`
- 도메인: `trophy`, `flag`, `mountain`, `clock`, `car`, `zap`, `users`, `map-pin`, `navigation`, `star`
- POI: `coffee`, `parking`, `square-parking`
- 데코: `sparkles`, `trending-up`, `layers`

**이모지 금지** — 아이콘만 사용해요.

---

## 7. Screens

각 화면은 `app.jsx`의 React 컴포넌트 하나에 대응해요. 모바일 디바이스 프레임은 **402×874px (iPhone 15 Pro)**.

### Screen 1 · Login (`LoginScreen`)
**목적**: 진입점. 새 사용자에게는 가입 입구, 기존 사용자에게는 로그인.

**레이아웃** (top → bottom):
- 88px top padding
- **브랜드 마크 영역**:
  - Overline `THE DRIVER'S ATLAS` (11px, indigo light, 0.32em tracking, uppercase)
  - 로고 텍스트 `APEX` 800w 44px + 줄바꿈 + `Lines.` 300w italic indigo light
  - Subtitle `전국의 가장 아름다운 와인딩 코스에서, 당신의 라인을 새겨요.` (15px, textSec, max-width 280)
- **폼**:
  - Email 필드 (`이메일` 라벨, placeholder `you@apex.kr`)
  - Password 필드 (`비밀번호`)
  - Right-aligned `비밀번호를 잊었어요` (13px, textTer, 링크 스타일)
- **CTA 그룹** (bottom):
  - Primary `로그인하기` (full-width, brand bg)
  - Secondary `새로 시작하기` (full-width, bgElevated bg, border)
  - Footnote `계속하면 약관 및 개인정보 처리방침에 동의해요.` (12px, textTer, center)

**배경**: 상단 `radial-gradient(ellipse 90% 60% at 50% 0%, rgba(79,70,229,0.18), transparent 60%)` 위에 `#0B0D12`.

**상태**: `email`, `pw` (state)

---

### Screen 2 · Signup (`SignupScreen`)
**목적**: 새 사용자에게 닉네임, 메이커, 모델 정보 받기. 같은 차종끼리 클래스 비교를 위해 필수.

**구조**: 3-step wizard (state `step` = 0/1/2)

**상단 chrome (모든 step 공통)**:
- 72px top padding
- Back button (chevron, 0번 step에서는 로그인으로)
- Progress bar: 3개 등분 트랙, 현재까지 brand 색
- Overline `STEP {n} / 3` (12px, 0.2em tracking, uppercase, textTer)
- Title (32px, 800w, -0.03em) + subtitle (15px, textSec)

**Step 0 — 닉네임**
- Title: `어떻게 불러드릴까요?`
- Subtitle: `랭킹과 코멘트에 표시될 닉네임이에요. 2-12자 한글/영문.`
- Field: 닉네임 인풋
- 추천 칩 4개: `+ ApexHunter`, `+ NightDriver`, `+ 곡선마스터`, `+ 0to100` (탭하면 인풋에 채움)

**Step 1 — 메이커**
- Title: `어떤 메이커를 / 타고 계세요?`
- Subtitle: 같은 메이커끼리 따로 랭킹 안내
- 2-column grid, 100px height 카드 8개:
  - 좌상단: 28×28 컬러 사각형(브랜드 액센트색) + 첫 글자
  - 좌하단: 브랜드명 (15px, 700w) + 국가코드 (11px, textTer, 0.1em tracking)
- 선택 시: 보더 `1.5px brand`, bg `bgElevated`

**브랜드 데이터**:
```
BMW (DEU, #1C69D4) | 현대 (KOR, #002C5F) | Porsche (DEU, #D5001C) | Tesla (USA, #E31937)
Mercedes-Benz (DEU, #00ADEF) | Audi (DEU, #BB0A30) | 기아 (KOR, #05141F) | Genesis (KOR, #9F8345)
```

**Step 2 — 모델**
- Title: `정확히 어떤 / 모델인가요?`
- Subtitle: `{브랜드}의 라인업이에요. 정확한 차종이어야 클래스별 비교가 정확해요.`
- 풀-너비 row 카드 리스트:
  - 좌측: 44×28 라이센스 플레이트(브랜드 액센트색 bg, 흰 글씨로 브랜드 슬러그 3자)
  - 가운데: 모델명 (16px, 600w)
  - 우측 (선택 시): brand 색 체크 아이콘
- 모델 데이터(브랜드별):
  - BMW: `M3 Competition`, `M2`, `X5 M50i`, `M4 CSL`, `420i Coupe`
  - Porsche: `911 GT3`, `911 Carrera S`, `Cayman GT4`, `Taycan GTS`
  - 현대: `아이오닉 5 N`, `아반떼 N`, `쏘나타 N Line`, `아이오닉 6`
  - … (나머지 brands는 `app.jsx`의 CARS 객체 참조)

**Bottom CTA**: `다음` (마지막 step은 `시작하기`), full-width primary, 비활성화 조건 = (nick.length<2 || !brand || !car).

---

### Screen 3 · Map Home (`MapHome`)
**목적**: 메인 화면. 지도 위에 코스 핀이 있고, 핀을 탭하면 하단 카드에 코스 정보가 나타남.

**구조**: 풀스크린 다크 토포그래픽 지도 + 떠있는 UI 레이어들

**Map Canvas**: 100% 너비, SVG로 렌더 (viewBox `0 0 100 130`)
- 베이스: 상단 라이트한 `#1B1F29` 라디얼 그라디언트, `#0B0D12`로 페이드
- 등고선: 6-12개 부드러운 곡선 (`#1B1F29`/`#14171F`, stroke 0.2-0.25)
- 해안선: 하단에 `#080A0F`로 채운 곡선
- 도로/하이웨이 힌트: 가는 `#14171F` 라인 2개
- **코스 라인**: 각 코스 액센트 색으로 dashed 곡선 (`stroke-dasharray 0.8 0.6`)
- 도시 라벨 (영문 + 한국어): Seoul/서울, Sokcho/속초, Daegu/대구, Jeonju/전주, Tongyeong/통영 — 9px textTer 0.16em tracking uppercase

**Map Pins** (코스마다 하나):
- 절대 위치 (코스의 `x%`, `y%`)
- Pin 카드 (라벨 형태):
  - bg: 비포커스 `bgRaised`, 포커스 시 `accent` 컬러
  - 패딩 `8px 12px`, radius 10
  - 좌측 6×6 점 (포커스: 검정, 비포커스: 액센트)
  - 텍스트: 코스명 (12px, 700w)
  - 보더: `1px borderStrong`, 포커스 시 `accent`
  - 그림자: `0 6px 18px rgba(0,0,0,0.5)`
- 화살표 꼬리: 6×6 삼각형 below pin

**Top overlay** (top: 56px):
- 검색바: 1×48 높이, `rgba(20,23,31,0.78)` + backdrop-blur 20px, 보더, radius 14
  - search 아이콘 + placeholder `코스, 지역, 라이더 검색`
- Profile 버튼 (48×48 정사각): 같은 글래스 스타일, 안에 30×30 brand색 원 + 닉네임 첫 글자

**Filter chips** (top: 116px):
- 가로 스크롤
- 36px 높이, 9999 radius, 14px horizontal padding
- 활성: 흰색 bg, `#0B0D12` 글씨
- 비활성: glass bg (`rgba(20,23,31,0.78)` + blur), textSec
- `내 주변` (mapPin), `주말 추천` (sparkles), `★ 친구` (users), `난이도 ↑` (trendingUp)

**Zoom controls** (right: 16px, top: 40%):
- 세로 스택, 8px gap
- `+`, `−` 글래스 버튼 (44×44, radius 12)
- 마지막에 brand색 navigation 버튼 (현재 위치)

**Bottom course peek card** (bottom: 24px, left/right: 12px):
- bg `bgRaised`, radius 22, padding 16, border, shadow `0 24px 48px rgba(0,0,0,0.6)`
- 좌측 56×56 그라디언트 사각형 (액센트→액센트66) + mountain 아이콘
- 우측 컨텐츠:
  - Overline 지역 (11px, textTer, 0.16em)
  - 코스명 (19px, 700w)
  - 메타 row: `{distance}km`, 난이도 별, 베스트 랩 (12px, textSec, 강조 부분만 text)
- Full-width primary 버튼: `코스 자세히 보기 →`

**상태**: `focused` (현재 포커스된 코스 id)

**코스 데이터** (4개):
```yaml
mishiryeong:
  name: 미시령 옛길
  region: 강원 인제 — 고성
  distance: 17.4 km
  duration: 24분
  difficulty: ★★★★☆ (4)
  bestLap: 14:38.220
  best: NightDriver
  participants: 1284
  elev: 482m
  corners: 47
  accent: #FFD60A
  blurb: 동해를 향해 떨어지는 18개의 헤어핀. 한국 와인딩의 원형.
  pin: { x: 64%, y: 28% }
  poi:
    - { type: coffee, name: 미시령 휴게소, dist: 0.4km }
    - { type: parking, name: 진부령 전망 주차장, dist: 12km }
    - { type: coffee, name: Café Loop, dist: 14km }
  splits:
    - { name: S1 · 출발 — 시작 헤어핀, time: 02:18.42 }
    - { name: S2 · 와인딩 구간, time: 08:44.10 }
    - { name: S3 · 다운힐, time: 03:35.70 }

palgong:
  name: 팔공산 스카이라인
  region: 대구 동구
  distance: 11.2, duration: 17분, difficulty: 3, bestLap: 09:22.840
  best: ApexHunter, participants: 892, elev: 318m, corners: 32
  accent: #7968EE, pin: { x: 52%, y: 56% }
  blurb: 도심에서 30분, 일출 라이드의 정석.

jirisan:
  name: 지리산 정령치 루프
  region: 전북 남원
  distance: 22.8, duration: 32분, difficulty: 5, bestLap: 21:04.110
  best: ApexHunter, participants: 612, elev: 718m, corners: 64
  accent: #22C55E, pin: { x: 38%, y: 72% }
  blurb: 2시간 거리, 평생 기억할 64개의 코너.

namhae:
  name: 남해 1024번 도로
  region: 경남 남해
  distance: 28.6, duration: 38분, difficulty: 3, bestLap: 23:11.300
  best: 곡선마스터, participants: 2104, elev: 220m, corners: 41
  accent: #F87171, pin: { x: 28%, y: 86% }
  blurb: 바다와 동백, 그리고 끝없는 미디엄 코너.
```

(전체는 `app.jsx`의 `COURSES` 배열 참조.)

---

### Screen 4 · Course Detail (`CourseDetail`)
**목적**: 한 코스에 대한 모든 정보 + 이 코스의 랭킹. CTA 두 개로 끝남: [참여 안내] [랭킹전 참여].

**스크롤 가능한 화면**, 하단에 sticky action footer.

**HERO** (320px tall):
- 그라디언트 배경: `linear-gradient(180deg, ${accent}11 0%, bg 100%)`
- 위에 추가 그라디언트: `linear-gradient(135deg, ${accent}33 0%, transparent 50%, ${accent}11 100%)` + radial 글로우
- 산실루엣 SVG (placeholder photo) — viewBox 100×60, opacity 0.55
- Top: Back 버튼 (44×44 glass) + EDITORS' PICK 배지 (gold trophy + 우상단)
- Bottom-left: 지역 overline (액센트색) + 코스명 (36px, 800w) + blurb (14px, textSec)

**Quick stats grid** (4-col, 1px gap, border bg):
- 거리 / 주행 시간 / 코너 / 고도
- 각 셀: bg, 18px vertical padding, center-aligned
- 값 22px 700w + 라벨 11px textTer 0.04em tracking

**Best Lap Card** (28px top padding):
- bgRaised + bgElevated 그라디언트, accent55 보더, radius 18, padding 18
- 좌측 52×52 액센트 그라디언트 사각형 + trophy 아이콘
- 우측: overline `코스 베스트 랩` + 시간 (26px 800w tabular) + `by {best} · 3일 전`

**Elevation graph card**:
- 헤딩 `고도 / 굴곡 프로파일` (18px 700w)
- bgRaised 카드 안에 SVG 그래프 (300×80 viewBox, accent색 라인 + accent gradient fill)
- 그래프 5개 마커 점
- x축 라벨 5개 (0km, 거리/4, 거리/2, 거리*0.75, 전체) — 11px textTer

**랭킹 섹션** — `이 코스의 랭킹`:
- 헤딩 + 우측 메타 `참여 {n}명`
- **Podium**: bgRaised 카드, padding 24/16/0, 3-col flex align-end
  - 각 칸: 56×56 원형 메달 (1등 amber, 2등 #B1B6C4 silver, 3등 #B68B00 bronze) + 닉네임 + 시간 + 컬러 그라디언트 블록 (높이 48/64/92, radius 8 8 0 0, 28px 900w 숫자 안쪽)
- **탭 (4개)**: 전체 / 내 차종 / 친구 / 이번 주 — bgRaised pill, 활성탭만 bgElevated
- **리스트**: bgRaised 카드, 6 row
  - row layout: rank | avatar (36×36 brand원) | nick(14/600) + car(11/500 textTer) | 시간(14/700 tabular) + delta(11 red)
  - rank 1은 delta 없음, 나머지는 `+04.660`, `+12.820`, `+20.090`, `+24.550`, `+33.200`

**Ranking 데이터 (top 6)**:
```
1. NightDriver · BMW M4 CSL · 14:38.220
2. ApexHunter · Porsche 911 GT3 · 14:42.880 (+04.660)
3. 곡선마스터 · Hyundai 아이오닉 5 N · 14:51.040 (+12.820)
4. 0to100 · AMG GT · 14:58.310 (+20.090)
5. WindingKim · Audi RS5 · 15:02.770 (+24.550)
6. TrackDad · Tesla Model 3 P · 15:11.420 (+33.200)
```

**주변 명소** (POI carousel):
- 가로 스크롤, 180px 너비 카드
- 36×36 bgElevated 원 + amber 아이콘 (coffee or parking)
- 이름 (14/600) + 거리 (12/500 textTer)

**Sticky action footer** (absolute bottom):
- 그라디언트 페이드 마스크 위에
- 두 버튼: `참여 안내` (secondary, flag 아이콘, flex 1) + `랭킹전 참여` (primary, zap 아이콘, flex 1.4)

---

### Screen 5 · Join Sheet (`JoinSheet`) — 모달

**트리거**: 코스 상세에서 `참여 안내` or `랭킹전 참여` 탭

**구조**: 백드롭 + 바텀 시트 (둘 다 absolute fill)

**Backdrop**:
- `rgba(0,0,0,0.6)` + backdrop-blur 4px
- fadeIn 200ms 애니메이션
- 클릭 시 닫힘

**Sheet**:
- bottom-anchored, full-width
- bg, border (top 보더만 보임), radius `28px 28px 0 0`
- padding `12px 24px 32px`, max-height 85%
- slideUp 320ms `cubic-bezier(0.2,0,0,1)` 애니메이션
- 상단 36×4 borderStrong drag handle

**Content**:
1. Overline `참여 안내` (11px, accent color, 0.2em tracking, uppercase)
2. Title (코스명, 26px 800w)
3. Subtitle (mode에 따라 다름)
4. **Rules list** (4개, 24px gap from title):
   - 36×36 bgRaised 아이콘 박스 (lock/flag/trophy/users)
   - 우측: 제목 (15/700) + 설명 (13/400 textSec)
   - 룰: 제한속도 / GPS 자동 감지 / 클래스별 비교 / 동승자 OK
5. **Best lap reminder card**: bgRaised, `도전할 베스트 랩` 라벨 + 시간
6. **Start CTA**: full-width primary `시작하기` (zap 아이콘)

---

### Screen 6 · Driving (`DrivingScreen`)
**목적**: 실시간 랩 측정 화면. 12초 후 자동으로 결과로 넘어감 (실 사용에서는 GPS 종료 라인 통과 시).

**배경**: `#000` + radial 그라디언트
- SVG 위에 코스 라인 그리고, 진행률만큼 accent 색으로 트레이스
- 현재 위치 점: 흰 점 + accent ring (1.5s pulse)

**Top HUD** (top: 60px, two cards):
- 좌측 (flex 1): glass card, `NOW DRIVING` overline + 코스명 (14/700)
- 우측: vs Best 델타 — delta < 0이면 green bg+border+text, 아니면 red. `VS BEST` overline + `−/+ {abs delta}s` (16/700 tabular)

**Center · Lap clock** (top: 32%):
- Overline `ELAPSED` (0.32em tracking, textSec)
- Giant time: 84px 900w -0.04em letterspace, format `MM:SS.ms`
- Below: 두 메트릭 (가운데 1px divider)
  - SPEED (km/h, 32/700) — 시뮬레이션 값
  - DISTANCE (km, 32/700) — 진행률 × 코스 거리

**Bottom · Sectors panel** (left/right: 16, bottom: 110):
- glass card, padding 16, radius 18
- Overline `SECTORS`
- 3개 sector 카드 (코스마다 정의된 splits 사용):
  - 도달함: green tint bg + green border + green time
  - 진행 중: accent22 bg + accent border + accent time
  - 미도달: bgRaised + border + `—`

**Abort button** (bottom: 48):
- 가운데 pill, glass, `× 주행 중단`
- 탭 시 결과 화면으로 (null result)

**상태/로직**:
- `t` (타이머, sec), `speed`, `progress` (0-1)
- requestAnimationFrame 루프
- speed sim: `72 + 38 * sin(t/1.7) + 10 * sin(t*1.3)`
- 12초 후 자동 onFinish 호출 → 결과로 이동
- 표시 시간은 `t * 73`으로 14:30 범위처럼 보이게 스케일

---

### Screen 7 · Results (`ResultsScreen`)
**목적**: 주행 완료 축하 + 새 기록/등수 표시. 게임 같은 보상감.

**배경**: 상단 라디얼 글로우 (코스 액센트) + bg

**Hero block** (center):
- 100×100 원형 메달 (액센트→액센트66 그라디언트) + trophy 아이콘
- 외곽 링 2개: `0 0 0 6px ${accent}22`, `0 0 0 10px ${accent}11`
- Overline `NEW PERSONAL BEST` (0.32em, accent)
- Title `완주했어요` (32/800)
- Subtitle `{코스명}을(를) 완주하고 새 기록을 세웠어요.`

**Time card** (28px top margin):
- bgRaised, padding 24/20, radius 20, center-aligned
- Overline `FINAL LAP`
- Giant time: 56px 900w tabular
- 3-col 메트릭 (1px dividers):
  - 전체 순위 `#4` (20/700, accent)
  - VS 1등 `+20.09` (20/700, text)
  - VS 내 PB `−1.4s` (20/700, green)

**Earned badges** (3 cards, gap 10):
- 각 14px padding 카드, color55 보더
- `TOP 5` (accent) — 신규 진입
- `PB` (green) — −1.4s
- `+128 XP` (brand) — 레벨 12

**Bottom CTAs**:
- Primary `지도로 돌아가기`
- Secondary `한 번 더 도전하기`

---

## 8. Navigation / State machine

```
[login]
  ├─ "로그인하기" ────────────────────────────────────────► [map]
  └─ "새로 시작하기" ──► [signup] ──"시작하기"──────────► [map]

[map]
  └─ 코스 카드 "코스 자세히 보기" ──► [course]

[course]
  ├─ Back ◄────────────────────────► [map]
  └─ "참여 안내" or "랭킹전 참여" ──► [course + sheet]

[course + sheet]
  ├─ backdrop click / drag-down ◄──► [course]
  └─ "시작하기" ─────────────────────► [drive]

[drive]
  ├─ 12초 자동 또는 sector 완료 ────► [results]
  └─ "주행 중단" ─────────────────────► [results (null)]

[results]
  ├─ "지도로 돌아가기" ───────────────► [map]
  └─ "한 번 더 도전하기" ─────────────► [drive]
```

**전역 상태 (앱 레벨)**:
- `screen`: `'login' | 'signup' | 'map' | 'course' | 'drive' | 'results'`
- `user`: `{ nick: string, brand: Brand, car: string }`
- `activeCourse`: `Course | null`
- `sheet`: `'info' | 'start' | null`
- `result`: `{ time: string, course: Course, user: User } | null`

---

## 9. Animations

| 위치 | 트리거 | 속성 | 지속 | Easing |
|---|---|---|---|---|
| Sheet backdrop | sheet 열림 | opacity 0→1 | 200ms | linear |
| Sheet | sheet 열림 | translateY(100%)→0 | 320ms | `cubic-bezier(0.2,0,0,1)` |
| Pin focus | 코스 핀 탭 | bg/color/border 색 전환 | 120ms | `cubic-bezier(0.2,0,0,1)` |
| Driving pulse | 항상 | radius 4→8→4, opacity 1→0→1 | 1500ms | infinite |
| Driving trace | progress 변화 | strokeDashoffset 200→0 | progressive (실시간) | linear |
| Card hover (웹 시) | hover | translateY(0→-2px) + elevation 1→3 | 120-200ms | standard |
| Button transition | 모든 버튼 | bg / color | 120ms | `cubic-bezier(0.2,0,0,1)` |

---

## 10. Responsive / Frame

- 디자인 베이스: **402×874 px (iPhone 15 Pro)**
- 안전 영역: top 60-72px (status bar + dynamic island), bottom 34px (home indicator)
- iOS 다크 system bar 가정. Android는 같은 안전 영역 토큰을 따름.
- 디자인 자체는 모바일 전용. 태블릿/데스크톱은 이번 스코프 외.

---

## 11. Files in this bundle

| 파일 | 용도 |
|---|---|
| `index.html` | 호스트 페이지 — iOS 프레임 + 스케일링 + App 컴포넌트 마운트 |
| `app.jsx` | 모든 화면 컴포넌트 + COURSES/RANK/BRANDS 데이터 + 내비게이션 |
| `ios-frame.jsx` | iOS 디바이스 프레임 (status bar, dynamic island, home indicator) |
| `tweaks-panel.jsx` | 디자인 검토용 토글 패널 (구현 시 제거 가능) |
| `colors_and_type.css` | UIUX-DH 디자인 시스템 토큰 + Pretendard `@font-face` |
| `fonts/` | Pretendard WOFF 9-weight |

**구현 시 주의**:
- `tweaks-panel.jsx`는 디자인 리뷰 전용 — 프로덕션에선 제거
- `ios-frame.jsx`는 데모용 디바이스 모형 — 실 앱에선 OS 네이티브 chrome 사용
- 데이터는 `app.jsx`에 하드코딩되어 있음. 실 구현에서는 백엔드 API 응답으로 대체

---

## 12. Open questions for product

다음 단계로 정의가 필요한 항목들:
- 실시간 랩 측정의 시작/종료 라인 검출 알고리즘 (GPS geofence radius?)
- 랭킹 무효 처리 기준 (속도 위반 감지 정확도, 우회 경로 검출)
- 친구 / 매거진 / 마이 가라지 화면 (이번 스코프 외)
- 푸시 노티 정책 (내 기록이 깨졌을 때 등)
- 코스 제안/큐레이션 워크플로우 (사용자가 새 코스를 제안할 수 있는지)

---

*이 문서는 디자인 프로토타입과 함께 봐주세요. 모호한 부분이 있으면 `index.html`을 직접 열어 인터랙션과 비주얼을 확인하세요.*
