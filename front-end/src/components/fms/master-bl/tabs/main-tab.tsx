import type { UseFormReturn } from "react-hook-form";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }           from "@/components/widget/widget-registry";
import type { MasterBlFormValues }  from "../master-bl-schema";
import { WidgetGrid }               from "@/components/widget/widget-grid";
import { MasterHouseBLGrid }        from "./sections/master-house-bl-grid";
import { MasterPartyPanel, MasterMarksPanel, MasterGoodsDescPanel, MasterRemarkPanel } from "./sections/master-panels";
import { MasterSchedulePanel }      from "./sections/master-schedule-panel";
import { MasterCargoDocPanel }      from "./sections/master-cargo-doc-panel";
import { MasterContainerDimPanel }  from "./sections/master-container-dim-panel";
import { MasterAirChargesPanel }    from "./sections/master-air-charges-panel";

interface Props {
  variant: MasterVariantConfig;
  form: UseFormReturn<MasterBlFormValues>;
}

function makeRegistry(form: UseFormReturn<MasterBlFormValues>): WidgetDef[] {
  return [
    { key: "house-bl-grid",  label: "House B/L List",    component: MasterHouseBLGrid,       defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
    { key: "party-master",   label: "Party",             component: (p) => <MasterPartyPanel {...p} form={form} />,          defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
    { key: "schedule-master",label: "Schedule",          component: (p) => <MasterSchedulePanel {...p} form={form} />,      defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
    { key: "cargo-doc",      label: "Cargo & Document", component: (p) => <MasterCargoDocPanel {...p} form={form} />,      defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
    { key: "container-dim",  label: "Container / Dim.", component: (p) => <MasterContainerDimPanel {...p} form={form} />,  defaultPosition: { col: 0, row: 6, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
    { key: "marks-master",   label: "Marks & Numbers",  component: (p) => <MasterMarksPanel {...p} form={form} />,         defaultPosition: { col: 0, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
    { key: "goods-desc",     label: "Goods Description",component: (p) => <MasterGoodsDescPanel {...p} form={form} />,    defaultPosition: { col: 2, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
    { key: "remark",         label: "Remark",           component: (p) => <MasterRemarkPanel {...p} form={form} />,        defaultPosition: { col: 4, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
    { key: "air-charges",    label: "Air Charges",      component: (p) => <MasterAirChargesPanel {...p} form={form} />,   defaultPosition: { col: 0, row: 10, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  ];
}

export function MasterMainTab({ variant, form }: Props) {
  const scope    = `master-bl-entry.main.${variant.key}`;
  const registry = makeRegistry(form);
  return <WidgetGrid scope={scope} variant={variant} registry={registry} />;
}
