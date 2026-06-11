// 모든 API 호출은 Spring Cloud Gateway(8084)를 단일 진입점으로 경유한다.
// env 변수로 모듈별 직결 오버라이드 가능 (e.g. NEXT_PUBLIC_FMS_API_URL=http://localhost:8080).
export const FMS_API_URL =
  process.env.NEXT_PUBLIC_FMS_API_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8084";

export const ADMIN_API_URL =
  process.env.NEXT_PUBLIC_ADMIN_API_URL ??
  "http://localhost:8084";

export const BMS_API_URL =
  process.env.NEXT_PUBLIC_BMS_API_URL ??
  "http://localhost:8084";

export const PMS_API_URL =
  process.env.NEXT_PUBLIC_PMS_API_URL ??
  "http://localhost:8084";
