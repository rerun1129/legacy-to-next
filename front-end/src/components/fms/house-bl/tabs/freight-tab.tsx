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
}

export function FreightTab({ active, mode, layoutScope }: FreightTabProps) {
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
      component: (_props: WidgetProps) => <FreightSellingPanel mode={mode} />,
      defaultPosition: { col: 0, row: 1, colSpan: 6, rowSpan: 2 },
      minColSpan: 2,
      minRowSpan: 2,
    },
    {
      key: "buying",
      label: tf("panels.buyingCredit"),
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      component: (_props: WidgetProps) => <FreightBuyingPanel mode={mode} />,
      defaultPosition: { col: 0, row: 3, colSpan: 6, rowSpan: 2 },
      minColSpan: 2,
      minRowSpan: 2,
    },
    {
      key: "account-docs",
      label: tf("panels.accountDocuments"),
      component: FreightAccountPanel,
      defaultPosition: { col: 0, row: 5, colSpan: 6, rowSpan: 2 },
      minColSpan: 3,
      minRowSpan: 1,
    },
  ];

  return <WidgetGrid scope={layoutScope ?? "freight-tab"} variant={undefined} registry={registry} active={active} />;
}
