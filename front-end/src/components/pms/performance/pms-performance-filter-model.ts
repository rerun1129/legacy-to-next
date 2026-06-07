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

/** PS-01 조회 폼 값 인터페이스 */
export interface PmsPerformanceFilter {
  // 집계 기준 (토글)
  basis: AggregationBasis;

  // B/L 공통
  jobDiv: string;
  bound: string;
  /** 단일 일자 콤보: ETD | ETA | PERF | DOC. 제출 시 백엔드 파라미터로 분기 매핑. */
  dateKind: string;
  dateFrom: string;
  dateTo: string;
  hblNo: string;
  mblNo: string;

  // 거래처
  actualCustomerCode: string;
  actualCustomerName: string;
  settlePartnerCode: string;
  settlePartnerName: string;

  // 운송사
  carrierCode: string;
  carrierName: string;

  // 항만
  portKind: string;
  portCode: string;
  portName: string;

  // 영업
  salesManCode: string;
  salesManName: string;
  salesClass: string;
  incoterms: string;
  loadType: string;
  teamCode: string;
  teamName: string;
  operator: string;
  operatorName: string;

  // BMS 서류
  documentTypes: string[];
  documentStatus: string;
  documentNoLike: string;
  groupFinancialNo: string;
  grouped: string;
  issued: string;

  // BMS 운임행
  financialDocType: string;
  taxType: string;

  // 총건수 정확도 — true: 정확치, false: 근사치(기본)
  exactCount: boolean;
}

// 옵션 배열은 PMS 백엔드 /api/enums/{name} 에서 동적 조회 (use-pms-enum.ts 참조)

export const DEFAULT_PMS_FILTER: PmsPerformanceFilter = {
  basis: "FREIGHT_INPUT",
  jobDiv: "",
  bound: "",
  dateKind: "ETD",
  dateFrom: from,
  dateTo: to,
  hblNo: "",
  mblNo: "",
  actualCustomerCode: "",
  actualCustomerName: "",
  settlePartnerCode: "",
  settlePartnerName: "",
  carrierCode: "",
  carrierName: "",
  portKind: "POL",
  portCode: "",
  portName: "",
  salesManCode: "",
  salesManName: "",
  salesClass: "",
  incoterms: "",
  loadType: "",
  teamCode: "",
  teamName: "",
  operator: "",
  operatorName: "",
  documentTypes: [],
  documentStatus: "",
  documentNoLike: "",
  groupFinancialNo: "",
  grouped: "",
  issued: "",
  financialDocType: "",
  taxType: "",
  exactCount: false,
};

