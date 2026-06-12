/**
 * BMS 금융 서류 리스트 필터 정적 옵션 상수.
 * BE enum 기반 옵션(DocumentStatus 등)은 useBmsEnumOptions로 동적 조회.
 * FE 전용 정적 옵션만 여기 정의.
 * labelKey는 messages bms.list.filter.* namespace 참조.
 */

/** 일자 종류 선택 옵션 — BE enum 없음(FE 전용 필터) */
export const DATE_KIND_OPTIONS = [
  { value: "DOCUMENT_DT", labelKey: "dateKindDocumentDt" },
  { value: "PERFORMANCE_DT", labelKey: "dateKindPerformanceDt" },
  { value: "ETD", labelKey: "dateKindEtd" },
  { value: "ETA", labelKey: "dateKindEta" },
] as const;

export type DateKind = (typeof DATE_KIND_OPTIONS)[number]["value"];
