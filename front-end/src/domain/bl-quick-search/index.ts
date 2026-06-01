/** B/L Quick Search 도메인 타입 */

export interface BlQuickSearchFilters {
  jobDiv?: string;
  bound?: string;
  /** react-hook-form 기본값 'ETD' */
  dateKind: "ETD" | "ETA";
  dateFrom?: string;
  dateTo?: string;
  teamCode?: string;
  teamName?: string;
  operatorCode?: string;
  operatorName?: string;
  salesManCode?: string;
  salesManName?: string;
  polCode?: string;
  polName?: string;
  podCode?: string;
  podName?: string;
  partyKind?: string;
  partyCode?: string;
  partyName?: string;
}

/** /api/bl/quick-search/autocomplete 응답 item */
export interface BlQuickSearchItem {
  id: number;
  blType: "HOUSE" | "MASTER";
  blNo: string;
  jobDiv: string;
  bound: string;
  shipperCode: string | null;
  polCode: string | null;
  podCode: string | null;
  etd: string | null;
  label: string;
}
