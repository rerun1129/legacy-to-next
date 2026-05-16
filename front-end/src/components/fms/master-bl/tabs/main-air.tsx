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

// House EXP registry + row+2 오프셋 + row 0~1 master-house-bl-grid
// Performance 위젯 자리(col 4, row 4, rowSpan 1)는 빈 공간으로 유지
export const MASTER_BL_AIR_EXP_REGISTRY: WidgetDef[] = [
  { key: "house-bl-grid",  label: "House AWB List",        component: MasterHouseBLGrid,        defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-air",      label: "Party",                 component: MasterAirPartyPanel,      defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air",   label: "Schedule",              component: MasterAirSchedulePanel,   defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air",   label: "Document",              component: MasterAirDocumentPanel,   defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",      label: "Trade",                 component: MasterAirTradePanel,      defaultPosition: { col: 2, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  // Performance 자리 (col 4, row 4, colSpan 2, rowSpan 1) — 빈 공간으로 남김
  { key: "issue-air",      label: "Issue Information",     component: MasterAirIssuePanel,      defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "handling-air",   label: "Handling Information",  component: MasterAirHandlingInfoPanel, defaultPosition: { col: 0, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-air",      label: "Cargo",                 component: MasterAirCargoPanel,      defaultPosition: { col: 3, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "dimension",      label: "Dimension",             component: MasterAirDimensionPanel,  defaultPosition: { col: 0, row: 11, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges",    label: "Charge Information",    component: MasterAirChargeInfoPanel, defaultPosition: { col: 3, row: 11, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",      label: "Marks & Numbers",       component: MasterAirMarksPanel,      defaultPosition: { col: 0, row: 13, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods",   label: "Nature & Qty",          component: MasterAirNatureGoodsPanel, defaultPosition: { col: 2, row: 13, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",     label: "Remark",                component: MasterAirRemarkPanel,     defaultPosition: { col: 4, row: 13, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

// House IMP registry + row+2 오프셋 + row 0~1 master-house-bl-grid (Issue 패널 없음)
// Performance 자리 (col 4, row 4, colSpan 2, rowSpan 2) — 빈 공간으로 남김
export const MASTER_BL_AIR_IMP_REGISTRY: WidgetDef[] = [
  { key: "house-bl-grid",  label: "House AWB List",        component: MasterHouseBLGrid,        defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-air",      label: "Party",                 component: MasterAirPartyPanel,      defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air",   label: "Schedule",              component: MasterAirSchedulePanel,   defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air",   label: "Document",              component: MasterAirDocumentPanel,   defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",      label: "Trade",                 component: MasterAirTradePanel,      defaultPosition: { col: 2, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  // Performance 자리 (col 4, row 4, colSpan 2, rowSpan 2) — 빈 공간으로 남김
  { key: "handling-air",   label: "Handling Information",  component: MasterAirHandlingInfoPanel, defaultPosition: { col: 0, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-air",      label: "Cargo",                 component: MasterAirCargoPanel,      defaultPosition: { col: 3, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "dimension",      label: "Dimension",             component: MasterAirDimensionPanel,  defaultPosition: { col: 0, row: 8, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges",    label: "Charge Information",    component: MasterAirChargeInfoPanel, defaultPosition: { col: 3, row: 8, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",      label: "Marks & Numbers",       component: MasterAirMarksPanel,      defaultPosition: { col: 0, row: 10, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods",   label: "Nature & Qty",          component: MasterAirNatureGoodsPanel, defaultPosition: { col: 2, row: 10, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",     label: "Remark",                component: MasterAirRemarkPanel,     defaultPosition: { col: 4, row: 10, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MasterMainTabAir({ variant, active }: Props) {
  const scope    = `master-bl-entry.main.${variant.key}`;
  const registry = variant.direction === "EXP" ? MASTER_BL_AIR_EXP_REGISTRY : MASTER_BL_AIR_IMP_REGISTRY;
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
