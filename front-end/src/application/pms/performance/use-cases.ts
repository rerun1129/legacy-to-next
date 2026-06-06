import type { SearchPmsPerformanceInput } from "./ports";

/** react-query 캐시 키 팩토리 — PMS 실적 조회 */
export const pmsPerformanceKeys = {
  all: ["pms-performance"] as const,
  search: (filter: SearchPmsPerformanceInput) =>
    [...pmsPerformanceKeys.all, "search", filter] as const,
};
