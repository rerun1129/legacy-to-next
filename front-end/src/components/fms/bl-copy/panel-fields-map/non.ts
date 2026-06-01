/**
 * Non-BL 패널별 폼 필드 경로.
 * Non-BL은 variant 없음 — variantKey는 무시하고 단일 상수 사용.
 *
 * 출처: non-bl/tabs/main-non-bl.tsx (NON_BL_REGISTRY) → 각 section 컴포넌트 직접 확인.
 */
import type { PanelFieldsMap } from "./types";

// ── Non-BL 7패널 ───────────────────────────────────────────
export const NON_PANEL_FIELDS: PanelFieldsMap = {
  // 출처: tabs/sections/nonbl-party-panel.tsx — NonBLPartyPanel
  //   CodeBox codeProps: register("actualCustomerCode"), register("shipperCode"),
  //           register("consigneeCode"), register("notifyCode"), register("settlePartnerCode")
  //   CodeBox nameProps: register("actualCustomerName"), register("shipperName"),
  //           register("consigneeName"), register("notifyName"), register("settlePartnerName")
  "party-nonbl": [
    "actualCustomerCode",
    "actualCustomerName",
    "shipperCode",
    "shipperName",
    "consigneeCode",
    "consigneeName",
    "notifyCode",
    "notifyName",
    "settlePartnerCode",
    "settlePartnerName",
  ],

  // 출처: tabs/sections/nonbl-document-panel.tsx — NonBLDocumentPanel
  //   Controller name="salesClass"
  //   CodeBox codeProps: register("salesManCode"), register("operatorCode"), register("teamCode")
  //   CodeBox nameProps: register("salesManName"), register("operatorName"), register("teamName")
  "document-nonbl": [
    "salesClass",
    "salesManCode",
    "salesManName",
    "operatorCode",
    "operatorName",
    "teamCode",
    "teamName",
  ],

  // 출처: tabs/sections/nonbl-schedule-panel.tsx — NonBLSchedulePanel
  //   CodeBox codeProps: register("linerCode"), register("polCode"),
  //           register("podCode"), register("finalDestCode")
  //   CodeBox nameProps: register("linerName"), register("polName"),
  //           register("podName"), register("finalDestName")
  //   register("vesselName"), register("voyNo")
  //   Controller name="etd", name="eta", name="finalEta"
  "schedule-nonbl": [
    "linerCode",
    "linerName",
    "vesselName",
    "voyNo",
    "etd",
    "eta",
    "polCode",
    "polName",
    "podCode",
    "podName",
    "finalDestCode",
    "finalDestName",
    "finalEta",
  ],

  // 출처: tabs/sections/nonbl-cargo-panel.tsx — NonBLCargoPanel
  //   register("mainItem")
  //   NumberBox name="cargoQty", CodeBox codeProps: register("pkgUnit")
  //   NumberBox name="grossWt", Controller name="weightUnit"
  //   NumberBox name="volWt", NumberBox name="totalCbm", NumberBox name="rton"
  //   CodeBox codeProps: register("hsCode"), nameProps: register("hsCodeName")
  "cargo-nonbl": [
    "mainItem",
    "cargoQty",
    "pkgUnit",
    "grossWt",
    "weightUnit",
    "volWt",
    "totalCbm",
    "rton",
    "hsCode",
    "hsCodeName",
  ],

  // 출처: tabs/sections/nonbl-dimension-panel.tsx — NonBLDimensionPanel
  //   useFieldArray name="dimensions"
  //   Controller name="dimensionDivisor"
  //   NumberBox: dimensions.i.length/width/height/qty/cbm/volWt
  "dimension-nonbl": [
    "dimensionDivisor",
    "dimensions",
  ],

  // 출처: tabs/sections/nonbl-container-info-panel.tsx — NonBLContainerInfoPanel
  //   useFieldArray name="containers"
  //   register: containers.i.cno / sealNo1 / sealNo2 / sealNo3
  //   Controller: containers.i.contType
  //   NumberBox: containers.i.pkg / grossWt / cbm
  //   PkgUnitCell: register(containers.i.pkgUnit)
  "container-info-nonbl": [
    "containers",
  ],

  // 출처: tabs/sections/nonbl-remark-panel.tsx — NonBLRemarkPanel
  //   Controller name="remark"
  "remark-nonbl": [
    "remark",
  ],
};
