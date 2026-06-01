/**
 * House SEA 패널별 폼 필드 경로.
 * 출처: house-bl/tabs/main-sea.tsx (HOUSE_BL_SEA_REGISTRY) → 각 section 컴포넌트 직접 확인.
 */
import type { PanelFieldsMap } from "./types";

// ── House SEA 9패널 ─────────────────────────────────────────
export const HOUSE_SEA_PANEL_FIELDS: PanelFieldsMap = {
  // 출처: tabs/sections/party-panel.tsx — ROLE_FIELDS, addrField
  "party-sea": [
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

  // 출처: tabs/sections/schedule-panel.tsx, schedule-panel-helpers.tsx
  "schedule-sea": [
    "pol",
    "seaDetail.polName",
    "pod",
    "seaDetail.podName",
    "seaDetail.deliveryCode",
    "seaDetail.deliveryName",
    "seaDetail.linerCode",
    "linerName",
    "seaDetail.vesselName",
    "seaDetail.voyageNo",
    "etd",
    "eta",
    "seaDetail.onboardDate",
    "seaDetail.issueDate",
    "seaDetail.noOfBl",
    "seaDetail.issuePlace",
    "seaDetail.issuePlaceName",
    "seaDetail.doDate",
    "seaDetail.signature",
  ],

  // 출처: tabs/sections/sea-trade-panel.tsx
  "trade-sea": [
    "incoterms",
    "freightTerm",
    "seaDetail.payableAt",
    "seaDetail.payableAtName",
    "actualCustomerCode",
    "actualCustomerName",
    "settlePartnerCode",
    "settlePartnerName",
  ],

  // 출처: tabs/sections/sea-document-panel.tsx
  "document-sea": [
    "salesClass",
    "salesManCode",
    "salesManName",
    "operatorCode",
    "operatorName",
    "teamCode",
    "teamName",
  ],

  // 출처: tabs/sections/container-grid-panel.tsx — useFieldArray name="containers"
  "container-sea": [
    "containers",
  ],

  // 출처: tabs/sections/sea-cargo-panel.tsx
  "cargo-sea": [
    "pkgQty",
    "pkgUnit",
    "grossWeightKg",
    "weightUnit",
    "cbm",
    "seaDetail.rton",
    "hsCode",
    "hsCodeName",
  ],

  // 출처: tabs/sections/marks-panel.tsx
  "marks-sea": [
    "desc.marks",
  ],

  // 출처: tabs/sections/description-panel.tsx
  "description-sea": [
    "desc.descClause1",
    "desc.descClause2",
    "desc.description",
  ],

  // 출처: tabs/sections/sea-remark-panel.tsx
  "remark-sea": [
    "remark",
  ],
};
