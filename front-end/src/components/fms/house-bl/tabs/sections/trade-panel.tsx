import type React from "react";
import { useFormContext } from "react-hook-form";
import { Search } from "lucide-react";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

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

// ── RHF-bound fields ────────────────────────────────────────
function PaymentTypeField({ inputProps }: { inputProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label is-required">Freight Term</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...inputProps} />
      </div>
    </div>
  );
}

function PaymentPlaceField({ inputProps }: { inputProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label">Payable At</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...inputProps} />
      </div>
    </div>
  );
}

const PERF_ITEMS: FieldItemDef[] = [
  { key: "customer", render: () => <LcnField label="Actual Customer" code="" name="" /> },
  { key: "sales",    render: () => <LcnField label="Sales Man"       code="" name="" /> },
  { key: "operator", render: () => <LcnField label="Operator"        code="" name="" /> },
  { key: "team",     render: () => <LcnField label="Team"            code="" name="" /> },
];

export function TradePanel({ variant }: { variant?: AnyVariantConfig }) {
  const { register } = useFormContext<HouseBlFormValues>();
  const panelScope = variant ? `trade-panel.${variant.key}` : "trade-panel";

  const tradeTermItems: FieldItemDef[] = [
    { key: "incoterms",    render: () => <LiField label="Incoterms"    value="FOB"     req /> },
    { key: "freight-term", render: () => <PaymentTypeField inputProps={{ ...register("paymentType"),  defaultValue: "Prepaid" }} /> },
    { key: "payable-at",   render: () => <PaymentPlaceField inputProps={{ ...register("paymentPlace"), defaultValue: "ORIGIN" }} /> },
    { key: "co-load",      render: () => <LiField label="Co-Load"      value="N" /> },
  ];

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: "Trade Terms",
      render: () => <FieldItemGrid itemScope={`${panelScope}.trade-terms`} items={tradeTermItems} />,
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
