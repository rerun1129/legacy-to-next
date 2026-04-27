import { Search } from "lucide-react";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";

// ── 공통 헬퍼 ──────────────────────────────────────────────
function LiField({ label, value, req }: { label: string; value: string; req?: boolean }) {
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        <input defaultValue={value} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
      </div>
    </div>
  );
}

function LcnField({ label, code, name }: { label: string; code: string; name: string }) {
  return (
    <div className="lcn">
      <span className="lcn__label is-required">{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input defaultValue={code} style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        <Search size={12} className="lcn__icon" />
      </div>
      <input className="lcn__name" defaultValue={name} placeholder="Name" />
    </div>
  );
}

// ── Field item 정의 ─────────────────────────────────────────
const TRADE_TERM_ITEMS: FieldItemDef[] = [
  { key: "incoterms",    render: () => <LiField label="Incoterms"    value="FOB"     req /> },
  { key: "freight-term", render: () => <LiField label="Freight Term" value="Prepaid" req /> },
  { key: "payable-at",   render: () => <LiField label="Payable At"   value="ORIGIN" /> },
  { key: "co-load",      render: () => <LiField label="Co-Load"      value="N" /> },
];

const PERF_ITEMS: FieldItemDef[] = [
  { key: "customer", render: () => <LcnField label="Actual Customer" code="HJTR001" name="한진무역(주)" /> },
  { key: "sales",    render: () => <LcnField label="Sales Man"       code="LJY"     name="이진영" /> },
  { key: "operator", render: () => <LcnField label="Operator"        code="KYS"     name="김영선" /> },
  { key: "team",     render: () => <LcnField label="Team"            code="SEA-EXP" name="해상수출팀" /> },
];

export function TradePanel({ variant }: { variant?: AnyVariantConfig }) {
  const panelScope = variant ? `trade-panel.${variant.key}` : "trade-panel";

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: "Trade Terms",
      render: () => <FieldItemGrid itemScope={`${panelScope}.trade-terms`} items={TRADE_TERM_ITEMS} />,
    },
    {
      key:   "performance",
      label: "Performance",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Performance</div>
          <FieldItemGrid itemScope={`${panelScope}.performance`} items={PERF_ITEMS} />
        </>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Trade & Performance</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
