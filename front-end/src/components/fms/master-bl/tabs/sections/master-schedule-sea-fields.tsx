"use client";

// SEA Master Schedule 패널 전용 카탈로그 필드 컴포넌트 + FieldWidgetDef 빌더
// master-schedule-panel.tsx에서 분리 (300줄 초과 기준, CLAUDE.md Critical)
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { FieldWidgetDef } from "@/components/widget/field-widget-list";
import {
  LinerLcnField, VesselField, VoyageField, EtdField, EtaField, IssueDateField, OnboardDateField,
  PolField, PodField, PorField, FinalDestField,
  LoadTypeField, ServiceTermField, BlTypeField,
} from "./master-schedule-sea-atoms";

// ── SEA schedule FieldWidgetDef 배열 빌더 ───────────────────────────────────
export function buildSeaFields(panelScope: string, isExp: boolean): FieldWidgetDef[] {
  const linerItems: FieldItemDef[] = [
    { key: "liner",   render: () => <LinerLcnField /> },
    { key: "vessel",  render: () => <VesselField /> },
    { key: "voyage",  render: () => <VoyageField /> },
    { key: "etd",     render: () => <EtdField /> },
    { key: "eta",     render: () => <EtaField /> },
  ];

  const portItems: FieldItemDef[] = [
    { key: "pol",        render: () => <PolField /> },
    { key: "pod",        render: () => <PodField /> },
    { key: "por",        render: () => <PorField /> },
    { key: "final-dest", render: () => <FinalDestField /> },
  ];

  const issueItems: FieldItemDef[] = [
    { key: "issue-date",   render: () => <IssueDateField /> },
    { key: "onboard-date", render: () => <OnboardDateField /> },
  ];

  const loadTypeItems: FieldItemDef[] = [
    { key: "load-type",    render: () => <LoadTypeField /> },
    { key: "service-term", render: () => <ServiceTermField /> },
    { key: "bl-type",      render: () => <BlTypeField /> },
  ];

  return [
    { key: "liner-vessel", label: "Liner & Vessel", render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={linerItems} /> },
    { key: "ports",        label: "Ports",          render: () => <FieldItemGrid itemScope={`${panelScope}.ports`} items={portItems} shouldShowRowControls={false} /> },
    { key: "terms",        label: "Terms",          render: () => <FieldItemGrid itemScope={`${panelScope}.terms`} items={loadTypeItems} /> },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];
}
