import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }           from "@/components/widget/widget-registry";
import type { UseFormReturn }       from "react-hook-form";
import { WidgetGrid }               from "@/components/widget/widget-grid";
import type { MasterBlFormValues }  from "../master-bl-schema";
import { MasterHouseBLGrid }        from "./sections/master-house-bl-grid";
import { MasterPartyPanel, MasterMarksPanel, MasterGoodsDescPanel, MasterRemarkPanel } from "./sections/master-panels";
import { MasterSchedulePanel }      from "./sections/master-schedule-panel";
import { MasterCargoDocPanel }      from "./sections/master-cargo-doc-panel";

interface Props {
  variant: MasterVariantConfig;
  form:    UseFormReturn<MasterBlFormValues>;
  active?: boolean;
}

// SEA Master 전용 레지스트리 — AIR 전용 패널(dim/airCharges)은 MASTER_BL_AIR_REGISTRY에만 등록
export const MASTER_BL_SEA_REGISTRY = (
  form: UseFormReturn<MasterBlFormValues>
): WidgetDef[] => [
  { key: "house-bl-grid",  label: "House B/L List",     component: MasterHouseBLGrid,                                                   defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-master",   label: "Party",              component: (p) => <MasterPartyPanel {...p} form={form} />,                      defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-master",label: "Schedule",           component: (p) => <MasterSchedulePanel {...p} form={form} />,                   defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-doc",      label: "Cargo & Document",   component: (p) => <MasterCargoDocPanel {...p} form={form} />,                   defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "marks-master",   label: "Marks & Numbers",    component: (p) => <MasterMarksPanel {...p} form={form} />,                     defaultPosition: { col: 0, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "goods-desc",     label: "Goods Description",  component: (p) => <MasterGoodsDescPanel {...p} form={form} />,                 defaultPosition: { col: 2, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark",         label: "Remark",             component: (p) => <MasterRemarkPanel {...p} form={form} />,                    defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MasterMainTabSea({ variant, form, active }: Props) {
  const scope    = `master-bl-entry.main.${variant.key}`;
  const registry = MASTER_BL_SEA_REGISTRY(form);
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
