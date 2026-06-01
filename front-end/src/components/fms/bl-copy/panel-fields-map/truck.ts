/**
 * Truck BL 패널별 폼 필드 경로.
 * Truck은 variant 없음 — variantKey는 무시하고 단일 상수 사용.
 *
 * 출처: truck-bl/tabs/main-truck.tsx (TRUCK_REGISTRY) → 각 section 컴포넌트 직접 확인.
 */
import type { PanelFieldsMap } from "./types";

// ── Truck 10패널 ───────────────────────────────────────────
export const TRUCK_PANEL_FIELDS: PanelFieldsMap = {
  // 출처: tabs/sections/truck-party-panel.tsx — TruckPartyBlock, TRUCK_PARTIES
  //   CodeBox codeProps: register("shipperCode"), register("consigneeCode"),
  //           register("notifyCode"), register("docPartnerCode")
  //   CodeBox nameProps: register("shipperName"), register("consigneeName"),
  //           register("notifyName"), register("docPartnerName")
  //   Controller name="shipperAddr", name="consigneeAddr",
  //             name="notifyAddr", name="docPartnerAddress"
  "party-truck": [
    "shipperCode",
    "shipperName",
    "shipperAddr",
    "consigneeCode",
    "consigneeName",
    "consigneeAddr",
    "notifyCode",
    "notifyName",
    "notifyAddr",
    "docPartnerCode",
    "docPartnerName",
    "docPartnerAddress",
  ],

  // 출처: tabs/sections/truck-schedule-panel.tsx — TruckSchedulePanel
  //   TruckScheduleVessel:  register("vesselName")
  //   TruckScheduleDates:   Controller name="etd", name="eta"
  //   TruckSchedulePol:     register("polCode"), register("polName")
  //   TruckSchedulePod:     register("podCode"), register("podName")
  //   (voyNo 필드: truck-bl-entry-header에서 register하므로 스케줄 패널에는 미포함)
  "schedule-truck": [
    "vesselName",
    "etd",
    "eta",
    "polCode",
    "polName",
    "podCode",
    "podName",
  ],

  // 출처: tabs/sections/truck-cargo-panel.tsx — TruckCargoFields
  //   NumberBox name="pkgQty", register("pkgUnit"), NumberBox name="grossWeightKg"
  //   Controller name="weightUnit", NumberBox name="cbm", NumberBox name="chargeWeightKg"
  //   CodeBox codeProps: register("hsCode"), nameProps: register("hsCodeName")
  "cargo-truck": [
    "pkgQty",
    "pkgUnit",
    "grossWeightKg",
    "weightUnit",
    "cbm",
    "chargeWeightKg",
    "hsCode",
    "hsCodeName",
  ],

  // 출처: tabs/sections/truck-document-panel.tsx — TruckDocumentPanel
  //   Controller name="pickupDate"
  //   CodeBox codeProps: register("truckerCode"), nameProps: register("truckerName")
  //   register("truckerPic")
  "document-truck": [
    "pickupDate",
    "truckerCode",
    "truckerName",
    "truckerPic",
  ],

  // 출처: tabs/sections/truck-performance-panel.tsx — TruckPerformancePanel
  //   CodeBox: register("actualCustomerCode"), register("actualCustomerName")
  //            register("settlePartnerCode"),  register("settlePartnerName")
  //            register("salesManCode"),        register("salesManName")
  //            register("operatorCode"),        register("operatorName")
  //            register("teamCode"),            register("teamName")
  "perf-truck": [
    "actualCustomerCode",
    "actualCustomerName",
    "settlePartnerCode",
    "settlePartnerName",
    "salesManCode",
    "salesManName",
    "operatorCode",
    "operatorName",
    "teamCode",
    "teamName",
  ],

  // 출처: tabs/sections/truck-dimension-panel.tsx — TruckDimensionPanel
  //   useFieldArray name="dimensions"
  //   Controller name="dimensionDivisor"
  //   NumberBox: dimensions.i.length/width/height/qty/cbm/volWt
  "dimension-truck": [
    "dimensionDivisor",
    "dimensions",
  ],

  // 출처: tabs/sections/truck-remark-panel.tsx — TruckRemarkPanel
  //   Controller name="remark"
  "remark-truck": [
    "remark",
  ],

  // 출처: tabs/sections/truck-order-grid-panel.tsx — TruckOrderGridPanel
  //   useFieldArray name="truckOrders"
  //   register: truckOrders.i.truckOrderNo / truckNo / driver / mobileNo
  //             truckOrders.i.containerNo / sealNo1 / sealNo2 / sealNo3
  //   NumberBox: truckOrders.i.pkgQty / grossWeightKg / cbm
  //   PkgUnitCell: register(truckOrders.i.pkgUnit)
  //   Controller: truckOrders.i.truckType / containerType
  "truck-order-grid": [
    "truckOrders",
  ],

  // 출처: tabs/sections/truck-marks-panel.tsx — TruckMarksPanel
  //   Controller name="marks"
  "marks-truck": [
    "marks",
  ],

  // 출처: tabs/sections/truck-description-panel.tsx — TruckDescriptionPanel
  //   Controller name="descClause1", name="descClause2", name="description"
  "description-truck": [
    "descClause1",
    "descClause2",
    "description",
  ],
};
