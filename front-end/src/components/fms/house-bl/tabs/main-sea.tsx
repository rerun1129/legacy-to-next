import type { BLVariantConfig } from "@/lib/bl-variants";
import type { WidgetDef }       from "@/components/widget/widget-registry";
import { WidgetGrid }           from "@/components/widget/widget-grid";
import { WIDGET_REGISTRY }      from "@/components/widget/widget-registry";
import { TruckOrderPanel }      from "@/components/fms/house-bl/panels/truck-order-panel";
import { PartyPanel }           from "./sections/party-panel";
import { SchedulePanel }        from "./sections/schedule-panel";
import { SeaTradePanel }        from "./sections/sea-trade-panel";
import { SeaDocumentPanel }     from "./sections/sea-document-panel";
import { ContainerGridPanel }   from "./sections/container-grid-panel";
import { MarksPanel }           from "./sections/marks-panel";
import { DescriptionPanel }     from "./sections/description-panel";
import { SeaCargoPanel }         from "./sections/sea-cargo-panel";
import { SeaRemarkPanel }       from "./sections/sea-remark-panel";

interface Props { variant: BLVariantConfig; active?: boolean }

export const HOUSE_BL_SEA_REGISTRY: WidgetDef[] = [
  { key: "party-sea",       label: "Party",            component: PartyPanel,        defaultPosition: { col: 0, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "schedule-sea",    label: "Schedule",          component: SchedulePanel,     defaultPosition: { col: 2, row: 0, colSpan: 2, rowSpan: 4 }, minColSpan: 1, minRowSpan: 2 },
  { key: "trade-sea",       label: "Trade & Perf.",     component: SeaTradePanel,     defaultPosition: { col: 4, row: 0, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 2 },
  { key: "document-sea",    label: "Document",          component: SeaDocumentPanel,  defaultPosition: { col: 4, row: 2, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "container-sea",   label: "Container",         component: ContainerGridPanel, defaultPosition: { col: 0, row: 4, colSpan: 4, rowSpan: 2 }, minColSpan: 3, minRowSpan: 1 },
  { key: "cargo-sea",       label: "Cargo",             component: SeaCargoPanel,      defaultPosition: { col: 4, row: 4, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "marks-sea",       label: "Marks & Numbers",   component: MarksPanel,        defaultPosition: { col: 0, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "description-sea", label: "Description",       component: DescriptionPanel,  defaultPosition: { col: 2, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
  { key: "remark-sea",      label: "Remark",            component: SeaRemarkPanel,    defaultPosition: { col: 4, row: 6, colSpan: 2, rowSpan: 2 }, minColSpan: 1, minRowSpan: 1 },
];

export function MainTabSea({ variant, active }: Props) {
  // TRUCK 전용: 트럭오더 그리드만 렌더링
  if (variant.mode === "TRUCK") {
    return (
      <div className="tab-content" style={{ padding: 8 }}>
        <TruckOrderPanel />
      </div>
    );
  }

  // SEA: SEA 전용 위젯 레지스트리 사용
  if (variant.mode === "SEA") {
    const scope = `house-bl-entry.main.${variant.key}`;
    return <WidgetGrid scope={scope} variant={variant} registry={HOUSE_BL_SEA_REGISTRY} active={active} />;
  }

  // NON_BL 등 나머지: 기존 공통 위젯 레지스트리 사용 (영향 회피)
  const scope = `house-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={WIDGET_REGISTRY} active={active} />;
}
