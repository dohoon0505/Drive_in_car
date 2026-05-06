/**
 * Drive in Car — Firestore seed script.
 *
 * Usage:
 *   1. Firebase Console → 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성
 *   2. 다운로드한 JSON을 ../serviceAccountKey.json 으로 저장
 *   3. cd firebase/seed && npm install && npm run seed
 *
 * Optional flags:
 *   --only=courses    courses 컬렉션만 시드
 *   --only=meta       meta/* 만 시드
 */

import * as admin from 'firebase-admin';
import * as path from 'path';
import coursesSeed from './courses.seed.json';

const PROJECT_ID = 'drive-3c0fd';

const onlyArg = process.argv.find((a) => a.startsWith('--only='));
const only = onlyArg ? onlyArg.split('=')[1] : 'all';

function loadCredentials(): admin.credential.Credential {
  const keyPath = path.resolve(__dirname, '..', 'serviceAccountKey.json');
  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const sa = require(keyPath);
    return admin.credential.cert(sa);
  } catch (err) {
    console.error(
      `[seed] Cannot find ${keyPath}.\n` +
        `      Firebase Console → 프로젝트 설정 → 서비스 계정 → 비공개 키 생성 후 ` +
        `해당 위치에 저장하세요.`
    );
    process.exit(1);
  }
}

admin.initializeApp({
  credential: loadCredentials(),
  projectId: PROJECT_ID,
});

const db = admin.firestore();

interface Waypoint {
  lat: number;
  lng: number;
  order: number;
}

interface CourseSeed {
  courseId: string;
  name: string;
  description: string;
  regionName: string;
  startCoord: { lat: number; lng: number };
  endCoord: { lat: number; lng: number };
  waypoints: Waypoint[];
  distanceMeters: number;
  difficulty: number;
  isActive: boolean;
}

async function seedMeta(): Promise<void> {
  console.log('[seed] meta/avatars …');
  await db.collection('meta').doc('avatars').set({
    ids: [
      'avatar_01',
      'avatar_02',
      'avatar_03',
      'avatar_04',
      'avatar_05',
      'avatar_06',
      'avatar_07',
      'avatar_08',
    ],
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  console.log('[seed] meta/config …');
  await db.collection('meta').doc('config').set({
    minRaceTimeMs: 30000,
    maxAverageKmh: 200,
    armRadiusM: 30,
    startTriggerRadiusM: 15,
    endTriggerRadiusM: 15,
    sampleIntervalMs: 1000,
    maxOutOfCorridorM: 200,
    maxOutOfCorridorMs: 15000,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

async function seedCourses(): Promise<void> {
  const courses = coursesSeed as CourseSeed[];
  console.log(`[seed] courses (${courses.length}) …`);

  const batch = db.batch();
  for (const c of courses) {
    const ref = db.collection('courses').doc(c.courseId);
    batch.set(ref, {
      name: c.name,
      description: c.description,
      regionName: c.regionName,
      startCoord: new admin.firestore.GeoPoint(c.startCoord.lat, c.startCoord.lng),
      endCoord: new admin.firestore.GeoPoint(c.endCoord.lat, c.endCoord.lng),
      waypoints: c.waypoints,
      distanceMeters: c.distanceMeters,
      difficulty: c.difficulty,
      isActive: c.isActive,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    console.log(`        - ${c.courseId} (${c.name})`);
  }
  await batch.commit();
}

(async () => {
  try {
    if (only === 'all' || only === 'meta') {
      await seedMeta();
    }
    if (only === 'all' || only === 'courses') {
      await seedCourses();
    }
    console.log('[seed] done.');
    process.exit(0);
  } catch (err) {
    console.error('[seed] failed:', err);
    process.exit(1);
  }
})();
