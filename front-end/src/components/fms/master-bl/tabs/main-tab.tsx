import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }           from "@/components/widget/widget-registry";
import { WidgetGrid }               from "@/components/widget/widget-grid";
import { MasterHouseBLGrid }        from "./sections/master-house-bl-grid";
import { MasterPartyPanel, MasterMarksPanel, MasterGoodsDescPanel, MasterRemarkPanel } from "./sections/master-panels";
import { MasterSchedulePanel }      from "./sections/master-schedule-panel";
import { MasterCargoDocPanel }      from "./sections/master-cargo-doc-panel";
import { MasterContainerDimPanel }  from "./sections/master-container-dim-panel";

interface Props { variant: MasterVariantConfig }

export const MASTER_BL_REGISTRY: WidgetDef[] = [
  { key: "house-bl-grid",  label: "House B/L List",     component: MasterHouseBLGrid,       defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-master",   label: "Party",               component: MasterPartyPanel,         defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-master",label: "Schedule",            component: MasterSchedulePanel,      defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-doc",      label: "Cargo & Document",   component: MasterCargoDocPanel,      defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "container-dim",  label: "Container / Dim.",   component: MasterContainerDimPanel,  defaultPosition: { col: 0, row: 6, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks-master",   label: "Marks & Numbers",    component: MasterMarksPanel,         defaultPosition: { col: 0, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "goods-desc",     label: "Goods Description",  component: MasterGoodsDescPanel,     defaultPosition: { col: 2, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark",         label: "Remark",             component: MasterRemarkPanel,        defaultPosition: { col: 4, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MasterMainTab({ variant }: Props) {
  const scope = `master-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={MASTER_BL_REGISTRY} />;
}
