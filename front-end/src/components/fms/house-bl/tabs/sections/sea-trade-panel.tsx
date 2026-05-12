"use client";

import type React from "react";
import { useFormContext, type FieldPath } from "react-hook-form";
import { Search } from "lucide-react";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

// ── 공통 헬퍼 ──────────────────────────────────────────────
function LiField({
  label,
  name,
  req,
}: {
  label: string;
  name?: FieldPath<HouseBlFormValues>;
  req?: boolean;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  const registerProps = name ? register(name) : {};
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...registerProps} />
      </div>
    </div>
  );
}

function LcnField({
  label,
  req,
  codeField,
  nameField,
}: {
  label: string;
  req?: boolean;
  codeField: FieldPath<HouseBlFormValues>;
  nameField: FieldPath<HouseBlFormValues>;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  return (
    <div className="lcn">
      <span className={`lcn__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register(codeField)} />
        <Search size={12} className="lcn__icon" />
      </div>
      <input className="lcn__name" placeholder="Name" {...register(nameField)} />
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

export function SeaTradePanel() {
  const { register } = useFormContext<HouseBlFormValues>();

  const tradeTermItems: FieldItemDef[] = [
    { key: "incoterms",    render: () => <LiField label="Incoterms"    name="incoterms" req /> },
    { key: "freight-term", render: () => <PaymentTypeField inputProps={register("paymentType")} /> },
    { key: "payable-at",   render: () => <PaymentPlaceField inputProps={register("paymentPlace")} /> },
    { key: "co-load",      render: () => <LiField label="Co-Load"      name="coLoad" /> },
  ];

  const perfItems: FieldItemDef[] = [
    {
      key: "customer",
      render: () => <LcnField label="Actual Customer" req codeField="actualCustomerCode" nameField="actualCustomerName" />,
    },
    {
      // settlePartnerName이 schema에 없음 — 마이그레이션 시 추가 예정. name input은 비제어로 둠.
      key: "settle-partner",
      render: () => (
        <div className="lcn">
          <span className="lcn__label">Settle Partner</span>
          <div className="lcn__code" style={{ position: "relative" }}>
            <input
              style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }}
              {...register("settlePartnerCode")}
            />
            <Search size={12} className="lcn__icon" />
          </div>
          <input className="lcn__name" placeholder="Name" />
        </div>
      ),
    },
  ];

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: "Trade Terms",
      render: () => <FieldItemGrid itemScope="sea-trade-panel.trade-terms" items={tradeTermItems} />,
    },
    {
      key:   "performance",
      label: "Performance",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Performance</div>
          <FieldItemGrid itemScope="sea-trade-panel.performance" items={perfItems} />
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
        <FieldWidgetList panelScope="sea-trade-panel" fields={fields} />
      </div>
    </div>
  );
}
