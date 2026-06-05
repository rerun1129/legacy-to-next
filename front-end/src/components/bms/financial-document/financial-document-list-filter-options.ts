/**
 * BMS 금융 서류 리스트 필터 정적 옵션 상수.
 * enum 기반 옵션은 useEnumOptions로, 정적 옵션만 여기 정의.
 * labelKey는 messages bms.list.filter.* namespace 참조.
 */

/** 일자 종류 선택 옵션 */
export const DATE_KIND_OPTIONS = [
  { value: "DOCUMENT_DT", labelKey: "dateKindDocumentDt" },
  { value: "PERFORMANCE_DT", labelKey: "dateKindPerformanceDt" },
  { value: "ETD", labelKey: "dateKindEtd" },
  { value: "ETA", labelKey: "dateKindEta" },
] as const;

export type DateKind = (typeof DATE_KIND_OPTIONS)[number]["value"];

/** 서류 Status 정적 옵션 — BE enum값 그대로 */
export const DOCUMENT_STATUS_OPTIONS = [
  { value: "CREATED", labelKey: "statusCreated" },
  { value: "GROUPED", labelKey: "statusGrouped" },
  { value: "TAX", labelKey: "statusTax" },
  { value: "SLIP", labelKey: "statusSlip" },
  { value: "CLEAR", labelKey: "statusClear" },
] as const;
