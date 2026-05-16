import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }           from "@/components/widget/widget-registry";
import type { UseFormReturn }       from "react-hook-form";
import { WidgetGrid }               from "@/components/widget/widget-grid";
import type { MasterBlFormValues }  from "../master-bl-schema";
import { MasterHouseBLGrid }        from "./sections/master-house-bl-grid";
import { MasterPartyPanel, MasterMarksPanel, MasterGoodsDescPanel, MasterRemarkPanel } from "./sections/master-panels";
import { MasterSchedulePanel }      from "./sections/master-schedule-panel";
import { MasterCargoDocPanel }      from "./sections/master-cargo-doc-panel";
// AIR 전용 패널 — 자체 수정 없이 registry에만 등록 (격리 원칙)
import { MasterContainerDimPanel }  from "./sections/master-container-dim-panel";
import { MasterAirChargesPanel }    from "./sections/master-air-charges-panel";
import { MasterAirIssuePanel }      from "./sections/master-air-issue-panel";

interface Props {
  variant: MasterVariantConfig;
  form:    UseFormReturn<MasterBlFormValues>;
  active?: boolean;
}

// AIR Master 전용 레지스트리 — dim/airCharges 패널 등록. AIR 패널 자체는 미수정(격리 원칙).
export const MASTER_BL_AIR_REGISTRY = (
  form: UseFormReturn<MasterBlFormValues>
): WidgetDef[] => [
  { key: "house-bl-grid",  label: "House B/L List",     component: MasterHouseBLGrid,                                                   defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-master",   label: "Party",              component: (p) => <MasterPartyPanel {...p} form={form} />,                      defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-master",label: "Schedule",           component: (p) => <MasterSchedulePanel {...p} form={form} />,                   defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-doc",      label: "Cargo & Document",   component: (p) => <MasterCargoDocPanel {...p} form={form} />,                   defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "container-dim",  label: "Container / Dim.",   component: (p) => <MasterContainerDimPanel {...p} form={form} />,               defaultPosition: { col: 0, row: 6, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks-master",   label: "Marks & Numbers",    component: (p) => <MasterMarksPanel {...p} form={form} />,                     defaultPosition: { col: 0, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "goods-desc",     label: "Goods Description",  component: (p) => <MasterGoodsDescPanel {...p} form={form} />,                 defaultPosition: { col: 2, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark",         label: "Remark",             component: (p) => <MasterRemarkPanel {...p} form={form} />,                    defaultPosition: { col: 4, row: 8, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "air-charges",    label: "Air Charges",        component: (p) => <MasterAirChargesPanel {...p} form={form} />,                defaultPosition: { col: 0, row: 10, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "air-issue",      label: "Issue Information",  component: (p) => <MasterAirIssuePanel {...p} form={form} />,                   defaultPosition: { col: 0, row: 12, colSpan: 3, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
];

export function MasterMainTabAir({ variant, form, active }: Props) {
  const scope    = `master-bl-entry.main.${variant.key}`;
  const registry = MASTER_BL_AIR_REGISTRY(form);
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
