import type { WidgetDef } from "@/components/widget/widget-registry";
import { WidgetGrid }     from "@/components/widget/widget-grid";
import {
  NonBLPartyPanel,
  NonBLDocumentPanel,
  NonBLSchedulePanel,
  NonBLCargoPanel,
  NonBLDimensionPanel,
  NonBLContainerInfoPanel,
  NonBLRemarkPanel,
} from "./sections/nonbl-panels";

const NON_BL_REGISTRY: WidgetDef[] = [
  { key: "party-nonbl",          label: "Party",                 component: NonBLPartyPanel,         defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 3 }, minColSpan: 1, minRowSpan: 1 },
  { key: "document-nonbl",       label: "Document",              component: NonBLDocumentPanel,      defaultPosition: { col: 0, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "schedule-nonbl",       label: "Schedule",              component: NonBLSchedulePanel,      defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 3 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-nonbl",          label: "Cargo",                 component: NonBLCargoPanel,         defaultPosition: { col: 2, row: 3, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "dimension-nonbl",      label: "Dimension",             component: NonBLDimensionPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "container-info-nonbl", label: "Container Information", component: NonBLContainerInfoPanel, defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-nonbl",         label: "Remark",                component: NonBLRemarkPanel,        defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainNonBL() {
  return <WidgetGrid scope="non-bl-entry.main" registry={NON_BL_REGISTRY} />;
}
