"use client";

// SEA Master Schedule 패널 전용 카탈로그 필드 컴포넌트 + FieldWidgetDef 빌더
// master-schedule-panel.tsx에서 분리 (300줄 초과 기준, CLAUDE.md Critical)
import { useTranslations } from "next-intl";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { FieldWidgetDef } from "@/components/widget/field-widget-list";
import {
  LinerLcnField, VesselField, VoyageField, EtdField, EtaField, IssueDateField,
  PolField, PodField, PorField, FinalDestField,
} from "./master-schedule-sea-atoms";

// ── 줄 단위 items (순수 렌더 — 라벨은 atoms 컴포넌트 내부에서 자체 관리) ──────
const linerItems:     FieldItemDef[] = [{ key: "liner",      render: () => <LinerLcnField /> }];
const vesselItems:    FieldItemDef[] = [{ key: "vessel",     render: () => <VesselField /> },  { key: "voyage",    render: () => <VoyageField /> }];
const dateItems:      FieldItemDef[] = [{ key: "etd",        render: () => <EtdField /> },     { key: "eta",       render: () => <EtaField /> }];
const porItems:       FieldItemDef[] = [{ key: "por",        render: () => <PorField /> }];
const polItems:       FieldItemDef[] = [{ key: "pol",        render: () => <PolField /> }];
const podItems:       FieldItemDef[] = [{ key: "pod",        render: () => <PodField /> }];
const finalDestItems: FieldItemDef[] = [{ key: "final-dest", render: () => <FinalDestField /> }];
const issueDateItems: FieldItemDef[] = [{ key: "issue-date", render: () => <IssueDateField /> }];

// ── SEA schedule FieldWidgetDef 배열 빌더 ───────────────────────────────────
// 옵션 A: 줄 단위 widget 분리 — 각 widget이 한 행을 담당하여 레이아웃 의도 명확
// IssueDate 단독 줄: items가 1개이므로 FieldItemGrid가 isSingle 분기로 좌측 배치 + 우측 빈 슬롯 자동 처리
export function buildSeaFields(
  panelScope: string,
  isExp: boolean,
  tf?: ReturnType<typeof useTranslations>,
): FieldWidgetDef[] {
  // tf 미전달 시 영어 fallback (하위호환 — SEA atoms 자체 라벨은 별도 처리)
  const l = (key: string, fallback: string) => tf ? tf(key) : fallback;

  const mk = (key: string, label: string, items: FieldItemDef[], cols: number = 2): FieldWidgetDef => ({
    key,
    label,
    render: () => <FieldItemGrid itemScope={`${panelScope}.${key}`} items={items} cols={cols} shouldShowRowControls={false} />,
  });

  const common: FieldWidgetDef[] = [
    mk("liner",      l("liner",      "Liner"),             linerItems,     1),
    mk("vessel",     l("vesselVoyage", "Vessel / Voyage"), vesselItems,    2),
    mk("dates",      `${l("etd", "ETD")} / ${l("eta", "ETA")}`, dateItems, 2),
    mk("por",        l("por",        "POR"),               porItems,       1),
    mk("pol",        l("pol",        "POL"),               polItems,       1),
    mk("pod",        l("pod",        "POD"),               podItems,       1),
    mk("final-dest", l("finalDest",  "Final Dest."),       finalDestItems, 1),
  ];

  return isExp
    ? [...common, mk("issue-date", l("issueDate", "Issue Date"), issueDateItems, 2)]
    : common;
}
