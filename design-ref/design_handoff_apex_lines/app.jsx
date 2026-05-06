// Drive Course Ranking App — full flow prototype
// Screens: splash → login → signup(nick/brand/car) → map → course detail
//          → ranking sheet → driving → finish/results

const { useState, useEffect, useRef } = React;

// ─── DESIGN TOKENS (component-scope, premium magazine + dark map) ───
const TOKENS = {
  bg: '#0B0D12',
  bgRaised: '#14171F',
  bgElevated: '#1B1F29',
  border: '#2A2D36',
  borderStrong: '#3C404B',
  text: '#FFFFFF',
  textSec: '#B1B6C4',
  textTer: '#818797',
  brand: '#4F46E5',
  brandLight: '#7968EE',
  amber: '#FFD60A',
  amberSoft: '#FFE066',
  green: '#22C55E',
  red: '#F87171',
};

// ─── SHARED PRIMITIVES ───────────────────────────────────────────────
const Btn = ({ children, variant = 'primary', size = 'lg', full, onClick, style, disabled }) => {
  const base = {
    border: 'none', cursor: disabled ? 'not-allowed' : 'pointer',
    fontFamily: 'Pretendard', fontWeight: 700, letterSpacing: '-0.01em',
    borderRadius: 14, transition: 'all 120ms cubic-bezier(0.2,0,0,1)',
    display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
    gap: 8, opacity: disabled ? 0.4 : 1,
  };
  const sizes = {
    lg: { height: 56, padding: '0 24px', fontSize: 17 },
    md: { height: 48, padding: '0 20px', fontSize: 15 },
    sm: { height: 36, padding: '0 14px', fontSize: 13 },
  };
  const variants = {
    primary: { background: TOKENS.brand, color: '#fff' },
    secondary: { background: TOKENS.bgElevated, color: TOKENS.text, border: `1px solid ${TOKENS.border}` },
    ghost: { background: 'transparent', color: TOKENS.text },
    light: { background: '#fff', color: '#0B0D12' },
  };
  return (
    <button onClick={disabled ? undefined : onClick} style={{
      ...base, ...sizes[size], ...variants[variant],
      width: full ? '100%' : undefined, ...style,
    }}>{children}</button>
  );
};

const Field = ({ label, value, onChange, placeholder, type = 'text' }) => (
  <label style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
    <span style={{
      font: '600 12px/1.4 Pretendard', color: TOKENS.textSec,
      letterSpacing: '0.04em', textTransform: 'uppercase',
    }}>{label}</span>
    <input value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} type={type}
      style={{
        height: 52, background: TOKENS.bgRaised,
        border: `1px solid ${TOKENS.border}`, borderRadius: 12,
        color: TOKENS.text, font: '500 16px/1 Pretendard',
        padding: '0 16px', outline: 'none',
      }} />
  </label>
);

// Icon helper (Lucide-style outline, currentColor)
const I = ({ d, size = 20, fill = 'none', stroke = 'currentColor', sw = 1.8, children }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke={stroke}
    strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}>
    {children || <path d={d} />}
  </svg>
);
const Icons = {
  arrowLeft: <path d="M19 12H5m6-7l-7 7 7 7"/>,
  arrowRight: <path d="M5 12h14m-7-7l7 7-7 7"/>,
  check: <path d="M20 6L9 17l-5-5"/>,
  close: <path d="M18 6L6 18M6 6l12 12"/>,
  search: <><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></>,
  trophy: <><path d="M6 9H4.5a2.5 2.5 0 010-5H6"/><path d="M18 9h1.5a2.5 2.5 0 000-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0012 0V2z"/></>,
  flag: <><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></>,
  mountain: <path d="m8 3 4 8 5-5 5 15H2L8 3z"/>,
  clock: <><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></>,
  car: <><path d="M14 16H9m10 0h3v-3.15a1 1 0 00-.84-.99L16 11l-2.7-3.6a1 1 0 00-.8-.4H5.24a2 2 0 00-1.8 1.1l-.8 1.63A6 6 0 002 12.42V16h2"/><circle cx="6.5" cy="16.5" r="2.5"/><circle cx="16.5" cy="16.5" r="2.5"/></>,
  zap: <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>,
  users: <><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></>,
  mapPin: <><path d="M20 10c0 7-8 12-8 12s-8-5-8-12a8 8 0 0116 0z"/><circle cx="12" cy="10" r="3"/></>,
  navigation: <polygon points="3 11 22 2 13 21 11 13 3 11"/>,
  chevronRight: <polyline points="9 18 15 12 9 6"/>,
  chevronUp: <polyline points="18 15 12 9 6 15"/>,
  star: <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>,
  coffee: <><path d="M17 8h1a4 4 0 010 8h-1"/><path d="M3 8h14v9a4 4 0 01-4 4H7a4 4 0 01-4-4V8z"/><line x1="6" y1="2" x2="6" y2="4"/><line x1="10" y1="2" x2="10" y2="4"/><line x1="14" y1="2" x2="14" y2="4"/></>,
  parking: <><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 17V7h4a3 3 0 010 6H9"/></>,
  trendingUp: <><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></>,
  layers: <><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></>,
  sparkles: <><path d="M12 3v3m0 12v3M3 12h3m12 0h3M5.6 5.6l2.1 2.1m8.6 8.6l2.1 2.1M5.6 18.4l2.1-2.1m8.6-8.6l2.1-2.1"/></>,
  user: <><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></>,
  home: <><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></>,
  lock: <><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></>,
};

// ─────────────────────────────────────────────────────────────
// SCREEN 1 · LOGIN
// ─────────────────────────────────────────────────────────────
function LoginScreen({ onLogin, onSignup }) {
  const [email, setEmail] = useState('drive@apex.kr');
  const [pw, setPw] = useState('••••••••');
  return (
    <div style={{
      width: '100%', height: '100%',
      background: `radial-gradient(ellipse 90% 60% at 50% 0%, rgba(79,70,229,0.18), transparent 60%), ${TOKENS.bg}`,
      color: TOKENS.text, padding: '88px 24px 32px',
      boxSizing: 'border-box', display: 'flex', flexDirection: 'column',
    }}>
      {/* Brand mark */}
      <div style={{ marginBottom: 64 }}>
        <div style={{
          font: '500 11px/1 Pretendard', letterSpacing: '0.32em',
          color: TOKENS.brandLight, textTransform: 'uppercase', marginBottom: 14,
        }}>The Driver's Atlas</div>
        <div style={{
          font: '800 44px/1.05 Pretendard', letterSpacing: '-0.035em',
        }}>APEX<br/>
          <span style={{ color: TOKENS.brandLight, fontWeight: 300, fontStyle: 'italic' }}>Lines.</span>
        </div>
        <div style={{
          marginTop: 18, font: '400 15px/1.6 Pretendard', color: TOKENS.textSec, maxWidth: 280,
        }}>전국의 가장 아름다운 와인딩 코스에서, 당신의 라인을 새겨요.</div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 14, flex: 1 }}>
        <Field label="이메일" value={email} onChange={setEmail} placeholder="you@apex.kr" />
        <Field label="비밀번호" value={pw} onChange={setPw} type="password" />
        <a style={{
          alignSelf: 'flex-end', font: '500 13px/1 Pretendard',
          color: TOKENS.textTer, marginTop: 4, textDecoration: 'none',
        }}>비밀번호를 잊었어요</a>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <Btn full onClick={onLogin}>로그인하기</Btn>
        <Btn full variant="secondary" onClick={onSignup}>새로 시작하기</Btn>
        <div style={{
          font: '400 12px/1.5 Pretendard', color: TOKENS.textTer,
          textAlign: 'center', marginTop: 12,
        }}>계속하면 약관 및 개인정보 처리방침에 동의해요.</div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// SCREEN 2 · SIGNUP (3 steps: nickname → brand → car)
