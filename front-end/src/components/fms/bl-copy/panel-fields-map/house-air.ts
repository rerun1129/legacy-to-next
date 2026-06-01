/**
 * House AIR 패널별 폼 필드 경로.
 * 출처: house-bl/tabs/main-air.tsx (HOUSE_BL_AIR_EXP_REGISTRY / HOUSE_BL_AIR_IMP_REGISTRY)
 *   → 각 section 컴포넌트 직접 확인.
 */
import type { PanelFieldsMap } from "./types";

// ── House AIR 13패널(EXP) / 12패널(IMP, issue-air 제외) ─────
export const HOUSE_AIR_PANEL_FIELDS: PanelFieldsMap = {
  // 출처: tabs/sections/party-panel.tsx (SEA와 공통)
  "party-air": [
    "shipperCode",
    "shipperName",
    "shipperAddress",
    "consigneeCode",
    "consigneeName",
    "consigneeAddress",
    "notifyCode",
    "notifyName",
    "notifyAddress",
    "docPartnerCode",
    "docPartnerName",
    "docPartnerAddress",
  ],

  // 출처: tabs/sections/air-schedule-panel.tsx — useFieldArray name="scheduleLegs"
  "schedule-air": [
    "airDetail.airlineCode",
    "airDetail.airlineName",
    "pol",
    "polName",
    "pod",
    "podName",
    "etd",
    "eta",
    "scheduleLegs",
  ],

  // 출처: tabs/sections/air-document-panel.tsx
  "document-air": [
    "salesManCode",
    "salesManName",
    "operatorCode",
    "operatorName",
    "teamCode",
    "teamName",
    "salesClass",
  ],

  // 출처: tabs/sections/air-trade-panel.tsx
  "trade-air": [
    "airDetail.currencyCode",
    "incoterms",
    "freightTerm",
    "airDetail.otherTerm",
    "airDetail.declaredValueCarriage",
    "airDetail.insurance",
    "airDetail.declaredValueCustoms",
    "airDetail.accountInformation",
    "airDetail.fhd",
  ],

  // 출처: tabs/sections/air-performance-panel.tsx
  "perf-air": [
    "actualCustomerCode",
    "actualCustomerName",
    "settlePartnerCode",
    "settlePartnerName",
  ],

  // 출처: tabs/sections/air-issue-panel.tsx — EXP 전용
  "issue-air": [
    "airDetail.issueDate",
    "airDetail.issuePlace",
    "airDetail.signature",
  ],

  // 출처: tabs/sections/air-cargo-panel.tsx
  "cargo-air": [
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
  ],

  // 출처: tabs/sections/air-handling-info-panel.tsx
  "handling-air": [
    "airDetail.handlingInformationCode",
    "airDetail.handlingInformationDesc",
  ],

  // 출처: tabs/sections/dimension-panel.tsx — useFieldArray name="dims"
  "dimension": [
    "dims",
  ],

  // 출처: tabs/sections/air-charge-info-panel.tsx — useFieldArray name="airCharges"
  "air-charges": [
    "airCharges",
  ],

  // 출처: tabs/sections/marks-panel.tsx
  "marks-air": [
    "desc.marks",
  ],

  // 출처: tabs/sections/nature-goods-panel.tsx
  "nature-goods": [
    "desc.description",
  ],

  // 출처: tabs/sections/air-remark-panel.tsx
  "remark-air": [
    "remark",
  ],
};
