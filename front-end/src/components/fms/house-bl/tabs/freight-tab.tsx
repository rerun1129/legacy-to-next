"use client";

import { useTranslations } from "next-intl";
import type { WidgetDef }  from "@/components/widget/widget-registry";
import { WidgetGrid }      from "@/components/widget/widget-grid";
import {
  FreightRatePanel,
  FreightSellingPanel,
  FreightBuyingPanel,
  FreightAccountPanel,
} from "./sections/freight-panels";

export function FreightTab({ active }: { active?: boolean }) {
  const tf = useTranslations("fms.houseBl.entry.freight");

  const registry: WidgetDef[] = [
    { key: "rate-headers", label: tf("panels.rateHeaders"),      component: FreightRatePanel,    defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
    { key: "selling",      label: tf("panels.sellingDebit"),      component: FreightSellingPanel, defaultPosition: { col: 0, row: 2, colSpan: 3, rowSpan: 3 }, minColSpan: 2, minRowSpan: 2 },
    { key: "buying",       label: tf("panels.buyingCredit"),      component: FreightBuyingPanel,  defaultPosition: { col: 3, row: 2, colSpan: 3, rowSpan: 3 }, minColSpan: 2, minRowSpan: 2 },
    { key: "account-docs", label: tf("panels.accountDocuments"),  component: FreightAccountPanel, defaultPosition: { col: 0, row: 5, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  ];

  return <WidgetGrid scope="freight-tab" variant={undefined} registry={registry} active={active} />;
}