// ─────────────────────────────────────────────────────────────
const BRANDS = [
  { id: 'bmw', name: 'BMW', country: 'DEU', accent: '#1C69D4' },
  { id: 'hyundai', name: '현대', country: 'KOR', accent: '#002C5F' },
  { id: 'porsche', name: 'Porsche', country: 'DEU', accent: '#D5001C' },
  { id: 'tesla', name: 'Tesla', country: 'USA', accent: '#E31937' },
  { id: 'mb', name: 'Mercedes-Benz', country: 'DEU', accent: '#00ADEF' },
  { id: 'audi', name: 'Audi', country: 'DEU', accent: '#BB0A30' },
  { id: 'kia', name: '기아', country: 'KOR', accent: '#05141F' },
  { id: 'genesis', name: 'Genesis', country: 'KOR', accent: '#9F8345' },
];
const CARS = {
  bmw: ['M3 Competition', 'M2', 'X5 M50i', 'M4 CSL', '420i Coupe'],
  hyundai: ['아이오닉 5 N', '아반떼 N', '쏘나타 N Line', '아이오닉 6'],
  porsche: ['911 GT3', '911 Carrera S', 'Cayman GT4', 'Taycan GTS'],
  tesla: ['Model 3 Performance', 'Model S Plaid', 'Model Y'],
  mb: ['AMG C63 S', 'AMG GT', 'EQS', 'E450 Coupe'],
  audi: ['RS5', 'RS6 Avant', 'R8 V10', 'e-tron GT'],
  kia: ['EV6 GT', '스팅어 마이스터', 'K5 GT'],
  genesis: ['G70 슈팅브레이크', 'G80 스포츠', 'GV70', 'X 컨버터블'],
};

