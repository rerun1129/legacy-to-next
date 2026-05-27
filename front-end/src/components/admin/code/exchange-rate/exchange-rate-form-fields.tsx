"use client";

import { useForm } from "react-hook-form";
import type { ExchangeRateFormValues } from "./exchange-rate-form-types";

interface Props {
  register: ReturnType<typeof useForm<ExchangeRateFormValues>>["register"];
  isEdit: boolean;
  isReadOnly: boolean;
}

export function ExchangeRateFormFields({ register, isEdit, isReadOnly }: Props) {
  const fromReg = register("fromCurrencyCode");
  const toReg = register("toCurrencyCode");

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="lcn">
        <span className="lcn__label">From Currency *</span>
        <input
          className="box-panel"
          placeholder="e.g. USD"
          maxLength={3}
          readOnly={isEdit || isReadOnly}
          style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
          {...fromReg}
          onChange={(e) => {
            e.target.value = e.target.value.toUpperCase();
            void fromReg.onChange(e);
          }}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">To Currency *</span>
        <input
          className="box-panel"
          placeholder="e.g. KRW"
          maxLength={3}
          readOnly={isEdit || isReadOnly}
          style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
          {...toReg}
          onChange={(e) => {
            e.target.value = e.target.value.toUpperCase();
            void toReg.onChange(e);
          }}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Exchange Date</span>
        <input
          className="box-panel"
          placeholder="YYYYMMDD (e.g. 20260527)"
          maxLength={8}
          readOnly={isEdit || isReadOnly}
          style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
          {...register("exchangeDate")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Cash Sell Rate</span>
        <input
          className="box-panel"
          type="number"
          step="0.000001"
          placeholder="0.000000"
          readOnly={isReadOnly}
          {...register("cashSellExchangeRate", { valueAsNumber: true })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Cash Buy Rate</span>
        <input
          className="box-panel"
          type="number"
          step="0.000001"
          placeholder="0.000000"
          readOnly={isReadOnly}
          {...register("cashBuyExchangeRate", { valueAsNumber: true })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Wire Send Rate</span>
        <input
          className="box-panel"
          type="number"
          step="0.000001"
          placeholder="0.000000"
          readOnly={isReadOnly}
          {...register("wireSendExchangeRate", { valueAsNumber: true })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Wire Receive Rate</span>
        <input
          className="box-panel"
          type="number"
          step="0.000001"
          placeholder="0.000000"
          readOnly={isReadOnly}
          {...register("wireReceiveExchangeRate", { valueAsNumber: true })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Standard Rate</span>
        <input
          className="box-panel"
          type="number"
          step="0.000001"
          placeholder="0.000000"
          readOnly={isReadOnly}
          {...register("standardExchangeRate", { valueAsNumber: true })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Name *</span>
        <input
          className="box-panel"
          placeholder="Name"
          readOnly={isReadOnly}
          {...register("name")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">English Name</span>
        <input
          className="box-panel"
          placeholder="English Name (optional)"
          readOnly={isReadOnly}
          {...register("nameEn")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Active</span>
        <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <input type="checkbox" disabled={isReadOnly} {...register("active")} />
          Active
        </label>
      </div>
    </div>
  );
}
