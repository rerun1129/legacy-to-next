import type { WidgetDef } from "@/components/widget/widget-registry";
import { WidgetGrid }     from "@/components/widget/widget-grid";
import { NonBLPartyPanel, NonBLSchedulePanel, NonBLWorkDivPanel, NonBLReferencePanel, NonBLPerformancePanel } from "./sections/nonbl-panels";

const NON_BL_REGISTRY: WidgetDef[] = [
  { key: "party-nonbl",   label: "Party",            component: NonBLPartyPanel,       defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-nonbl",label: "Schedule",         component: NonBLSchedulePanel,    defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "workdiv-nonbl", label: "Work Division",    component: NonBLWorkDivPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "ref-nonbl",     label: "Reference Numbers",component: NonBLReferencePanel,   defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "perf-nonbl",    label: "Performance",      component: NonBLPerformancePanel, defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainNonBL() {
  return <WidgetGrid scope="non-bl-entry.main" registry={NON_BL_REGISTRY} />;
}