function SignupScreen({ onDone, onBack }) {
  const [step, setStep] = useState(0);
  const [nick, setNick] = useState('');
  const [brand, setBrand] = useState(null);
  const [car, setCar] = useState(null);

  const canNext = [nick.length >= 2, !!brand, !!car][step];
  const next = () => step < 2 ? setStep(step + 1) : onDone({ nick, brand, car });

  return (
    <div style={{
      width: '100%', height: '100%', background: TOKENS.bg, color: TOKENS.text,
      display: 'flex', flexDirection: 'column', boxSizing: 'border-box',
    }}>
      {/* top: back + progress */}
      <div style={{ padding: '72px 24px 0' }}>
        <button onClick={() => step === 0 ? onBack() : setStep(step - 1)} style={{
          background: 'none', border: 'none', color: TOKENS.text, padding: 0, cursor: 'pointer',
          display: 'flex', alignItems: 'center', gap: 6, font: '500 14px/1 Pretendard',
        }}>
          <I size={18}>{Icons.arrowLeft}</I>
        </button>
        <div style={{ display: 'flex', gap: 6, marginTop: 22 }}>
          {[0, 1, 2].map(i => (
            <div key={i} style={{
              flex: 1, height: 3, borderRadius: 2,
              background: i <= step ? TOKENS.brand : TOKENS.bgElevated,
              transition: 'background 200ms',
            }}/>
          ))}
        </div>
        <div style={{
          marginTop: 16, font: '500 12px/1 Pretendard', letterSpacing: '0.2em',
          color: TOKENS.textTer, textTransform: 'uppercase',
        }}>{`STEP ${step + 1} / 3`}</div>
        <h1 style={{
          marginTop: 8, font: '800 32px/1.15 Pretendard', letterSpacing: '-0.03em',
        }}>{['어떻게 불러드릴까요?', '어떤 메이커를\n타고 계세요?', '정확히 어떤\n모델인가요?'][step].split('\n').map((s, i) => <div key={i}>{s}</div>)}</h1>
        <p style={{
          marginTop: 12, font: '400 15px/1.55 Pretendard', color: TOKENS.textSec,
        }}>{[
          '랭킹과 코멘트에 표시될 닉네임이에요. 2-12자 한글/영문.',
          '내 차의 메이커를 골라주세요. 같은 메이커끼리 따로 랭킹을 볼 수 있어요.',
          `${brand?.name} 라인업이에요. 정확한 차종이어야 클래스별 비교가 정확해요.`,
        ][step]}</p>
      </div>

      <div style={{ flex: 1, padding: '32px 24px 0', overflowY: 'auto' }}>
        {step === 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
            <Field label="닉네임" value={nick} onChange={setNick} placeholder="예: ApexHunter" />
            <div style={{
              display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 4,
            }}>
              {['ApexHunter', 'NightDriver', '곡선마스터', '0to100'].map(s => (
                <button key={s} onClick={() => setNick(s)} style={{
                  background: TOKENS.bgRaised, border: `1px solid ${TOKENS.border}`,
                  color: TOKENS.textSec, padding: '8px 14px', borderRadius: 9999,
                  font: '500 13px/1 Pretendard', cursor: 'pointer',
                }}>+ {s}</button>
              ))}
            </div>
          </div>
        )}
        {step === 1 && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
            {BRANDS.map(b => {
              const sel = brand?.id === b.id;
              return (
                <button key={b.id} onClick={() => setBrand(b)} style={{
                  height: 100, background: sel ? TOKENS.bgElevated : TOKENS.bgRaised,
                  border: `1.5px solid ${sel ? TOKENS.brand : TOKENS.border}`,
                  borderRadius: 14, cursor: 'pointer', textAlign: 'left',
                  padding: 14, display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
                  transition: 'all 120ms',
                }}>
                  <div style={{
                    width: 28, height: 28, borderRadius: 6, background: b.accent,
                    color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    font: '800 11px/1 Pretendard',
                  }}>{b.name[0]}</div>
                  <div>
                    <div style={{ font: '700 15px/1.2 Pretendard', color: TOKENS.text }}>{b.name}</div>
                    <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, marginTop: 4, letterSpacing: '0.1em' }}>{b.country}</div>
                  </div>
                </button>
              );
            })}
          </div>
        )}
        {step === 2 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {CARS[brand?.id || 'bmw'].map(c => {
              const sel = car === c;
              return (
                <button key={c} onClick={() => setCar(c)} style={{
                  background: sel ? TOKENS.bgElevated : TOKENS.bgRaised,
                  border: `1.5px solid ${sel ? TOKENS.brand : TOKENS.border}`,
                  borderRadius: 14, padding: '18px 18px', cursor: 'pointer',
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  transition: 'all 120ms',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <div style={{
                      width: 44, height: 28, borderRadius: 4, background: brand?.accent || TOKENS.brand,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      color: '#fff', font: '700 10px/1 Pretendard', letterSpacing: '0.05em',
                    }}>{brand?.name.slice(0, 3).toUpperCase()}</div>
                    <span style={{ font: '600 16px/1 Pretendard', color: TOKENS.text }}>{c}</span>
                  </div>
                  {sel && <I size={20} stroke={TOKENS.brand}>{Icons.check}</I>}
                </button>
              );
            })}
          </div>
        )}
      </div>

      <div style={{ padding: '20px 24px 32px', background: TOKENS.bg }}>
        <Btn full onClick={next} disabled={!canNext}>
          {step === 2 ? '시작하기' : '다음'}
          {step < 2 && <I size={18}>{Icons.arrowRight}</I>}
        </Btn>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// SCREEN 3 · MAP HOME
// ─────────────────────────────────────────────────────────────
const COURSES = [
  {
    id: 'mishiryeong', name: '미시령 옛길', region: '강원 인제 — 고성',
    distance: 17.4, duration: '24분', difficulty: '★★★★☆', diffNum: 4,
    bestLap: '14:38.220', best: 'NightDriver', participants: 1284,
    elev: 482, corners: 47,
    x: 64, y: 28, accent: '#FFD60A',
    blurb: '동해를 향해 떨어지는 18개의 헤어핀. 한국 와인딩의 원형.',
    poi: [
      { type: 'coffee', name: '미시령 휴게소', dist: '0.4km' },
      { type: 'parking', name: '진부령 전망 주차장', dist: '12km' },
      { type: 'coffee', name: 'Café Loop', dist: '14km' },
    ],
    splits: [
      { lap: 'S1 · 출발 — 시작 헤어핀', t: '02:18.42' },
      { lap: 'S2 · 와인딩 구간', t: '08:44.10' },
      { lap: 'S3 · 다운힐', t: '03:35.70' },
    ],
  },
  {
    id: 'palgong', name: '팔공산 스카이라인', region: '대구 동구',
    distance: 11.2, duration: '17분', difficulty: '★★★☆☆', diffNum: 3,
    bestLap: '09:22.840', best: 'ApexHunter', participants: 892,
    elev: 318, corners: 32, x: 52, y: 56, accent: '#7968EE',
    blurb: '도심에서 30분, 일출 라이드의 정석.',
    poi: [
      { type: 'coffee', name: '관암사 카페', dist: '2km' },
      { type: 'parking', name: '동봉 전망대', dist: '6km' },
    ],
    splits: [
      { lap: 'S1 · 시내 — 입구', t: '01:48.10' },
      { lap: 'S2 · 본 구간', t: '06:10.40' },
      { lap: 'S3 · 정상 진입', t: '01:24.30' },
    ],
  },
  {
    id: 'jirisan', name: '지리산 정령치 루프', region: '전북 남원',
    distance: 22.8, duration: '32분', difficulty: '★★★★★', diffNum: 5,
    bestLap: '21:04.110', best: 'ApexHunter', participants: 612,
    elev: 718, corners: 64, x: 38, y: 72, accent: '#22C55E',
    blurb: '2시간 거리, 평생 기억할 64개의 코너.',
    poi: [
      { type: 'coffee', name: '정령치 휴게소', dist: '11km' },
      { type: 'coffee', name: '운봉 베이커리', dist: '18km' },
    ],
    splits: [
      { lap: 'S1 · 워밍업', t: '04:12.10' },
      { lap: 'S2 · 정령치 본구간', t: '11:28.30' },
      { lap: 'S3 · 다운 + 마을', t: '05:23.71' },
    ],
  },
  {
    id: 'namhae', name: '남해 1024번 도로', region: '경남 남해',
    distance: 28.6, duration: '38분', difficulty: '★★★☆☆', diffNum: 3,
    bestLap: '23:11.300', best: '곡선마스터', participants: 2104,
    elev: 220, corners: 41, x: 28, y: 86, accent: '#F87171',
    blurb: '바다와 동백, 그리고 끝없는 미디엄 코너.',
    poi: [
      { type: 'coffee', name: '독일마을 카페', dist: '8km' },
      { type: 'parking', name: '미조항 주차장', dist: '21km' },
    ],
    splits: [
      { lap: 'S1 · 다리 진입', t: '04:48.40' },
      { lap: 'S2 · 해안 와인딩', t: '13:50.20' },
      { lap: 'S3 · 마을 통과', t: '04:32.70' },
    ],
  },
];

function MapPin({ course, onClick, focused }) {
  return (
    <button onClick={onClick} style={{
      position: 'absolute', left: `${course.x}%`, top: `${course.y}%`,
      transform: 'translate(-50%, -100%)',
      background: 'none', border: 'none', cursor: 'pointer', padding: 0,
      filter: focused ? 'none' : 'none',
      zIndex: focused ? 6 : 4,
    }}>
      <div style={{ position: 'relative' }}>
        <div style={{
          background: focused ? course.accent : TOKENS.bgRaised,
          color: focused ? '#0B0D12' : TOKENS.text,
          padding: '8px 12px', borderRadius: 10,
          font: '700 12px/1 Pretendard', letterSpacing: '-0.01em',
          whiteSpace: 'nowrap', boxShadow: '0 6px 18px rgba(0,0,0,0.5)',
          border: `1px solid ${focused ? course.accent : TOKENS.borderStrong}`,
          display: 'flex', alignItems: 'center', gap: 6,
        }}>
          <div style={{
            width: 6, height: 6, borderRadius: 9999,
            background: focused ? '#0B0D12' : course.accent,
          }}/>
          {course.name}
        </div>
        <div style={{
          width: 0, height: 0, margin: '0 auto',
          borderLeft: '6px solid transparent', borderRight: '6px solid transparent',
          borderTop: `8px solid ${focused ? course.accent : TOKENS.borderStrong}`,
        }}/>
      </div>
    </button>
  );
}

function MapCanvas({ onPick, focused }) {
  return (
    <div style={{
      position: 'absolute', inset: 0,
      background: '#0B0D12', overflow: 'hidden',
    }}>
      {/* base topographic feel */}
      <svg width="100%" height="100%" viewBox="0 0 100 130" preserveAspectRatio="none"
        style={{ position: 'absolute', inset: 0 }}>
        <defs>
          <radialGradient id="glow" cx="50%" cy="20%" r="60%">
            <stop offset="0%" stopColor="#1B1F29"/>
            <stop offset="100%" stopColor="#0B0D12"/>
          </radialGradient>
        </defs>
        <rect width="100" height="130" fill="url(#glow)"/>
        {/* subtle terrain contours */}
        {[15, 30, 50, 70, 90, 110].map(y => (
          <path key={y} d={`M0 ${y} Q 25 ${y-6} 50 ${y} T 100 ${y-2}`}
            stroke="#1B1F29" strokeWidth="0.25" fill="none"/>
        ))}
        {[20, 40, 60, 80, 100, 120].map(y => (
          <path key={`b-${y}`} d={`M0 ${y+3} Q 30 ${y+8} 60 ${y+1} T 100 ${y+5}`}
            stroke="#14171F" strokeWidth="0.2" fill="none"/>
        ))}
        {/* coast */}
        <path d="M 0 95 Q 18 88 30 92 Q 45 100 55 96 Q 70 90 80 100 Q 92 108 100 102 L 100 130 L 0 130 Z"
          fill="#080A0F" stroke="#14171F" strokeWidth="0.3"/>
        {/* roads — courses */}
        <path d="M 60 30 Q 65 38 62 46 Q 58 54 64 60" stroke="#FFD60A" strokeWidth="0.6" fill="none" strokeDasharray="0.8 0.6" opacity="0.85"/>
        <path d="M 50 56 Q 54 60 52 64 Q 49 68 53 72" stroke="#7968EE" strokeWidth="0.6" fill="none" strokeDasharray="0.8 0.6" opacity="0.85"/>
        <path d="M 36 72 Q 32 78 38 84 Q 44 90 38 94" stroke="#22C55E" strokeWidth="0.6" fill="none" strokeDasharray="0.8 0.6" opacity="0.85"/>
        <path d="M 26 86 Q 30 92 24 98 Q 18 104 26 108" stroke="#F87171" strokeWidth="0.6" fill="none" strokeDasharray="0.8 0.6" opacity="0.85"/>
        {/* major highway hint */}
        <path d="M 10 10 L 100 70" stroke="#1B1F29" strokeWidth="1.2" fill="none"/>
        <path d="M 0 50 Q 35 58 70 50 Q 95 45 100 30" stroke="#14171F" strokeWidth="1" fill="none"/>
        {/* faint city dots */}
        {[[18,48],[72,40],[44,108],[88,82]].map(([cx,cy],i)=>(
          <g key={i}>
            <circle cx={cx} cy={cy} r="0.6" fill="#3C404B"/>
          </g>
        ))}
      </svg>

      {/* city labels */}
      {[
        { x: 18, y: 38, name: 'Seoul', sub: '서울' },
        { x: 75, y: 38, name: 'Sokcho', sub: '속초' },
        { x: 50, y: 50, name: 'Daegu', sub: '대구' },
        { x: 36, y: 65, name: 'Jeonju', sub: '전주' },
        { x: 28, y: 80, name: 'Tongyeong', sub: '통영' },
      ].map(c => (
        <div key={c.name} style={{
          position: 'absolute', left: `${c.x}%`, top: `${c.y}%`,
          font: '500 9px/1 Pretendard', color: TOKENS.textTer,
          letterSpacing: '0.16em', textTransform: 'uppercase',
        }}>{c.name}</div>
      ))}

      {COURSES.map(c => (
        <MapPin key={c.id} course={c} onClick={() => onPick(c)} focused={focused === c.id}/>
      ))}
    </div>
  );
}

function MapHome({ user, onCourse, onProfile }) {
  const [focused, setFocused] = useState('mishiryeong');
  const focusedCourse = COURSES.find(c => c.id === focused) || COURSES[0];

  return (
    <div style={{
      width: '100%', height: '100%', background: TOKENS.bg, color: TOKENS.text,
      position: 'relative', overflow: 'hidden',
    }}>
      <MapCanvas onPick={(c) => setFocused(c.id)} focused={focused}/>

      {/* TOP overlay — search + profile */}
      <div style={{
        position: 'absolute', top: 56, left: 0, right: 0, padding: '0 16px',
        display: 'flex', alignItems: 'center', gap: 10, zIndex: 10,
      }}>
        <div style={{
          flex: 1, height: 48, background: 'rgba(20,23,31,0.78)',
          backdropFilter: 'blur(20px)', WebkitBackdropFilter: 'blur(20px)',
          border: `1px solid ${TOKENS.border}`, borderRadius: 14,
          display: 'flex', alignItems: 'center', padding: '0 14px', gap: 10,
        }}>
          <I size={18} stroke={TOKENS.textSec}>{Icons.search}</I>
          <span style={{ font: '500 14px/1 Pretendard', color: TOKENS.textSec }}>코스, 지역, 라이더 검색</span>
        </div>
        <button onClick={onProfile} style={{
          width: 48, height: 48, borderRadius: 14,
          background: 'rgba(20,23,31,0.78)', backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          border: `1px solid ${TOKENS.border}`, cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: TOKENS.text,
        }}>
          <div style={{
            width: 30, height: 30, borderRadius: 9999, background: TOKENS.brand,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: '#fff', font: '700 12px/1 Pretendard',
          }}>{user.nick[0].toUpperCase()}</div>
        </button>
      </div>

      {/* filter chips */}
      <div style={{
        position: 'absolute', top: 116, left: 0, right: 0, padding: '0 16px',
        display: 'flex', gap: 8, zIndex: 10, overflowX: 'auto',
      }}>
        {[
          { l: '내 주변', i: Icons.mapPin, on: true },
          { l: '주말 추천', i: Icons.sparkles },
          { l: '★ 친구', i: Icons.users },
          { l: '난이도 ↑', i: Icons.trendingUp },
        ].map((c, i) => (
          <div key={i} style={{
            height: 36, padding: '0 14px',
            background: c.on ? TOKENS.text : 'rgba(20,23,31,0.78)',
            color: c.on ? '#0B0D12' : TOKENS.textSec,
            backdropFilter: 'blur(20px)',
            border: `1px solid ${c.on ? TOKENS.text : TOKENS.border}`,
            borderRadius: 9999, display: 'flex', alignItems: 'center', gap: 6,
            font: '600 13px/1 Pretendard', whiteSpace: 'nowrap',
          }}>
            <I size={14} stroke={c.on ? '#0B0D12' : TOKENS.textSec}>{c.i}</I>
            {c.l}
          </div>
        ))}
      </div>

      {/* zoom controls */}
      <div style={{
        position: 'absolute', right: 16, top: '40%', display: 'flex', flexDirection: 'column', gap: 8,
        zIndex: 10,
      }}>
        {['+', '−'].map(s => (
          <button key={s} style={{
            width: 44, height: 44, borderRadius: 12,
            background: 'rgba(20,23,31,0.85)', backdropFilter: 'blur(20px)',
            border: `1px solid ${TOKENS.border}`, color: TOKENS.text,
            font: '500 22px/1 Pretendard', cursor: 'pointer',
          }}>{s}</button>
        ))}
        <button style={{
          width: 44, height: 44, borderRadius: 12,
          background: TOKENS.brand, color: '#fff', border: 'none',
          cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <I size={20}>{Icons.navigation}</I>
        </button>
      </div>

      {/* BOTTOM card — focused course peek */}
      <div style={{
        position: 'absolute', left: 12, right: 12, bottom: 24,
        background: TOKENS.bgRaised,
        borderRadius: 22, padding: 16, zIndex: 10,
        border: `1px solid ${TOKENS.border}`,
        boxShadow: '0 24px 48px rgba(0,0,0,0.6)',
      }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 12,
            background: `linear-gradient(135deg, ${focusedCourse.accent}, ${focusedCourse.accent}66)`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0,
          }}>
            <I size={28} stroke="#0B0D12">{Icons.mountain}</I>
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{
              font: '500 11px/1 Pretendard', letterSpacing: '0.16em',
              color: TOKENS.textTer, textTransform: 'uppercase', marginBottom: 4,
            }}>{focusedCourse.region}</div>
            <div style={{
              font: '700 19px/1.2 Pretendard', letterSpacing: '-0.02em',
              color: TOKENS.text,
            }}>{focusedCourse.name}</div>
            <div style={{
              display: 'flex', gap: 14, marginTop: 8, font: '500 12px/1 Pretendard',
              color: TOKENS.textSec,
            }}>
              <span><span style={{ color: TOKENS.text, fontWeight: 700 }}>{focusedCourse.distance}</span>km</span>
              <span style={{ color: TOKENS.amber }}>{focusedCourse.difficulty}</span>
              <span><span style={{ color: TOKENS.text, fontWeight: 700 }} className="tabular-nums">{focusedCourse.bestLap}</span></span>
            </div>
          </div>
        </div>
        <Btn full style={{ marginTop: 14 }} onClick={() => onCourse(focusedCourse)}>
          코스 자세히 보기
          <I size={18}>{Icons.arrowRight}</I>
        </Btn>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// SCREEN 4 · COURSE DETAIL
// ─────────────────────────────────────────────────────────────
const RANK = [
  { rank: 1, nick: 'NightDriver', car: 'BMW M4 CSL', time: '14:38.220', delta: null, brand: 'bmw' },
  { rank: 2, nick: 'ApexHunter', car: 'Porsche 911 GT3', time: '14:42.880', delta: '+04.660', brand: 'porsche' },
  { rank: 3, nick: '곡선마스터', car: 'Hyundai 아이오닉 5 N', time: '14:51.040', delta: '+12.820', brand: 'hyundai' },
  { rank: 4, nick: '0to100', car: 'AMG GT', time: '14:58.310', delta: '+20.090', brand: 'mb' },
  { rank: 5, nick: 'WindingKim', car: 'Audi RS5', time: '15:02.770', delta: '+24.550', brand: 'audi' },
  { rank: 6, nick: 'TrackDad', car: 'Tesla Model 3 P', time: '15:11.420', delta: '+33.200', brand: 'tesla' },
];

function PodiumStep({ rank, nick, time, accent, height }) {
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10,
    }}>
      <div style={{
        width: 56, height: 56, borderRadius: 9999,
        background: accent, color: '#0B0D12',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        font: '800 22px/1 Pretendard', position: 'relative',
        border: `3px solid ${TOKENS.bg}`,
        boxShadow: `0 0 0 2px ${accent}`,
      }}>{rank}
      </div>
      <div style={{
        font: '700 13px/1.2 Pretendard', color: TOKENS.text, textAlign: 'center',
        maxWidth: 100, overflow: 'hidden', textOverflow: 'ellipsis',
      }}>{nick}</div>
      <div className="tabular-nums" style={{
        font: '600 12px/1 Pretendard', color: TOKENS.textSec,
      }}>{time}</div>
      <div style={{
        height, width: '100%', maxWidth: 88,
        background: `linear-gradient(180deg, ${accent}, ${accent}33)`,
        borderRadius: '8px 8px 0 0',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        font: '900 28px/1 Pretendard', color: '#0B0D12',
      }}>{rank}</div>
    </div>
  );
}

