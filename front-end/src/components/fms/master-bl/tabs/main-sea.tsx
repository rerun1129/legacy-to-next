"use client";

import { useMemo }                  from "react";
import { useTranslations }          from "next-intl";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }           from "@/components/widget/widget-registry";
import type { UseFormReturn }       from "react-hook-form";
import { WidgetGrid }               from "@/components/widget/widget-grid";
import type { MasterBlFormValues }  from "../master-bl-schema";
import { MasterHouseBLGrid }        from "./sections/master-house-bl-grid";
import { MasterPartyPanel, MasterMarksPanel, MasterGoodsDescPanel, MasterRemarkPanel } from "./sections/master-panels";
import { MasterSchedulePanel }      from "./sections/master-schedule-panel";
import { MasterCargoDocPanel }      from "./sections/master-cargo-doc-panel";
import { MasterContainerGrid }      from "./sections/master-container-grid";

interface Props {
  variant: MasterVariantConfig;
  form:    UseFormReturn<MasterBlFormValues>;
  active?: boolean;
}

// SEA Master 전용 레지스트리 — labelKey は i18n の panels.* キーと対応; label はフォールバック用英語テキスト
// AIR 전용 패널(dim/airCharges)은 MASTER_BL_AIR_REGISTRY에만 등록
const buildSeaRegistry = (form: UseFormReturn<MasterBlFormValues>): WidgetDef[] => [
  { key: "house-bl-grid",   label: "House B/L List",    labelKey: "houseBLList",      component: MasterHouseBLGrid,                                                defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "party-master",    label: "Party",             labelKey: "party",            component: (p) => <MasterPartyPanel {...p} form={form} />,                   defaultPosition: { col: 0, row: 2, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-master", label: "Schedule",          labelKey: "schedule",         component: (p) => <MasterSchedulePanel {...p} form={form} />,                defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "cargo-doc",       label: "Cargo & Document",  labelKey: "cargoDocument",    component: (p) => <MasterCargoDocPanel {...p} form={form} />,                defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "container-master",label: "Container",         labelKey: "container",        component: MasterContainerGrid,                                              defaultPosition: { col: 2, row: 4, colSpan: 4, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks-master",    label: "Marks & Numbers",   labelKey: "marksNumbers",     component: (p) => <MasterMarksPanel {...p} form={form} />,                   defaultPosition: { col: 0, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "goods-desc",      label: "Goods Description", labelKey: "goodsDescription", component: (p) => <MasterGoodsDescPanel {...p} form={form} />,               defaultPosition: { col: 2, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark",          label: "Remark",            labelKey: "remark",           component: (p) => <MasterRemarkPanel {...p} form={form} />,                  defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

// 後方互換のため旧名でも再エクスポート — 外部から直接参照される可能性あり
export const MASTER_BL_SEA_REGISTRY = buildSeaRegistry;

export function MasterMainTabSea({ variant, form, active }: Props) {
  const tp = useTranslations("fms.masterBl.entry.panels");

  // labelKey があるエントリーのみ label を上書き。
  // useMemo のメモ化で locale 切替時も余計な再生成を抑える。
  const registry = useMemo(
    () => buildSeaRegistry(form).map(w => w.labelKey ? { ...w, label: tp(w.labelKey) } : w),
    [form, tp]
  );

  const scope = `master-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={registry} active={active} />;
}
