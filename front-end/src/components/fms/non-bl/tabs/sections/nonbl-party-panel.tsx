"use client";

import { useFormContext } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLPartyPanel() {
  const { register } = useFormContext<NonBlFormValues>();

  const fields: FieldWidgetDef[] = [
    {
      key:    "actual-customer",
      label:  "ACTUAL CUSTOMER",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="ACTUAL CUSTOMER"
          required
          codeProps={{ ...register("actualCustomerCode") }}
          nameProps={{ ...register("actualCustomerName") }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key:    "shipper",
      label:  "SHIPPER",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="SHIPPER"
          codeProps={{ ...register("shipperCode") }}
          nameProps={{ ...register("shipperName") }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key:    "consignee",
      label:  "CONSIGNEE",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="CONSIGNEE"
          codeProps={{ ...register("consigneeCode") }}
          nameProps={{ ...register("consigneeName") }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key:    "notify",
      label:  "NOTIFY",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="NOTIFY"
          codeProps={{ ...register("notifyCode") }}
          nameProps={{ ...register("notifyName") }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key:    "settle-partner",
      label:  "SETTLE PARTNER",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="SETTLE PARTNER"
          codeProps={{ ...register("settlePartnerCode") }}
          nameProps={{ ...register("settlePartnerName") }}
          onLookup={() => {}}
        />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Party</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="nonbl-party-panel" fields={fields} />
      </div>
    </div>
  );
}
