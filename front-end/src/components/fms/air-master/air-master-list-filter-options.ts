// list-filter 전용 옵션 상수 — Entry 스코프 누출 금지 (ARCH2 단순 상수 테이블 예외)
// labelKey는 fms.airMaster.list.filter 네임스페이스 키. 렌더 시 컴포넌트에서 번역.

export interface FilterOption {
  value: string;
  labelKey: string;
}

export const DATE_KIND_OPTIONS: FilterOption[] = [
  { value: "ETD", labelKey: "dateKindEtd" },
  { value: "ETA", labelKey: "dateKindEta" },
];

export const MASTER_AWB_KIND_OPTIONS: FilterOption[] = [
  { value: "MBL", labelKey: "masterAwbKindMawb" },
  { value: "REF", labelKey: "masterAwbKindRef" },
];

export const PARTY_KIND_OPTIONS: FilterOption[] = [
  { value: "SHIPPER",   labelKey: "partyKindShipper" },
  { value: "CONSIGNEE", labelKey: "partyKindConsignee" },
  { value: "NOTIFY",    labelKey: "partyKindNotify" },
];

export const PORT_KIND_OPTIONS: FilterOption[] = [
  { value: "POL", labelKey: "portKindDeparture" },
  { value: "POD", labelKey: "portKindDestination" },
];