function CourseDetail({ course, onBack, onJoin }) {
  return (
    <div style={{
      width: '100%', height: '100%', background: TOKENS.bg, color: TOKENS.text,
      overflowY: 'auto', position: 'relative',
    }}>
      {/* HERO */}
      <div style={{
        position: 'relative', height: 320,
        background: `linear-gradient(180deg, ${course.accent}11 0%, ${TOKENS.bg} 100%)`,
        borderBottom: `1px solid ${TOKENS.border}`,
      }}>
        {/* placeholder magazine "photo" */}
        <div style={{
          position: 'absolute', inset: 0,
          background: `linear-gradient(135deg, ${course.accent}33 0%, transparent 50%, ${course.accent}11 100%), radial-gradient(ellipse at 30% 70%, ${course.accent}66 0%, transparent 50%)`,
        }}/>
        <svg width="100%" height="100%" viewBox="0 0 100 60" preserveAspectRatio="none"
          style={{ position: 'absolute', inset: 0, opacity: 0.55 }}>
          <path d="M 0 50 Q 20 40 35 45 Q 50 50 60 35 Q 70 20 85 28 Q 95 32 100 25 L 100 60 L 0 60 Z" fill="#0B0D12"/>
          <path d="M 0 45 Q 18 35 32 40 Q 48 47 60 30 Q 70 15 90 22 L 100 20 L 100 60 L 0 60 Z" fill="#14171F" opacity="0.7"/>
        </svg>

        <button onClick={onBack} style={{
          position: 'absolute', top: 64, left: 16, width: 44, height: 44,
          borderRadius: 14, background: 'rgba(11,13,18,0.65)',
          backdropFilter: 'blur(14px)', WebkitBackdropFilter: 'blur(14px)',
          border: `1px solid ${TOKENS.border}`, color: TOKENS.text, cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <I size={18}>{Icons.arrowLeft}</I>
        </button>

        {/* badge */}
        <div style={{
          position: 'absolute', top: 64, right: 16,
          background: 'rgba(11,13,18,0.65)',
          backdropFilter: 'blur(14px)', WebkitBackdropFilter: 'blur(14px)',
          border: `1px solid ${TOKENS.border}`, borderRadius: 9999,
          padding: '8px 14px', font: '600 12px/1 Pretendard',
          display: 'flex', alignItems: 'center', gap: 6, color: TOKENS.amber,
        }}>
          <I size={14} stroke={TOKENS.amber} fill={TOKENS.amber}>{Icons.star}</I>
          EDITORS' PICK
        </div>

        {/* title block */}
        <div style={{ position: 'absolute', left: 24, right: 24, bottom: 22 }}>
          <div style={{
            font: '500 11px/1 Pretendard', letterSpacing: '0.24em',
            color: course.accent, textTransform: 'uppercase', marginBottom: 8,
          }}>{course.region}</div>
          <h1 style={{
            font: '800 36px/1.05 Pretendard', letterSpacing: '-0.035em', color: TOKENS.text,
          }}>{course.name}</h1>
          <p style={{ marginTop: 8, font: '400 14px/1.55 Pretendard', color: TOKENS.textSec, maxWidth: 320 }}>{course.blurb}</p>
        </div>
      </div>

      {/* QUICK STATS */}
      <div style={{
        display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 1,
        background: TOKENS.border, borderBottom: `1px solid ${TOKENS.border}`,
      }}>
        {[
          { l: '거리', v: course.distance, u: 'km' },
          { l: '주행 시간', v: course.duration, u: '' },
          { l: '코너', v: course.corners, u: '개' },
          { l: '고도', v: course.elev, u: 'm' },
        ].map((s, i) => (
          <div key={i} style={{
            background: TOKENS.bg, padding: '18px 12px', textAlign: 'center',
          }}>
            <div className="tabular-nums" style={{
              font: '700 22px/1 Pretendard', letterSpacing: '-0.02em', color: TOKENS.text,
            }}>{s.v}</div>
            <div style={{ font: '500 11px/1.4 Pretendard', color: TOKENS.textTer, marginTop: 6, letterSpacing: '0.04em' }}>{s.l}{s.u && <span style={{ color: TOKENS.textTer }}> ({s.u})</span>}</div>
          </div>
        ))}
      </div>

      {/* BEST LAP CARD */}
      <div style={{ padding: '28px 16px 0' }}>
        <div style={{
          background: `linear-gradient(135deg, ${TOKENS.bgElevated}, ${TOKENS.bgRaised})`,
          border: `1px solid ${course.accent}55`, borderRadius: 18, padding: 18,
          display: 'flex', alignItems: 'center', gap: 14,
        }}>
          <div style={{
            width: 52, height: 52, borderRadius: 12,
            background: `linear-gradient(135deg, ${course.accent}, ${course.accent}99)`,
            display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#0B0D12',
          }}>
            <I size={26} stroke="#0B0D12">{Icons.trophy}</I>
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, letterSpacing: '0.16em', textTransform: 'uppercase' }}>코스 베스트 랩</div>
            <div className="tabular-nums" style={{ font: '800 26px/1.1 Pretendard', letterSpacing: '-0.02em', marginTop: 6, color: TOKENS.text }}>{course.bestLap}</div>
            <div style={{ font: '500 12px/1 Pretendard', color: TOKENS.textSec, marginTop: 6 }}>by <strong style={{ color: TOKENS.text }}>{course.best}</strong> · 3일 전</div>
          </div>
        </div>
      </div>

      {/* ELEVATION GRAPH */}
      <div style={{ padding: '24px 16px 0' }}>
        <div style={{ font: '700 18px/1.2 Pretendard', letterSpacing: '-0.02em', marginBottom: 14, color: TOKENS.text }}>고도 / 굴곡 프로파일</div>
        <div style={{
          background: TOKENS.bgRaised, borderRadius: 16, padding: 18,
          border: `1px solid ${TOKENS.border}`,
        }}>
          <svg viewBox="0 0 300 80" width="100%" height="80">
            <defs>
              <linearGradient id="elevGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={course.accent} stopOpacity="0.4"/>
                <stop offset="100%" stopColor={course.accent} stopOpacity="0"/>
              </linearGradient>
            </defs>
            <path d="M 0 60 Q 30 50 50 35 T 100 22 Q 130 18 160 30 T 220 18 Q 250 14 280 28 L 300 38 L 300 80 L 0 80 Z" fill="url(#elevGrad)"/>
            <path d="M 0 60 Q 30 50 50 35 T 100 22 Q 130 18 160 30 T 220 18 Q 250 14 280 28 L 300 38" stroke={course.accent} strokeWidth="2" fill="none" strokeLinecap="round"/>
            {[60, 25, 40, 18, 35].map((y, i) => (
              <circle key={i} cx={50 + i * 60} cy={y} r="3" fill={course.accent}/>
            ))}
          </svg>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8, font: '500 11px/1 Pretendard', color: TOKENS.textTer }}>
            <span>0km</span><span>{(course.distance / 4).toFixed(1)}km</span>
            <span>{(course.distance / 2).toFixed(1)}km</span><span>{(course.distance * 0.75).toFixed(1)}km</span>
            <span>{course.distance}km</span>
          </div>
        </div>
      </div>

      {/* RANKING — podium + list */}
      <div style={{ padding: '32px 16px 0' }}>
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 18 }}>
          <div style={{ font: '700 20px/1.2 Pretendard', letterSpacing: '-0.02em' }}>이 코스의 랭킹</div>
          <span style={{ font: '500 12px/1 Pretendard', color: TOKENS.textTer }}>참여 {course.participants.toLocaleString()}명</span>
        </div>

        {/* podium */}
        <div style={{
          display: 'flex', alignItems: 'flex-end', gap: 8,
          background: TOKENS.bgRaised, borderRadius: 16, padding: '24px 16px 0',
          border: `1px solid ${TOKENS.border}`,
        }}>
          <PodiumStep rank={2} nick={RANK[1].nick} time={RANK[1].time} accent="#B1B6C4" height={64}/>
          <PodiumStep rank={1} nick={RANK[0].nick} time={RANK[0].time} accent={TOKENS.amber} height={92}/>
          <PodiumStep rank={3} nick={RANK[2].nick} time={RANK[2].time} accent="#B68B00" height={48}/>
        </div>

        {/* tabs */}
        <div style={{
          display: 'flex', gap: 6, marginTop: 18, padding: 4,
          background: TOKENS.bgRaised, borderRadius: 12, border: `1px solid ${TOKENS.border}`,
        }}>
          {['전체', '내 차종', '친구', '이번 주'].map((t, i) => (
            <div key={t} style={{
              flex: 1, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'center',
              borderRadius: 9, font: '600 13px/1 Pretendard',
              background: i === 0 ? TOKENS.bgElevated : 'transparent',
              color: i === 0 ? TOKENS.text : TOKENS.textTer,
              border: i === 0 ? `1px solid ${TOKENS.border}` : 'none',
            }}>{t}</div>
          ))}
        </div>

        {/* list */}
        <div style={{
          marginTop: 12, background: TOKENS.bgRaised, borderRadius: 16,
          border: `1px solid ${TOKENS.border}`, overflow: 'hidden',
        }}>
          {RANK.map((r, i) => (
            <div key={r.rank} style={{
              display: 'flex', alignItems: 'center', padding: '14px 16px', gap: 12,
              borderBottom: i < RANK.length - 1 ? `1px solid ${TOKENS.border}` : 'none',
            }}>
              <div className="tabular-nums" style={{
                width: 24, font: '700 14px/1 Pretendard', color: TOKENS.textTer, textAlign: 'right',
              }}>{r.rank}</div>
              <div style={{
                width: 36, height: 36, borderRadius: 9999, background: TOKENS.brand,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: '#fff', font: '700 13px/1 Pretendard', flexShrink: 0,
              }}>{r.nick[0]}</div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ font: '600 14px/1.2 Pretendard', color: TOKENS.text, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.nick}</div>
                <div style={{ font: '500 11px/1.2 Pretendard', color: TOKENS.textTer, marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.car}</div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div className="tabular-nums" style={{ font: '700 14px/1 Pretendard', color: TOKENS.text }}>{r.time}</div>
                {r.delta && (
                  <div className="tabular-nums" style={{ font: '500 11px/1 Pretendard', color: TOKENS.red, marginTop: 4 }}>{r.delta}</div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* POI — 주변 명소 */}
      <div style={{ padding: '32px 16px 0' }}>
        <div style={{ font: '700 20px/1.2 Pretendard', letterSpacing: '-0.02em', marginBottom: 14 }}>주변 명소</div>
        <div style={{ display: 'flex', gap: 10, overflowX: 'auto', paddingBottom: 8, marginRight: -16 }}>
          {course.poi.map((p, i) => (
            <div key={i} style={{
              minWidth: 180, background: TOKENS.bgRaised, borderRadius: 14,
              border: `1px solid ${TOKENS.border}`, padding: 14,
            }}>
              <div style={{
                width: 36, height: 36, borderRadius: 10, background: TOKENS.bgElevated,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: TOKENS.amber,
              }}>
                <I size={18} stroke={TOKENS.amber}>{p.type === 'coffee' ? Icons.coffee : Icons.parking}</I>
              </div>
              <div style={{ font: '600 14px/1.2 Pretendard', color: TOKENS.text, marginTop: 12 }}>{p.name}</div>
              <div style={{ font: '500 12px/1 Pretendard', color: TOKENS.textTer, marginTop: 6 }}>{p.dist}</div>
            </div>
          ))}
        </div>
      </div>

      {/* spacer for sticky footer */}
      <div style={{ height: 140 }}/>

      {/* STICKY ACTION FOOTER */}
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 0,
        padding: '14px 16px 32px',
        background: 'linear-gradient(180deg, transparent 0%, rgba(11,13,18,0.95) 30%, rgba(11,13,18,1) 100%)',
        display: 'flex', gap: 10,
      }}>
        <Btn variant="secondary" style={{ flex: 1 }} onClick={() => onJoin('info')}>
          <I size={18}>{Icons.flag}</I>
          참여 안내
        </Btn>
        <Btn style={{ flex: 1.4 }} onClick={() => onJoin('start')}>
          <I size={18}>{Icons.zap}</I>
          랭킹전 참여
        </Btn>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// RANKING JOIN BOTTOM SHEET (rules + start)
// ─────────────────────────────────────────────────────────────
function JoinSheet({ course, mode, onClose, onStart }) {
  return (
    <div style={{
      position: 'absolute', inset: 0, zIndex: 100,
    }}>
      {/* backdrop */}
      <div onClick={onClose} style={{
        position: 'absolute', inset: 0,
        background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)',
        animation: 'fadeIn 200ms',
      }}/>
      {/* sheet */}
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 0,
        background: TOKENS.bg, borderRadius: '28px 28px 0 0',
        padding: '12px 24px 32px', maxHeight: '85%', overflowY: 'auto',
        animation: 'slideUp 320ms cubic-bezier(0.2,0,0,1)',
        border: `1px solid ${TOKENS.border}`, borderBottom: 'none',
      }}>
        <div style={{
          width: 36, height: 4, background: TOKENS.borderStrong,
          borderRadius: 9999, margin: '0 auto 18px',
        }}/>

        <div style={{
          font: '500 11px/1 Pretendard', letterSpacing: '0.2em',
          color: course.accent, textTransform: 'uppercase', marginBottom: 6,
        }}>참여 안내</div>
        <h2 style={{
          font: '800 26px/1.15 Pretendard', letterSpacing: '-0.025em',
        }}>{course.name}</h2>
        <p style={{ marginTop: 8, font: '400 14px/1.55 Pretendard', color: TOKENS.textSec }}>
          {mode === 'info' ? '아래 룰을 확인하고 시작해요. 안전이 가장 큰 규칙이에요.' : '시작 라인을 통과하면 자동으로 측정이 시작돼요.'}
        </p>

        {/* rules */}
        <div style={{ marginTop: 24, display: 'flex', flexDirection: 'column', gap: 12 }}>
          {[
            { ic: Icons.lock, t: '제한 속도를 지켜요', d: '도로 법규 준수가 모든 랭킹의 전제예요. 위반 감지 시 기록은 무효 처리돼요.' },
            { ic: Icons.flag, t: '시작/종료 자동 감지', d: '구간 진입과 이탈을 GPS로 인식해서 따로 버튼을 누를 필요가 없어요.' },
            { ic: Icons.trophy, t: '클래스별로 비교돼요', d: `${course.name} 랭킹은 차종 클래스(EV / 4기통 / 6기통+)별로 별도 집계돼요.` },
            { ic: Icons.users, t: '동승자가 있어도 OK', d: '단, 한 번의 주행 = 한 명의 기록. 핸들을 잡은 사람의 차로 등록돼요.' },
          ].map((r, i) => (
            <div key={i} style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
              <div style={{
                width: 36, height: 36, borderRadius: 10, background: TOKENS.bgRaised,
                border: `1px solid ${TOKENS.border}`, color: course.accent,
                display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
              }}>
                <I size={18} stroke={course.accent}>{r.ic}</I>
              </div>
              <div>
                <div style={{ font: '700 15px/1.3 Pretendard', color: TOKENS.text }}>{r.t}</div>
                <div style={{ font: '400 13px/1.5 Pretendard', color: TOKENS.textSec, marginTop: 4 }}>{r.d}</div>
              </div>
            </div>
          ))}
        </div>

        {/* current pb */}
        <div style={{
          marginTop: 22, padding: 14, background: TOKENS.bgRaised, borderRadius: 14,
          border: `1px solid ${TOKENS.border}`, display: 'flex', alignItems: 'center', gap: 12,
        }}>
          <I size={18} stroke={TOKENS.amber}>{Icons.trophy}</I>
          <div style={{ flex: 1, font: '500 13px/1.4 Pretendard', color: TOKENS.textSec }}>
            도전할 베스트 랩
          </div>
          <div className="tabular-nums" style={{ font: '700 16px/1 Pretendard', color: TOKENS.text }}>{course.bestLap}</div>
        </div>

        <Btn full style={{ marginTop: 22 }} onClick={onStart}>
          <I size={18}>{Icons.zap}</I>
          시작하기
        </Btn>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// SCREEN 5 · DRIVING (live)
// ─────────────────────────────────────────────────────────────
function DrivingScreen({ course, user, onFinish }) {
  const [t, setT] = useState(0); // tenths of seconds
  const [speed, setSpeed] = useState(0);
  const [progress, setProgress] = useState(0);
  const animRef = useRef();
  const startedRef = useRef(Date.now());

  useEffect(() => {
    const tick = () => {
      const elapsed = (Date.now() - startedRef.current) / 1000;
      setT(elapsed);
      // sim speed: smooth wave
      setSpeed(72 + 38 * Math.sin(elapsed / 1.7) + 10 * Math.sin(elapsed * 1.3));
      setProgress(Math.min(elapsed / 12, 1)); // 12s sim
      if (elapsed < 12) animRef.current = requestAnimationFrame(tick);
      else onFinish({
        time: formatTime(elapsed * 73), // make it feel like 14:30 range
        course, user,
      });
    };
    animRef.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(animRef.current);
  }, []);

  const fmt = formatTime(t * 73);
  const target = 14 * 60 + 38.22;
  const projected = t * 73;
  const delta = projected - target;

  return (
    <div style={{
      width: '100%', height: '100%', background: '#000', color: TOKENS.text,
      position: 'relative', overflow: 'hidden',
    }}>
      {/* Live map background */}
      <div style={{
        position: 'absolute', inset: 0,
        background: `radial-gradient(ellipse 80% 50% at 50% 50%, #14171F 0%, #000 70%)`,
      }}>
        <svg width="100%" height="100%" viewBox="0 0 100 130" preserveAspectRatio="none">
          {/* the route */}
          <path d="M 50 110 Q 40 90 50 75 Q 65 60 50 45 Q 35 30 50 15"
            stroke={TOKENS.border} strokeWidth="2.5" fill="none" strokeLinecap="round"/>
          <path d="M 50 110 Q 40 90 50 75 Q 65 60 50 45 Q 35 30 50 15"
            stroke={course.accent} strokeWidth="2.5" fill="none" strokeLinecap="round"
            strokeDasharray="200" strokeDashoffset={200 - progress * 200}/>
          {/* current location dot */}
          <circle cx="50" cy={110 - progress * 95} r="2" fill="#fff"/>
          <circle cx="50" cy={110 - progress * 95} r="4" fill="none" stroke={course.accent} strokeWidth="1">
            <animate attributeName="r" values="4;8;4" dur="1.5s" repeatCount="indefinite"/>
            <animate attributeName="opacity" values="1;0;1" dur="1.5s" repeatCount="indefinite"/>
          </circle>
        </svg>
      </div>

      {/* TOP HUD — course + delta */}
      <div style={{
        position: 'absolute', top: 60, left: 16, right: 16,
        display: 'flex', alignItems: 'center', gap: 12,
      }}>
        <div style={{
          flex: 1, padding: '10px 14px',
          background: 'rgba(11,13,18,0.7)', backdropFilter: 'blur(20px)',
          border: `1px solid ${TOKENS.border}`, borderRadius: 14,
        }}>
          <div style={{ font: '500 10px/1 Pretendard', color: TOKENS.textTer, letterSpacing: '0.16em', textTransform: 'uppercase' }}>NOW DRIVING</div>
          <div style={{ font: '700 14px/1.2 Pretendard', color: TOKENS.text, marginTop: 4 }}>{course.name}</div>
        </div>
        <div style={{
          padding: '10px 14px', textAlign: 'right',
          background: delta < 0 ? 'rgba(34,197,94,0.18)' : 'rgba(248,113,113,0.18)',
          border: `1px solid ${delta < 0 ? TOKENS.green : TOKENS.red}`,
          backdropFilter: 'blur(20px)',
          borderRadius: 14,
        }}>
          <div style={{ font: '500 10px/1 Pretendard', color: TOKENS.textSec, letterSpacing: '0.12em', textTransform: 'uppercase' }}>VS BEST</div>
          <div className="tabular-nums" style={{
            font: '700 16px/1 Pretendard', color: delta < 0 ? TOKENS.green : TOKENS.red, marginTop: 4,
          }}>{delta < 0 ? '−' : '+'}{Math.abs(delta).toFixed(2).replace('.', '.')}s</div>
        </div>
      </div>

      {/* CENTER · giant lap time */}
      <div style={{
        position: 'absolute', top: '32%', left: 0, right: 0, textAlign: 'center',
      }}>
        <div style={{
          font: '500 11px/1 Pretendard', color: TOKENS.textSec, letterSpacing: '0.32em',
          textTransform: 'uppercase', marginBottom: 14,
        }}>ELAPSED</div>
        <div className="tabular-nums" style={{
          font: '900 84px/1 Pretendard', letterSpacing: '-0.04em',
          color: TOKENS.text,
        }}>{fmt}</div>
        <div style={{ marginTop: 18, display: 'flex', justifyContent: 'center', gap: 36 }}>
          <div>
            <div className="tabular-nums" style={{ font: '700 32px/1 Pretendard', color: TOKENS.text }}>
              {speed.toFixed(0)}<span style={{ font: '500 14px/1 Pretendard', color: TOKENS.textTer, marginLeft: 4 }}>km/h</span>
            </div>
            <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, marginTop: 6, letterSpacing: '0.12em' }}>SPEED</div>
          </div>
          <div style={{ width: 1, background: TOKENS.border }}/>
          <div>
            <div className="tabular-nums" style={{ font: '700 32px/1 Pretendard', color: TOKENS.text }}>
              {(progress * course.distance).toFixed(1)}<span style={{ font: '500 14px/1 Pretendard', color: TOKENS.textTer, marginLeft: 4 }}>km</span>
            </div>
            <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, marginTop: 6, letterSpacing: '0.12em' }}>DISTANCE</div>
          </div>
        </div>
      </div>

      {/* BOTTOM · sector splits + progress */}
      <div style={{
        position: 'absolute', left: 16, right: 16, bottom: 110,
        background: 'rgba(11,13,18,0.7)', backdropFilter: 'blur(20px)',
        border: `1px solid ${TOKENS.border}`, borderRadius: 18, padding: 16,
      }}>
        <div style={{ font: '600 11px/1 Pretendard', color: TOKENS.textTer, letterSpacing: '0.16em', textTransform: 'uppercase', marginBottom: 12 }}>SECTORS</div>
        <div style={{ display: 'flex', gap: 8 }}>
          {course.splits.map((s, i) => {
            const reached = progress > (i + 1) / course.splits.length;
            const active = !reached && progress > i / course.splits.length;
            return (
              <div key={i} style={{
                flex: 1, padding: 10, borderRadius: 10,
                background: reached ? 'rgba(34,197,94,0.14)' : active ? `${course.accent}22` : TOKENS.bgRaised,
                border: `1px solid ${reached ? TOKENS.green : active ? course.accent : TOKENS.border}`,
              }}>
                <div style={{ font: '600 10px/1 Pretendard', color: TOKENS.textTer, letterSpacing: '0.06em' }}>
                  {s.lap.split(' · ')[0]}
                </div>
                <div className="tabular-nums" style={{
                  font: '700 13px/1 Pretendard', marginTop: 6,
                  color: reached ? TOKENS.green : active ? course.accent : TOKENS.textSec,
                }}>{reached ? s.t : '—'}</div>
              </div>
            );
          })}
        </div>
      </div>

      {/* ABORT */}
      <div style={{
        position: 'absolute', left: 16, right: 16, bottom: 48,
        display: 'flex', justifyContent: 'center',
      }}>
        <button onClick={() => onFinish(null)} style={{
          padding: '12px 28px', borderRadius: 9999, background: 'rgba(11,13,18,0.7)',
          backdropFilter: 'blur(20px)', border: `1px solid ${TOKENS.border}`,
          color: TOKENS.textSec, font: '600 13px/1 Pretendard', cursor: 'pointer',
          display: 'flex', alignItems: 'center', gap: 8,
        }}>
          <I size={14} stroke={TOKENS.textSec}>{Icons.close}</I>
          주행 중단
        </button>
      </div>
    </div>
  );
}

