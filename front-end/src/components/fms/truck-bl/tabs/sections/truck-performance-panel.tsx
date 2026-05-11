"use client";

import { useFormContext } from "react-hook-form";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import { TextBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

export function TruckPerformancePanel() {
  const { register } = useFormContext<TruckBlFormValues>();

  const PERF_ITEMS: FieldItemDef[] = [
    {
      key: "actual-customer",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Actual Customer</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Actual Customer" {...register("actualCustomerCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "customer-pic",
      render: () => (
        <div className="li">
          <span className="li__label">Customer PIC</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Customer PIC" {...register("customerPic")} />
          </div>
        </div>
      ),
    },
    {
      key: "settle-partner",
      render: () => (
        <div className="li">
          <span className="li__label">Settle Partner</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Settle Partner" {...register("settlePartnerCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "sales-man",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Sales Man</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Sales Man" {...register("salesManCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "operator",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Operator</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Operator" {...register("operatorCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "team",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Team</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Team" {...register("teamCode")} />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="truck-performance-panel" items={PERF_ITEMS} />
      </div>
    </div>
  );
}
