import type { WidgetDef } from "@/components/widget/widget-registry";
import { WidgetGrid }     from "@/components/widget/widget-grid";
import { TruckPartyPanel, TruckSchedulePanel, TruckCargoPanel, TruckDocumentPanel, TruckPerformancePanel, TruckRemarkPanel, TruckDimensionPanel } from "./sections/truck-panels";
import { TruckOrderGridPanel }   from "./sections/truck-order-grid-panel";
import { TruckMarksPanel }       from "./sections/truck-marks-panel";
import { TruckDescriptionPanel } from "./sections/truck-description-panel";

const TRUCK_REGISTRY: WidgetDef[] = [
  { key: "party-truck",   label: "Party",       component: TruckPartyPanel,       defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-truck",label: "Schedule",    component: TruckSchedulePanel,    defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-truck",   label: "Cargo",       component: TruckCargoPanel,       defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "document-truck",label: "Document",    component: TruckDocumentPanel,    defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "perf-truck",      label: "Performance", component: TruckPerformancePanel, defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "dimension-truck", label: "Dimension",   component: TruckDimensionPanel,   defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-truck",    label: "Remark",      component: TruckRemarkPanel,      defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "truck-order-grid",   label: "Truck Information", component: TruckOrderGridPanel,   defaultPosition: { col: 0, row: 4, colSpan: 4, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks-truck",        label: "Marks & Numbers",  component: TruckMarksPanel,       defaultPosition: { col: 0, row: 6, colSpan: 2, rowSpan: 3 }, minColSpan: 1, minRowSpan: 1 },
  { key: "description-truck",  label: "Description",      component: TruckDescriptionPanel, defaultPosition: { col: 2, row: 6, colSpan: 2, rowSpan: 3 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainTruck({ active }: { active?: boolean }) {
  return <WidgetGrid scope="truck-bl-entry.main" registry={TRUCK_REGISTRY} active={active} />;
}
