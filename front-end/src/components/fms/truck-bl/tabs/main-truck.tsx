import type { WidgetDef } from "@/components/widget/widget-registry";
import { WidgetGrid }     from "@/components/widget/widget-grid";
import { TruckPartyPanel, TruckSchedulePanel, TruckCargoPanel, TruckDocumentPanel, TruckPerformancePanel } from "./sections/truck-panels";

const TRUCK_REGISTRY: WidgetDef[] = [
  { key: "party-truck",   label: "Party",       component: TruckPartyPanel,       defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-truck",label: "Schedule",    component: TruckSchedulePanel,    defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-truck",   label: "Cargo",       component: TruckCargoPanel,       defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "document-truck",label: "Document",    component: TruckDocumentPanel,    defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "perf-truck",    label: "Performance", component: TruckPerformancePanel, defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainTruck() {
  return <WidgetGrid scope="truck-bl-entry.main" variant={{}} registry={TRUCK_REGISTRY} />;
}
