import type { AggregationBasis } from "@/application/pms/performance/ports";

// BMS financial-document 동일 패턴: 모듈 초기화 시점의 당월 첫날/마지막날 (YYYYMMDD)
function getDefaultMonthRange() {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const pad = (n: number) => String(n).padStart(2, "0");
  const lastDate = new Date(y, m + 1, 0).getDate();
  return {
    from: `${y}${pad(m + 1)}01`,
    to: `${y}${pad(m + 1)}${pad(lastDate)}`,
  };
}

const { from, to } = getDefaultMonthRange();

/** PS-01 조회 폼 값 인터페이스 — 정형 7개 조건만 노출 */
export interface PmsPerformanceFilter {
  // 집계 기준 (토글)
  basis: AggregationBasis;

  // B/L 공통 (FMS 정형 5개)
  jobDiv: string;
  bound: string;
  /** 단일 일자 콤보: ETD | ETA | PERF | DOC. 제출 시 백엔드 파라미터로 분기 매핑. */
  dateKind: string;
  dateFrom: string;
  dateTo: string;

  // BMS 서류 (BMS 정형 2개)
  documentTypes: string[];
  documentStatus: string;
}

// 옵션 배열은 PMS 백엔드 /api/pms/enums/{name} 에서 동적 조회 (use-pms-enum.ts 참조)

export const DEFAULT_PMS_FILTER: PmsPerformanceFilter = {
  basis: "FREIGHT_INPUT",
  jobDiv: "",
  bound: "",
  dateKind: "ETD",
  dateFrom: from,
  dateTo: to,
  documentTypes: [],
  documentStatus: "",
};

