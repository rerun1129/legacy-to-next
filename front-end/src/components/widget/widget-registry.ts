import type { ComponentType } from "react";
import type { WidgetKey, WidgetPosition } from "@/lib/use-widget-layout";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { PartyPanel }         from "@/components/fms/house-bl/tabs/sections/party-panel";
import { SchedulePanel }      from "@/components/fms/house-bl/tabs/sections/schedule-panel";
import { TradePanel }         from "@/components/fms/house-bl/tabs/sections/trade-panel";
import { ContainerGridPanel } from "@/components/fms/house-bl/tabs/sections/container-grid-panel";
import { MarksPanel }         from "@/components/fms/house-bl/tabs/sections/marks-panel";
import { DescriptionPanel }   from "@/components/fms/house-bl/tabs/sections/description-panel";
import { ItemHsPanel }        from "@/components/fms/house-bl/tabs/sections/item-hs-panel";

export interface WidgetProps {
  variant: BLVariantConfig;
}

export interface WidgetDef {
  key:             WidgetKey;
  label:           string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  component:       ComponentType<any>;
  defaultPosition: Omit<WidgetPosition, "key">;
  minColSpan:      number;
  minRowSpan:      number;
}

export const WIDGET_REGISTRY: WidgetDef[] = [
  { key: "party",       label: "Party",           component: PartyPanel,         defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule",    label: "Schedule",         component: SchedulePanel,      defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "trade",       label: "Trade & Perf.",    component: TradePanel,         defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "container",   label: "Container",        component: ContainerGridPanel, defaultPosition: { col: 0, row: 4, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks",       label: "Marks & Numbers",  component: MarksPanel,         defaultPosition: { col: 0, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "description", label: "Description",      component: DescriptionPanel,   defaultPosition: { col: 2, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "item-hs",     label: "Item / HS Code",   component: ItemHsPanel,        defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 2, minRowSpan: 2 },
];

export const REGISTRY_MAP = Object.fromEntries(
  WIDGET_REGISTRY.map(d => [d.key, d])
) as Record<WidgetKey, WidgetDef>;

export function getDefaultPositions(): WidgetPosition[] {
  return WIDGET_REGISTRY.map(d => ({ key: d.key, ...d.defaultPosition }));
}
