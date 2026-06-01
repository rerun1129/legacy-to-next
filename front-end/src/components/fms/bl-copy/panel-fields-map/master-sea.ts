/**
 * Master SEA 패널별 폼 필드 경로 (house-bl-grid 제외).
 *
 * 출처: master-bl/tabs/main-sea.tsx (buildSeaRegistry) → 각 section 컴포넌트 직접 확인.
 * house-bl-grid 패널은 자식 BL 참조 그리드이므로 Copy 대상 제외.
 */
import type { PanelFieldsMap } from "./types";

// ── Master SEA 7패널 (house-bl-grid 제외) ───────────────────
export const MASTER_SEA_PANEL_FIELDS: PanelFieldsMap = {
  // 출처: tabs/sections/master-panels.tsx — MasterPartyPanel
  //   register("shipperCode"), Controller name="shipperAddress"
  //   register("consigneeCode"), Controller name="consigneeAddress"
  //   register("notifyCode"), Controller name="notifyAddress"
  "party-master": [
    "shipperCode",
    "shipperAddress",
    "consigneeCode",
    "consigneeAddress",
    "notifyCode",
    "notifyAddress",
  ],

  // 출처: tabs/sections/master-schedule-panel.tsx (MasterSchedulePanel, mode=SEA)
  //   buildSeaFields → master-schedule-sea-atoms.tsx:
  //     LinerLcnField: register("seaDetail.linerCode")
  //     VesselField: register("seaDetail.vesselName")
  //     VoyageField: register("seaDetail.voyageNo")
  //     EtdField: Controller name="etd"
  //     EtaField: Controller name="eta"
  //     IssueDateField: Controller name="seaDetail.issueDate"
  //     PorField: register("seaDetail.porCode")
  //     PolField: register("polCode")
  //     PodField: register("podCode")
  //     FinalDestField: register("seaDetail.finalDestCode")
  "schedule-master": [
    "seaDetail.linerCode",
    "seaDetail.vesselName",
    "seaDetail.voyageNo",
    "etd",
    "eta",
    "seaDetail.issueDate",
    "seaDetail.porCode",
    "polCode",
    "podCode",
    "seaDetail.finalDestCode",
  ],

  // 출처: tabs/sections/master-cargo-doc-panel.tsx — MasterCargoDocPanel (mode=SEA)
  //   cargoBase: name="mainItemName", register("pkgQty"), register("grossWeightKg"),
  //     Controller name="weightUnit", NumberBox name="cbm"
  //   cargoExtras(SEA): NumberBox name="seaDetail.rton"
  //   HsCodeLcnField: register("hsCode"), register("hsCodeName")
  //   seaDoc: SettlePartnerLcnField(register("settlePartnerCode")),
  //           OperatorLcnField(register("operatorCode")),
  //           TeamLcnField(register("teamCode"), register("teamName"))
  "cargo-doc": [
    "mainItemName",
    "pkgQty",
    "pkgUnit",
    "grossWeightKg",
    "weightUnit",
    "cbm",
    "seaDetail.rton",
    "hsCode",
    "hsCodeName",
    "settlePartnerCode",
    "operatorCode",
    "teamCode",
    "teamName",
  ],

  // 출처: tabs/sections/master-container-grid.tsx — MasterContainerGrid
  //   useWatch(name="consoledSeaContainers") 표시 전용 그리드.
  //   consoledSeaContainers는 House BL에서 집계된 읽기 전용 데이터 → 복사 의미 없음.
  //   레지스트리에 포함되나 panelFieldsMap에 빈 배열로 등록해 체크박스 비활성 처리.
  "container-master": [],

  // 출처: tabs/sections/master-panels.tsx — MasterMarksPanel
  //   Controller name="desc.marks"
  "marks-master": [
    "desc.marks",
  ],

  // 출처: tabs/sections/master-panels.tsx — MasterGoodsDescPanel (mode=SEA)
  //   Controller name="desc.descClause1", name="desc.descClause2", name="desc.description"
  "goods-desc": [
    "desc.descClause1",
    "desc.descClause2",
    "desc.description",
  ],

  // 출처: tabs/sections/master-panels.tsx — MasterRemarkPanel
  //   Controller name="remark"
  "remark": [
    "remark",
  ],
};