function formatTime(secs) {
  const m = Math.floor(secs / 60).toString().padStart(2, '0');
  const s = (secs % 60).toFixed(2).padStart(5, '0');
  return `${m}:${s}`;
}

// ─────────────────────────────────────────────────────────────
// SCREEN 6 · RESULTS
// ─────────────────────────────────────────────────────────────
function ResultsScreen({ result, onHome, onRetry }) {
  if (!result) return null;
  const { time, course, user } = result;
  const myRank = 4;
  return (
    <div style={{
      width: '100%', height: '100%',
      background: `radial-gradient(ellipse 100% 60% at 50% 0%, ${course.accent}22, transparent 60%), ${TOKENS.bg}`,
      color: TOKENS.text, padding: '64px 24px 32px',
      boxSizing: 'border-box', display: 'flex', flexDirection: 'column', overflowY: 'auto',
    }}>
      {/* medal */}
      <div style={{ textAlign: 'center', marginTop: 18 }}>
        <div style={{
          width: 100, height: 100, margin: '0 auto', borderRadius: 9999,
          background: `linear-gradient(135deg, ${course.accent}, ${course.accent}66)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative',
          boxShadow: `0 0 0 6px ${course.accent}22, 0 0 0 10px ${course.accent}11`,
        }}>
          <I size={56} stroke="#0B0D12">{Icons.trophy}</I>
        </div>
        <div style={{
          font: '500 11px/1 Pretendard', letterSpacing: '0.32em',
          color: course.accent, textTransform: 'uppercase', marginTop: 22,
        }}>NEW PERSONAL BEST</div>
        <h1 style={{
          font: '800 32px/1.1 Pretendard', letterSpacing: '-0.03em', marginTop: 12,
        }}>완주했어요</h1>
        <p style={{ font: '400 14px/1.55 Pretendard', color: TOKENS.textSec, marginTop: 8 }}>
          {course.name}을(를) 완주하고 새 기록을 세웠어요.
        </p>
      </div>

      {/* time card */}
      <div style={{
        marginTop: 28, padding: '24px 20px', borderRadius: 20,
        background: TOKENS.bgRaised, border: `1px solid ${TOKENS.border}`, textAlign: 'center',
      }}>
        <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, letterSpacing: '0.16em', textTransform: 'uppercase' }}>FINAL LAP</div>
        <div className="tabular-nums" style={{
          font: '900 56px/1 Pretendard', letterSpacing: '-0.035em', marginTop: 12,
        }}>{time}</div>
        <div style={{ marginTop: 16, display: 'flex', justifyContent: 'center', gap: 18 }}>
          <div style={{ textAlign: 'center' }}>
            <div className="tabular-nums" style={{ font: '700 20px/1 Pretendard', color: course.accent }}>#{myRank}</div>
            <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, marginTop: 4, letterSpacing: '0.04em' }}>전체 순위</div>
          </div>
          <div style={{ width: 1, background: TOKENS.border }}/>
          <div style={{ textAlign: 'center' }}>
            <div className="tabular-nums" style={{ font: '700 20px/1 Pretendard', color: TOKENS.text }}>+20.09</div>
            <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, marginTop: 4, letterSpacing: '0.04em' }}>VS 1등</div>
          </div>
          <div style={{ width: 1, background: TOKENS.border }}/>
          <div style={{ textAlign: 'center' }}>
            <div className="tabular-nums" style={{ font: '700 20px/1 Pretendard', color: TOKENS.green }}>−1.4s</div>
            <div style={{ font: '500 11px/1 Pretendard', color: TOKENS.textTer, marginTop: 4, letterSpacing: '0.04em' }}>VS 내 PB</div>
          </div>
        </div>
      </div>

      {/* badges */}
      <div style={{ marginTop: 22 }}>
        <div style={{ font: '600 11px/1 Pretendard', color: TOKENS.textTer, letterSpacing: '0.16em', textTransform: 'uppercase', marginBottom: 10 }}>EARNED</div>
        <div style={{ display: 'flex', gap: 10 }}>
          {[
            { l: 'TOP 5', sub: '신규 진입', c: course.accent },
            { l: 'PB', sub: '−1.4s', c: TOKENS.green },
            { l: '+128 XP', sub: '레벨 12', c: TOKENS.brand },
          ].map((b, i) => (
            <div key={i} style={{
              flex: 1, padding: 14, borderRadius: 14,
              background: TOKENS.bgRaised, border: `1px solid ${b.c}55`,
              display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center',
            }}>
              <div style={{ font: '800 16px/1 Pretendard', color: b.c }}>{b.l}</div>
              <div style={{ font: '500 11px/1.2 Pretendard', color: TOKENS.textSec, marginTop: 6 }}>{b.sub}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ flex: 1, minHeight: 16 }}/>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <Btn full onClick={onHome}>지도로 돌아가기</Btn>
        <Btn full variant="secondary" onClick={onRetry}>한 번 더 도전하기</Btn>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// APP — flow controller
// ─────────────────────────────────────────────────────────────
function App() {
  const [tweaks, setTweak] = useTweaks(/*EDITMODE-BEGIN*/{
    "accent": "#4F46E5",
    "mapStyle": "topo",
    "showHud": true
  }/*EDITMODE-END*/);

  const [screen, setScreen] = useState('login'); // login | signup | map | course | drive | results
  const [user, setUser] = useState({ nick: 'ApexHunter', brand: BRANDS[2], car: '911 GT3' });
  const [activeCourse, setActiveCourse] = useState(null);
  const [sheet, setSheet] = useState(null);
  const [result, setResult] = useState(null);

  const screenLabel = ({
    login: '01 Login', signup: '02 Signup', map: '03 Map Home',
    course: '04 Course Detail', drive: '05 Driving', results: '06 Results',
  })[screen];

  return (
    <>
      <style>{`
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
        ::-webkit-scrollbar { display: none; }
        * { -webkit-tap-highlight-color: transparent; }
      `}</style>

      <div data-screen-label={screenLabel} style={{
        position: 'absolute', inset: 0, background: TOKENS.bg, overflow: 'hidden',
      }}>
        {screen === 'login' && (
          <LoginScreen
            onLogin={() => setScreen('map')}
            onSignup={() => setScreen('signup')}
          />
        )}
        {screen === 'signup' && (
          <SignupScreen
            onBack={() => setScreen('login')}
            onDone={(u) => { setUser(u); setScreen('map'); }}
          />
        )}
        {screen === 'map' && (
          <MapHome
            user={user}
            onCourse={(c) => { setActiveCourse(c); setScreen('course'); }}
            onProfile={() => {}}
          />
        )}
        {screen === 'course' && activeCourse && (
          <CourseDetail
            course={activeCourse}
            onBack={() => setScreen('map')}
            onJoin={(mode) => setSheet(mode)}
          />
        )}
        {screen === 'drive' && activeCourse && (
          <DrivingScreen
            course={activeCourse}
            user={user}
            onFinish={(r) => { setResult(r); setScreen('results'); }}
          />
        )}
        {screen === 'results' && result && (
          <ResultsScreen
            result={result}
            onHome={() => { setResult(null); setScreen('map'); }}
            onRetry={() => { setResult(null); setScreen('drive'); }}
          />
        )}
        {sheet && activeCourse && (
          <JoinSheet
            course={activeCourse}
            mode={sheet}
            onClose={() => setSheet(null)}
            onStart={() => { setSheet(null); setScreen('drive'); }}
          />
        )}
      </div>

      {/* TWEAKS */}
      <TweaksPanel title="Tweaks">
        <TweakSection title="Flow">
          <TweakSelect
            label="Jump to screen"
            value={screen}
            onChange={(v) => setScreen(v)}
            options={[
              { value: 'login', label: '01 · Login' },
              { value: 'signup', label: '02 · Signup' },
              { value: 'map', label: '03 · Map Home' },
              { value: 'course', label: '04 · Course Detail' },
              { value: 'drive', label: '05 · Driving' },
              { value: 'results', label: '06 · Results' },
            ]}
          />
          <TweakButton onClick={() => {
            // pre-load a course before jumping into detail/driving/results
            if (!activeCourse) setActiveCourse(COURSES[0]);
            if (!result) setResult({ time: '14:36.820', course: COURSES[0], user });
          }}>코스/결과 데이터 채우기</TweakButton>
        </TweakSection>
        <TweakSection title="Course">
          <TweakSelect
            label="Active course"
            value={activeCourse?.id || COURSES[0].id}
            onChange={(id) => setActiveCourse(COURSES.find(c => c.id === id))}
            options={COURSES.map(c => ({ value: c.id, label: c.name }))}
          />
        </TweakSection>
      </TweaksPanel>
    </>
  );
}

// Make components available globally
Object.assign(window, { App });
