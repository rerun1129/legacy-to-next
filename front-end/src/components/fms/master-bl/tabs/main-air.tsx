"use client";

import { useMemo }                  from "react";
import { useTranslations }          from "next-intl";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }           from "@/components/widget/widget-registry";
import { WidgetGrid }               from "@/components/widget/widget-grid";
import { MasterHouseBLGrid }        from "./sections/master-house-bl-grid";
import { MasterAirPartyPanel }      from "./sections/master-air-party-panel";
import { MasterAirSchedulePanel }   from "./sections/master-air-schedule-panel";
import { MasterAirDocumentPanel }   from "./sections/master-air-document-panel";
import { MasterAirTradePanel }      from "./sections/master-air-trade-panel";
import { MasterAirIssuePanel }      from "./sections/master-air-issue-panel";
import { MasterAirCargoPanel }      from "./sections/master-air-cargo-panel";
import { MasterAirHandlingInfoPanel } from "./sections/master-air-handling-info-panel";
import { MasterAirDimensionPanel }  from "./sections/master-air-dimension-panel";
import { MasterAirChargeInfoPanel } from "./sections/master-air-charge-info-panel";
import { MasterAirMarksPanel }      from "./sections/master-air-marks-panel";
import { MasterAirNatureGoodsPanel } from "./sections/master-air-nature-goods-panel";
import { MasterAirRemarkPanel }     from "./sections/master-air-remark-panel";

interface Props { variant: MasterVariantConfig; active?: boolean }

// labelKey は i18n の panels.* キーと対応; label はフォールバック用英語テキスト
// House EXP registry + row+2 오프셋 + row 0~1 master-house-bl-grid
// Performance 위젯 자리(col 4, row 4, rowSpan 1)는 빈 공간으로 유지
const MASTER_BL_AIR_EXP_BASE: WidgetDef[] = [
  { key: "house-bl-grid",  label: "House AWB List",        labelKey: "houseAWBList",        component: MasterHouseBLGrid,          defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-air",      label: "Party",                 labelKey: "party",               component: MasterAirPartyPanel,        defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air",   label: "Schedule",              labelKey: "schedule",            component: MasterAirSchedulePanel,     defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air",   label: "Document",              labelKey: "document",            component: MasterAirDocumentPanel,     defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",      label: "Trade",                 labelKey: "trade",               component: MasterAirTradePanel,        defaultPosition: { col: 2, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  // Performance 자리 (col 4, row 4, colSpan 2, rowSpan 1) — 빈 공간으로 남김
  { key: "issue-air",      label: "Issue Information",     labelKey: "issueInformation",    component: MasterAirIssuePanel,        defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "handling-air",   label: "Handling Information",  labelKey: "handlingInformation", component: MasterAirHandlingInfoPanel, defaultPosition: { col: 0, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-air",      label: "Cargo",                 labelKey: "cargo",               component: MasterAirCargoPanel,        defaultPosition: { col: 3, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "dimension",      label: "Dimension",             labelKey: "dimension",           component: MasterAirDimensionPanel,    defaultPosition: { col: 0, row: 11, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges",    label: "Charge Information",    labelKey: "chargeInformation",   component: MasterAirChargeInfoPanel,   defaultPosition: { col: 3, row: 11, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",      label: "Marks & Numbers",       labelKey: "marksNumbers",        component: MasterAirMarksPanel,        defaultPosition: { col: 0, row: 13, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods",   label: "Nature & Qty",          labelKey: "natureQty",           component: MasterAirNatureGoodsPanel,  defaultPosition: { col: 2, row: 13, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",     label: "Remark",                labelKey: "remark",              component: MasterAirRemarkPanel,       defaultPosition: { col: 4, row: 13, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

// House IMP registry + row+2 오프셋 + row 0~1 master-house-bl-grid (Issue 패널 없음)
// Performance 자리 (col 4, row 4, colSpan 2, rowSpan 2) — 빈 공간으로 남김
const MASTER_BL_AIR_IMP_BASE: WidgetDef[] = [
  { key: "house-bl-grid",  label: "House AWB List",        labelKey: "houseAWBList",        component: MasterHouseBLGrid,          defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-air",      label: "Party",                 labelKey: "party",               component: MasterAirPartyPanel,        defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air",   label: "Schedule",              labelKey: "schedule",            component: MasterAirSchedulePanel,     defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air",   label: "Document",              labelKey: "document",            component: MasterAirDocumentPanel,     defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",      label: "Trade",                 labelKey: "trade",               component: MasterAirTradePanel,        defaultPosition: { col: 2, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  // Performance 자리 (col 4, row 4, colSpan 2, rowSpan 2) — 빈 공간으로 남김
  { key: "handling-air",   label: "Handling Information",  labelKey: "handlingInformation", component: MasterAirHandlingInfoPanel, defaultPosition: { col: 0, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-air",      label: "Cargo",                 labelKey: "cargo",               component: MasterAirCargoPanel,        defaultPosition: { col: 3, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "dimension",      label: "Dimension",             labelKey: "dimension",           component: MasterAirDimensionPanel,    defaultPosition: { col: 0, row: 8, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges",    label: "Charge Information",    labelKey: "chargeInformation",   component: MasterAirChargeInfoPanel,   defaultPosition: { col: 3, row: 8, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",      label: "Marks & Numbers",       labelKey: "marksNumbers",        component: MasterAirMarksPanel,        defaultPosition: { col: 0, row: 10, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods",   label: "Nature & Qty",          labelKey: "natureQty",           component: MasterAirNatureGoodsPanel,  defaultPosition: { col: 2, row: 10, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",     label: "Remark",                labelKey: "remark",              component: MasterAirRemarkPanel,       defaultPosition: { col: 4, row: 10, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

// 後方互換のため旧名でも再エクスポート
export const MASTER_BL_AIR_EXP_REGISTRY = MASTER_BL_AIR_EXP_BASE;
export const MASTER_BL_AIR_IMP_REGISTRY = MASTER_BL_AIR_IMP_BASE;

export function MasterMainTabAir({ variant, active }: Props) {
  const tp = useTranslations("fms.masterBl.entry.panels");

  const baseRegistry = variant.direction === "EXP" ? MASTER_BL_AIR_EXP_BASE : MASTER_BL_AIR_IMP_BASE;

  // labelKey があるエントリーのみ label を上書き。
  // useMemo のメモ化で locale 切替時も余計な再生成を抑える。
  const registry = useMemo(
    () => baseRegistry.map(w => w.labelKey ? { ...w, label: tp(w.labelKey) } : w),
    [baseRegistry, tp]
  );

  const scope = `master-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
