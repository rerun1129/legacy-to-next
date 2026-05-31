"use client";

import { useMemo }              from "react";
import { useTranslations }      from "next-intl";
import type { BLVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }       from "@/components/widget/widget-registry";
import { WidgetGrid }           from "@/components/widget/widget-grid";
import { PartyPanel }           from "./sections/party-panel";
import { AirSchedulePanel }     from "./sections/air-schedule-panel";
import { AirTradePanel }        from "./sections/air-trade-panel";
import { AirCargoPanel }        from "./sections/air-cargo-panel";
import { AirIssuePanel }        from "./sections/air-issue-panel";
import { DimensionPanel }       from "./sections/dimension-panel";
import { MarksPanel }           from "./sections/marks-panel";
import { NatureGoodsPanel }     from "./sections/nature-goods-panel";
import { AirDocumentPanel }     from "./sections/air-document-panel";
import { AirPerformancePanel }  from "./sections/air-performance-panel";
import { AirHandlingInfoPanel } from "./sections/air-handling-info-panel";
import { AirRemarkPanel }      from "./sections/air-remark-panel";
import { AirChargeInfoPanel }  from "./sections/air-charge-info-panel";

interface Props { variant: BLVariantConfig; active?: boolean }

// labelKey はパネルタイトル i18n キー (fms.houseBl.entry.panels.*) と対応
const HOUSE_BL_AIR_EXP_BASE: WidgetDef[] = [
  { key: "party-air",    label: "Party",               labelKey: "party",               component: PartyPanel,           defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air", label: "Schedule",             labelKey: "schedule",            component: AirSchedulePanel,     defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air", label: "Document",             labelKey: "document",            component: AirDocumentPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",    label: "Trade",                labelKey: "trade",               component: AirTradePanel,        defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "perf-air",     label: "Performance",          labelKey: "performance",         component: AirPerformancePanel,  defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "issue-air",    label: "Issue Information",    labelKey: "issueInformation",    component: AirIssuePanel,        defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-air",    label: "Cargo",                labelKey: "cargo",               component: AirCargoPanel,        defaultPosition: { col: 3, row: 7, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "handling-air", label: "Handling Information", labelKey: "handlingInformation", component: AirHandlingInfoPanel, defaultPosition: { col: 0, row: 7, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "dimension",    label: "Dimension",            labelKey: "dimension",           component: DimensionPanel,       defaultPosition: { col: 0, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges",  label: "Charge Information",   labelKey: "chargeInformation",   component: AirChargeInfoPanel,   defaultPosition: { col: 3, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",    label: "Marks & Numbers",      labelKey: "marksNumbers",        component: MarksPanel,           defaultPosition: { col: 0, row: 11, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods", label: "Nature & Qty",         labelKey: "natureQty",           component: NatureGoodsPanel,     defaultPosition: { col: 2, row: 11, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",   label: "Remark",               labelKey: "remark",              component: AirRemarkPanel,       defaultPosition: { col: 4, row: 11, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

const HOUSE_BL_AIR_IMP_BASE: WidgetDef[] = [
  { key: "party-air",    label: "Party",               labelKey: "party",               component: PartyPanel,           defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air", label: "Schedule",             labelKey: "schedule",            component: AirSchedulePanel,     defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air", label: "Document",             labelKey: "document",            component: AirDocumentPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",    label: "Trade",                labelKey: "trade",               component: AirTradePanel,        defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "perf-air",     label: "Performance",          labelKey: "performance",         component: AirPerformancePanel,  defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-air",    label: "Cargo",                labelKey: "cargo",               component: AirCargoPanel,        defaultPosition: { col: 3, row: 4, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "handling-air", label: "Handling Information", labelKey: "handlingInformation", component: AirHandlingInfoPanel, defaultPosition: { col: 0, row: 4, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "dimension",    label: "Dimension",            labelKey: "dimension",           component: DimensionPanel,       defaultPosition: { col: 0, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges",  label: "Charge Information",   labelKey: "chargeInformation",   component: AirChargeInfoPanel,   defaultPosition: { col: 3, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",    label: "Marks & Numbers",      labelKey: "marksNumbers",        component: MarksPanel,           defaultPosition: { col: 0, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods", label: "Nature & Qty",         labelKey: "natureQty",           component: NatureGoodsPanel,     defaultPosition: { col: 2, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",   label: "Remark",               labelKey: "remark",              component: AirRemarkPanel,       defaultPosition: { col: 4, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

// 後方互換のため旧名でも再エクスポート
export const HOUSE_BL_AIR_EXP_REGISTRY = HOUSE_BL_AIR_EXP_BASE;
export const HOUSE_BL_AIR_IMP_REGISTRY = HOUSE_BL_AIR_IMP_BASE;

export function MainTabAir({ variant, active }: Props) {
  const tp = useTranslations("fms.houseBl.entry.panels");
  const base = variant.direction === "EXP" ? HOUSE_BL_AIR_EXP_BASE : HOUSE_BL_AIR_IMP_BASE;

  // labelKey があるエントリーのみ label を上書き。
  // WidgetGrid の layout 保持は scope+key ベースなので registry 参照変更は無害。
  const registry = useMemo(
    () => base.map(w => w.labelKey ? { ...w, label: tp(w.labelKey) } : w),
    [tp, base]
  );

  const scope = `house-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
