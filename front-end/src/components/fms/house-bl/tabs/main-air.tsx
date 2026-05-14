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

export const HOUSE_BL_AIR_EXP_REGISTRY: WidgetDef[] = [
  { key: "party-air",    label: "Party",             component: PartyPanel,           defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air", label: "Schedule",          component: AirSchedulePanel,     defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air", label: "Document",          component: AirDocumentPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",    label: "Trade",             component: AirTradePanel,        defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "perf-air",     label: "Performance",       component: AirPerformancePanel,  defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "issue-air",    label: "Issue Information", component: AirIssuePanel,        defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-air",    label: "Cargo",             component: AirCargoPanel,        defaultPosition: { col: 3, row: 7, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "handling-air", label: "Handling Information", component: AirHandlingInfoPanel, defaultPosition: { col: 0, row: 7, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "dimension",    label: "Dimension",         component: DimensionPanel,       defaultPosition: { col: 0, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges", label: "Charge Information", component: AirChargeInfoPanel,   defaultPosition: { col: 3, row: 9, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",    label: "Marks & Numbers",   component: MarksPanel,           defaultPosition: { col: 0, row: 11, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods", label: "Nature & Qty",      component: NatureGoodsPanel,     defaultPosition: { col: 2, row: 11, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",  label: "Remark",             component: AirRemarkPanel,       defaultPosition: { col: 4, row: 11, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export const HOUSE_BL_AIR_IMP_REGISTRY: WidgetDef[] = [
  { key: "party-air",    label: "Party",           component: PartyPanel,           defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air", label: "Schedule",        component: AirSchedulePanel,     defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-air", label: "Document",        component: AirDocumentPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "trade-air",    label: "Trade",           component: AirTradePanel,        defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "perf-air",     label: "Performance",     component: AirPerformancePanel,  defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-air",    label: "Cargo",           component: AirCargoPanel,        defaultPosition: { col: 3, row: 4, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "handling-air", label: "Handling Information", component: AirHandlingInfoPanel, defaultPosition: { col: 0, row: 4, colSpan: 3, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "dimension",    label: "Dimension",       component: DimensionPanel,       defaultPosition: { col: 0, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-charges", label: "Charge Information", component: AirChargeInfoPanel, defaultPosition: { col: 3, row: 6, colSpan: 3, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "marks-air",    label: "Marks & Numbers", component: MarksPanel,           defaultPosition: { col: 0, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods", label: "Nature & Qty",    component: NatureGoodsPanel,     defaultPosition: { col: 2, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-air",  label: "Remark",           component: AirRemarkPanel,       defaultPosition: { col: 4, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainTabAir({ variant, active }: Props) {
  const scope    = `house-bl-entry.main.${variant.key}`;
  const registry = variant.direction === "EXP" ? HOUSE_BL_AIR_EXP_REGISTRY : HOUSE_BL_AIR_IMP_REGISTRY;
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
