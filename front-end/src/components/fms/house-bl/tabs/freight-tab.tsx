"use client";

import { useTranslations } from "next-intl";
import type { WidgetDef, WidgetProps } from "@/components/widget/widget-registry";
import { WidgetGrid }      from "@/components/widget/widget-grid";
import { FreightRatePanel } from "./sections/freight-rate-panel";
import {
  FreightSellingPanel,
  FreightBuyingPanel,
  FreightAccountPanel,
} from "./sections/freight-panels";

export function FreightTab({ active, mode, layoutScope }: { active?: boolean; mode?: "SEA" | "AIR"; layoutScope?: string }) {
  const tf = useTranslations("fms.houseBl.entry.freight");

  const registry: WidgetDef[] = [
    {
      key: "rate-headers",
      label: tf("panels.freightInformation"),
      // WidgetProps를 수신해야 ComponentType<WidgetProps>와 타입 일치 — 인자는 타입 목적으로만 선언
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      component: (_props: WidgetProps) => <FreightRatePanel mode={mode} />,
      defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 1 },
      minColSpan: 2,
      minRowSpan: 1,
    },
    {
      key: "selling",
      label: tf("panels.sellingDebit"),
      component: FreightSellingPanel,
      defaultPosition: { col: 0, row: 1, colSpan: 6, rowSpan: 2 },
      minColSpan: 2,
      minRowSpan: 2,
    },
    {
      key: "buying",
      label: tf("panels.buyingCredit"),
      component: FreightBuyingPanel,
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
