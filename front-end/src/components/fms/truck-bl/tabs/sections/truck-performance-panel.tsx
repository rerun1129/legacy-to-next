"use client";

import { useFormContext } from "react-hook-form";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import { CodeBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

export function TruckPerformancePanel() {
  const { register } = useFormContext<TruckBlFormValues>();

  const PERF_ITEMS: FieldItemDef[] = [
    {
      key: "actual-customer",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Actual Customer"
          required
          codeProps={{ ...register("actualCustomerCode") }}
          nameProps={{ ...register("actualCustomerName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
    {
      key: "settle-partner",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Settle Partner"
          codeProps={{ ...register("settlePartnerCode") }}
          nameProps={{ ...register("settlePartnerName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
    {
      key: "sales-man",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Sales Man"
          required
          codeProps={{ ...register("salesManCode") }}
          nameProps={{ ...register("salesManName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
    {
      key: "operator",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Operator"
          required
          codeProps={{ ...register("operatorCode") }}
          nameProps={{ ...register("operatorName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
    {
      key: "team",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Team"
          required
          codeProps={{ ...register("teamCode") }}
          nameProps={{ ...register("teamName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
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
