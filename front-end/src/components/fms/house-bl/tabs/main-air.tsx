import type { BLVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }       from "@/components/widget/widget-registry";
import { WidgetGrid }           from "@/components/widget/widget-grid";
import { PartyPanel }           from "./sections/party-panel";
import { AirSchedulePanel }     from "./sections/air-schedule-panel";
import { AirTradePanel }        from "./sections/air-trade-panel";
import { AirCargoPanel }        from "./sections/air-cargo-panel";
import { DimensionPanel }       from "./sections/dimension-panel";
import { MarksPanel }           from "./sections/marks-panel";
import { NatureGoodsPanel }     from "./sections/nature-goods-panel";

interface Props { variant: BLVariantConfig; active?: boolean }

export const HOUSE_BL_AIR_REGISTRY: WidgetDef[] = [
  { key: "party-air",    label: "Party",           component: PartyPanel,       defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 6 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-air", label: "Schedule",        component: AirSchedulePanel, defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "trade-air",    label: "Trade",           component: AirTradePanel,    defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-air",    label: "Cargo",           component: AirCargoPanel,    defaultPosition: { col: 2, row: 4, colSpan: 4, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "dimension",    label: "Dimension",       component: DimensionPanel,   defaultPosition: { col: 0, row: 6, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks-air",    label: "Marks & Numbers", component: MarksPanel,       defaultPosition: { col: 0, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "nature-goods", label: "Nature & Qty",    component: NatureGoodsPanel, defaultPosition: { col: 2, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainTabAir({ variant, active }: Props) {
  const scope = `house-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={HOUSE_BL_AIR_REGISTRY} active={active} />;
}
