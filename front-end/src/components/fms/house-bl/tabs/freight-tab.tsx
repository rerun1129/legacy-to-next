import type { WidgetDef } from "@/components/widget/widget-registry";
import { WidgetGrid }    from "@/components/widget/widget-grid";
import {
  FreightRatePanel,
  FreightSellingPanel,
  FreightBuyingPanel,
  FreightAccountPanel,
} from "./sections/freight-panels";

const FREIGHT_REGISTRY: WidgetDef[] = [
  { key: "rate-headers", label: "Rate Headers",      component: FreightRatePanel,    defaultPosition: { col: 0, row: 0, colSpan: 6, rowSpan: 2 }, minColSpan: 2, minRowSpan: 1 },
  { key: "selling",      label: "Selling / Debit",   component: FreightSellingPanel, defaultPosition: { col: 0, row: 2, colSpan: 3, rowSpan: 3 }, minColSpan: 2, minRowSpan: 2 },
  { key: "buying",       label: "Buying / Credit",   component: FreightBuyingPanel,  defaultPosition: { col: 3, row: 2, colSpan: 3, rowSpan: 3 }, minColSpan: 2, minRowSpan: 2 },
  { key: "account-docs", label: "Account Documents", component: FreightAccountPanel, defaultPosition: { col: 0, row: 5, colSpan: 6, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
];

export function FreightTab({ active }: { active?: boolean }) {
  return <WidgetGrid scope="freight-tab" variant={undefined} registry={FREIGHT_REGISTRY} active={active} />;
}
