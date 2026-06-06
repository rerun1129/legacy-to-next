/**
 * BMS 운임 행 발급 리스트 필터 정적 옵션 상수.
 * DB-driven useEnum 사용 금지 — 없는 enum fetch 방지(S8).
 * labelKey는 messages bms.issue.filter.* namespace 참조.
 */

/** 발급상태 옵션 — 미발급/발급/전체 */
export const ISSUED_STATUS_OPTIONS = [
  { value: "UNISSUED", labelKey: "issuedStatusUnissued" },
  { value: "ISSUED",   labelKey: "issuedStatusIssued" },
] as const;

export type IssuedStatus = (typeof ISSUED_STATUS_OPTIONS)[number]["value"];
