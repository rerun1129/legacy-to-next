"use client";

import { useMemo }                from "react";
import { useTranslations }        from "next-intl";
import type { WidgetDef }         from "@/components/widget/widget-registry";
import { WidgetGrid }             from "@/components/widget/widget-grid";
import { TruckPartyPanel, TruckSchedulePanel, TruckCargoPanel, TruckDocumentPanel, TruckPerformancePanel, TruckRemarkPanel, TruckDimensionPanel } from "./sections/truck-panels";
import { TruckOrderGridPanel }    from "./sections/truck-order-grid-panel";
import { TruckMarksPanel }        from "./sections/truck-marks-panel";
import { TruckDescriptionPanel }  from "./sections/truck-description-panel";

// labelKey は i18n の panels.* キーと対応; label はフォールバック用英語テキスト
// copy-bl-modal 등 form context 없이 패널 목록만 필요한 곳에서 사용 가능 — form 비의존 정적 배열
export const TRUCK_REGISTRY: WidgetDef[] = [
  { key: "party-truck",        label: "Party",              labelKey: "party",            component: TruckPartyPanel,       defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-truck",     label: "Schedule",           labelKey: "schedule",         component: TruckSchedulePanel,    defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "cargo-truck",        label: "Cargo",              labelKey: "cargo",            component: TruckCargoPanel,       defaultPosition: { col: 2, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "document-truck",     label: "Document",           labelKey: "document",         component: TruckDocumentPanel,    defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "perf-truck",         label: "Performance",        labelKey: "performance",      component: TruckPerformancePanel, defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "dimension-truck",    label: "Dimension",          labelKey: "dimension",        component: TruckDimensionPanel,   defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-truck",       label: "Remark",             labelKey: "remark",           component: TruckRemarkPanel,      defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "truck-order-grid",   label: "Truck Information",  labelKey: "truckInformation", component: TruckOrderGridPanel,   defaultPosition: { col: 0, row: 4, colSpan: 4, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "marks-truck",        label: "Marks & Numbers",    labelKey: "marksNumbers",     component: TruckMarksPanel,       defaultPosition: { col: 0, row: 6, colSpan: 2, rowSpan: 3 }, minColSpan: 1, minRowSpan: 1 },
  { key: "description-truck",  label: "Description",        labelKey: "description",      component: TruckDescriptionPanel, defaultPosition: { col: 2, row: 6, colSpan: 2, rowSpan: 3 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainTruck({ active }: { active?: boolean }) {
  // Rules of Hooks: unconditionally at top
  const tp = useTranslations("fms.truckBl.entry.panels");

  // labelKey があるエントリーのみ label を上書き。useMemo でロケール切替時の余計な再生成を抑える
  const registry = useMemo(
    () => TRUCK_REGISTRY.map(w => w.labelKey ? { ...w, label: tp(w.labelKey) } : w),
    [tp]
  );

  return <WidgetGrid scope="truck-bl-entry.main" registry={registry} active={active} />;
}
