"use client";

import { useMemo }        from "react";
import { useTranslations } from "next-intl";
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

// labelKey は i18n の panels.* キーと対応; label はフォールバック用英語テキスト
// copy-bl-modal.tsx에서 form 없이 패널 목록(key/label)을 읽기 위해 export
export const NON_BL_REGISTRY: WidgetDef[] = [
  { key: "party-nonbl",          label: "Party",                 labelKey: "party",         component: NonBLPartyPanel,         defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "document-nonbl",       label: "Document",              labelKey: "document",      component: NonBLDocumentPanel,      defaultPosition: { col: 0, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "schedule-nonbl",       label: "Schedule",              labelKey: "schedule",      component: NonBLSchedulePanel,      defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-nonbl",          label: "Cargo",                 labelKey: "cargo",         component: NonBLCargoPanel,         defaultPosition: { col: 2, row: 3, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "dimension-nonbl",      label: "Dimension",             labelKey: "dimension",     component: NonBLDimensionPanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "container-info-nonbl", label: "Container Information", labelKey: "containerInfo", component: NonBLContainerInfoPanel, defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-nonbl",         label: "Remark",                labelKey: "remark",        component: NonBLRemarkPanel,        defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 1 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainNonBL({ active }: { active?: boolean }) {
  // Rules of Hooks: unconditionally at top
  const tp = useTranslations("fms.nonBl.entry.panels");

  // labelKey があるエントリーのみ label を上書き。useMemo でロケール切替時の余計な再生成を抑える
  const registry = useMemo(
    () => NON_BL_REGISTRY.map(w => w.labelKey ? { ...w, label: tp(w.labelKey) } : w),
    [tp]
  );

  return <WidgetGrid scope="non-bl-entry.main" registry={registry} active={active} />;
}
