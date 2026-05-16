// list-filter 전용 옵션 상수 — Entry 스코프 누출 금지 (ARCH2 단순 상수 테이블 예외)

export const DATE_KIND_OPTIONS = [
  { value: "ETD", label: "ETD" },
  { value: "ETA", label: "ETA" },
];

export const MASTER_BL_KIND_OPTIONS = [
  { value: "MBL", label: "Master B/L No" },
  { value: "REF", label: "Master Reference No." },
];

export const PARTY_KIND_OPTIONS = [
  { value: "SHIPPER", label: "Shipper" },
  { value: "CONSIGNEE", label: "Consignee" },
  { value: "NOTIFY", label: "Notify" },
];

export const PARTNER_KIND_OPTIONS = [
  { value: "SETTLE_PARTNER", label: "Settle Partner" },
  { value: "DOC_PARTNER", label: "Doc Partner" },
];

export const PORT_KIND_OPTIONS = [
  { value: "POL", label: "POL" },
  { value: "POD", label: "POD" },
];
