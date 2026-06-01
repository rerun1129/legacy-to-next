/**
 * Master AIR 패널별 폼 필드 경로 (house-bl-grid 제외).
 *
 * 출처: master-bl/tabs/main-air.tsx (MASTER_BL_AIR_EXP_BASE / MASTER_BL_AIR_IMP_BASE)
 *   → 각 section 컴포넌트 직접 확인.
 * house-bl-grid 패널은 자식 BL 참조 그리드이므로 Copy 대상 제외.
 *
 * EXP: 12패널(issue-air 포함), IMP: 11패널(issue-air 제외).
 * EXP/IMP 공통 패널은 동일 상수 공유.
 */
import type { PanelFieldsMap } from "./types";

// ── Master AIR 공통 패널 ────────────────────────────────────

// 출처: tabs/sections/master-air-party-panel.tsx — MasterAirPartyPanel
//   register("shipperCode"), Controller name="shipperAddress"
//   register("consigneeCode"), Controller name="consigneeAddress"
//   register("notifyCode"), Controller name="notifyAddress"
//   (Doc Partner 없음 — House 4슬롯 대비 3슬롯)
const PARTY_AIR: string[] = [
  "shipperCode",
  "shipperAddress",
  "consigneeCode",
  "consigneeAddress",
  "notifyCode",
  "notifyAddress",
];

// 출처: tabs/sections/master-air-schedule-panel.tsx — MasterAirSchedulePanel
//   register("airDetail.airlineCode"), register("polCode"), register("podCode")
//   Controller name="etd", name="eta"
//   useFieldArray name="scheduleLegs"
const SCHEDULE_AIR: string[] = [
  "airDetail.airlineCode",
  "polCode",
  "podCode",
  "etd",
  "eta",
  "scheduleLegs",
];

// 출처: tabs/sections/master-air-document-panel.tsx — MasterAirDocumentPanel
//   register("operatorCode"), register("teamCode"), register("teamName"),
//   register("settlePartnerCode")
const DOCUMENT_AIR: string[] = [
  "operatorCode",
  "teamCode",
  "teamName",
  "settlePartnerCode",
];

// 출처: tabs/sections/master-air-trade-panel.tsx — MasterAirTradePanel
//   register("airDetail.currencyCode"), Controller name="freightTerm",
//   Controller name="airDetail.otherTerm",
//   register("airDetail.declaredValueCarriage"), register("airDetail.insurance"),
//   register("airDetail.declaredValueCustoms"), register("airDetail.accountInformation")
const TRADE_AIR: string[] = [
  "airDetail.currencyCode",
  "freightTerm",
  "airDetail.otherTerm",
  "airDetail.declaredValueCarriage",
  "airDetail.insurance",
  "airDetail.declaredValueCustoms",
  "airDetail.accountInformation",
];

// 출처: tabs/sections/master-air-handling-info-panel.tsx — MasterAirHandlingInfoPanel
//   Controller name="airDetail.handlingInformationCode"
//   Controller name="airDetail.handlingInformationText"
//   (House는 handlingInformationDesc, Master는 handlingInformationText)
const HANDLING_AIR: string[] = [
  "airDetail.handlingInformationCode",
  "airDetail.handlingInformationText",
];

// 출처: tabs/sections/master-air-cargo-panel.tsx — MasterAirCargoPanel
//   register("pkgQty"), register("pkgUnit"),
//   register("grossWeightKg"), Controller name="weightUnit"
//   register("airDetail.volumeWeightKg"), register("airDetail.chargeWeightKg")
//   Controller name="airDetail.rateClass", register("cbm")
//   register("hsCode"), register("hsCodeName")
const CARGO_AIR: string[] = [
  "pkgQty",
  "pkgUnit",
  "grossWeightKg",
  "weightUnit",
  "airDetail.volumeWeightKg",
  "airDetail.chargeWeightKg",
  "airDetail.rateClass",
  "cbm",
  "hsCode",
  "hsCodeName",
];

// 출처: tabs/sections/master-air-dimension-panel.tsx — useFieldArray name="dims"
const DIMENSION: string[] = [
  "dims",
];

// 출처: tabs/sections/master-air-charge-info-panel.tsx — useFieldArray name="airCharges"
const AIR_CHARGES: string[] = [
  "airCharges",
];

// 출처: tabs/sections/master-air-marks-panel.tsx
//   Controller name="desc.marks"
const MARKS_AIR: string[] = [
  "desc.marks",
];

// 출처: tabs/sections/master-air-nature-goods-panel.tsx
//   Controller name="desc.description"
const NATURE_GOODS: string[] = [
  "desc.description",
];

// 출처: tabs/sections/master-air-remark-panel.tsx
//   Controller name="remark"
const REMARK_AIR: string[] = [
  "remark",
];

// ── EXP: 12패널 (issue-air 포함) ────────────────────────────
export const MASTER_AIR_EXP_PANEL_FIELDS: PanelFieldsMap = {
  "party-air":    PARTY_AIR,
  "schedule-air": SCHEDULE_AIR,
  "document-air": DOCUMENT_AIR,
  "trade-air":    TRADE_AIR,
  // 출처: tabs/sections/master-air-issue-panel.tsx — MasterAirIssuePanel (EXP only)
  //   AIR_ISSUE_LABEL_TO_FIELD: airDetail.issueDate / airDetail.issuePlace / airDetail.signature
  "issue-air": [
    "airDetail.issueDate",
    "airDetail.issuePlace",
    "airDetail.signature",
  ],
  "handling-air": HANDLING_AIR,
  "cargo-air":    CARGO_AIR,
  "dimension":    DIMENSION,
  "air-charges":  AIR_CHARGES,
  "marks-air":    MARKS_AIR,
  "nature-goods": NATURE_GOODS,
  "remark-air":   REMARK_AIR,
};

// ── IMP: 11패널 (issue-air 없음) ─────────────────────────────
export const MASTER_AIR_IMP_PANEL_FIELDS: PanelFieldsMap = {
  "party-air":    PARTY_AIR,
  "schedule-air": SCHEDULE_AIR,
  "document-air": DOCUMENT_AIR,
  "trade-air":    TRADE_AIR,
  "handling-air": HANDLING_AIR,
  "cargo-air":    CARGO_AIR,
  "dimension":    DIMENSION,
  "air-charges":  AIR_CHARGES,
  "marks-air":    MARKS_AIR,
  "nature-goods": NATURE_GOODS,
  "remark-air":   REMARK_AIR,
};
