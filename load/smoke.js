// k6 스모크 — 게이트웨이 경유 핵심 조회 경로의 회귀 감지용 (NFR §1.2의 본격 프로필 아님)
// 실행: k6 run smoke.js  (env: BASE_URL, SMOKE_USER, SMOKE_PASS, VUS, HOLD)
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8084';
const USER = __ENV.SMOKE_USER || 'fms';
const PASS = __ENV.SMOKE_PASS || 'fms12345';
const VUS = Number(__ENV.VUS || 10);
const HOLD = __ENV.HOLD || '60s';

export const options = {
  scenarios: {
    smoke: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: VUS },
        { duration: HOLD, target: VUS },
        { duration: '5s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  // 느슨한 임계 — 러너는 부하기·대상 동거 환경이라 절대수치 대표성 없음. 파국적 회귀만 잡는다.
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
    checks: ['rate>0.99'],
  },
};

export function setup() {
  const res = http.post(
    `${BASE}/api/admin/auth/login`,
    JSON.stringify({ username: USER, password: PASS }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'login' } },
  );
  if (res.status !== 200) {
    throw new Error(`login failed: status=${res.status} body=${String(res.body).slice(0, 300)}`);
  }
  const token = res.json('data.accessToken');
  if (!token) {
    throw new Error('login response에 data.accessToken 없음');
  }
  return { token };
}

export default function (data) {
  const auth = { Authorization: `Bearer ${data.token}` };

  // 핵심 조회 1 — House B/L List 검색 (게이트웨이→FMS, 인증 헤더 번들 포함 경로)
  const search = http.post(
    `${BASE}/api/house-bl/search`,
    JSON.stringify({ jobDiv: 'SEA', page: 0, size: 20 }),
    { headers: { 'Content-Type': 'application/json', ...auth }, tags: { name: 'house-bl-search' } },
  );
  check(search, { 'house-bl-search 200': (r) => r.status === 200 });

  // 핵심 조회 2 — ENUM 메타 (공통코드 Redis→DB→enum 리더 체인 경유)
  const enums = http.get(`${BASE}/api/enums/housebl.JobDiv`, {
    headers: auth,
    tags: { name: 'fms-enums' },
  });
  check(enums, { 'fms-enums 200': (r) => r.status === 200 });

  sleep(1);
}
