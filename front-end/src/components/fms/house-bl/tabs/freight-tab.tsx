"use client";

import { useTranslations } from "next-intl";
import type { WidgetDef, WidgetProps } from "@/components/widget/widget-registry";
import { WidgetGrid }      from "@/components/widget/widget-grid";
import { FreightRatePanel } from "./sections/freight-rate-panel";
import {
  FreightSellingPanel,
  FreightBuyingPanel,
} from "./sections/freight-panels";
import { FreightAccountPanel } from "./sections/freight-account-panel";
import type { Mode } from "@/lib/bl-variants";

interface FreightTabProps {
  active?: boolean;
  /** BLVariantConfig.mode — SEA/AIR/TRUCK/NON_BL 4종. Per scope 필터에 사용. */
  mode?: Mode;
  layoutScope?: string;
  /** 발행/Account Documents 배선용 blType. house/non/truck → 'HOUSE', master → 'MASTER'. */
  blType?: "HOUSE" | "MASTER";
  /** 현재 저장된 B/L id. 신규 미저장이면 null/undefined. */
  blId?: number | null;
  /**
   * 발행(issue) 또는 서류 삭제(account-docs) 성공 후 엔트리가 수신하는 콜백.
   * entry에서 detailLoadedRef.current=false + detail invalidate를 수행해
   * freight 그리드(useFieldArray)가 최신 BE 응답으로 리셋되도록 보장한다.
   */
  onFreightMutated?: () => void;
}

export function FreightTab({ active, mode, layoutScope, blType, blId, onFreightMutated }: FreightTabProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");

  // FreightRatePanel은 SEA/AIR 구분만 필요 — TRUCK/NON_BL은 undefined 처리
  const ratePanelMode = mode === "SEA" || mode === "AIR" ? mode : undefined;

  const registry: WidgetDef[] = [
    {
      key: "rate-headers",
      label: tf("panels.freightInformation"),
      // WidgetProps를 수신해야 ComponentType<WidgetProps>와 타입 일치 — 인자는 타입 목적으로만 선언
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      component: (_props: WidgetProps) => <FreightRatePanel mode={ratePanelMode} />,
      defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 1 },
      minColSpan: 2,
      minRowSpan: 1,
    },
    {
      key: "selling",
      label: tf("panels.sellingDebit"),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      component: (_props: WidgetProps) => (
        <FreightSellingPanel mode={mode} blType={blType} blId={blId} onFreightMutated={onFreightMutated} />
      ),
      defaultPosition: { col: 0, row: 1, colSpan: 6, rowSpan: 2 },
      minColSpan: 2,
      minRowSpan: 2,
    },
    {
      key: "buying",
      label: tf("panels.buyingCredit"),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      component: (_props: WidgetProps) => (
        <FreightBuyingPanel mode={mode} blType={blType} blId={blId} onFreightMutated={onFreightMutated} />
      ),
      defaultPosition: { col: 0, row: 3, colSpan: 6, rowSpan: 2 },
      minColSpan: 2,
      minRowSpan: 2,
    },
    {
      key: "account-docs",
      label: tf("panels.accountDocuments"),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      component: (_props: WidgetProps) => (
        <FreightAccountPanel blType={blType} blId={blId} onFreightMutated={onFreightMutated} />
      ),
      defaultPosition: { col: 0, row: 5, colSpan: 6, rowSpan: 2 },
      minColSpan: 3,
      minRowSpan: 1,
    },
  ];

  return <WidgetGrid scope={layoutScope ?? "freight-tab"} variant={undefined} registry={registry} active={active} />;
}
