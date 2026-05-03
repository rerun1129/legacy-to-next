"use client";

import { useFormContext } from "react-hook-form";
import { Search } from "lucide-react";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

interface PartyBlockProps {
  role:       string;
  req:        boolean;
  codeProps:  React.InputHTMLAttributes<HTMLInputElement>;
  nameProps:  React.InputHTMLAttributes<HTMLInputElement>;
}

function PartyBlock({ role, req, codeProps, nameProps }: PartyBlockProps) {
  return (
    <div className="party-block" style={{ paddingBottom: 8 }}>
      <div className="party-block__head">
        <span className={req ? "is-required" : undefined} style={{ fontSize: 11, minWidth: 120, flexShrink: 0 }}>
          {role}
        </span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input placeholder="Code" {...codeProps} />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" placeholder="Company Name" {...nameProps} />
        </div>
      </div>
    </div>
  );
}

export function NonBLPartyPanel() {
  const { register } = useFormContext<NonBlFormValues>();

  const fields: FieldWidgetDef[] = [
    {
      key:    "actual-customer",
      label:  "ACTUAL CUSTOMER",
      render: () => (
        <PartyBlock
          role="ACTUAL CUSTOMER"
          req={true}
          codeProps={register("actualCustomerCode")}
          nameProps={register("actualCustomerName")}
        />
      ),
    },
    {
      key:    "shipper",
      label:  "SHIPPER",
      render: () => (
        <PartyBlock
          role="SHIPPER"
          req={false}
          codeProps={register("shipperCode")}
          nameProps={register("shipperName")}
        />
      ),
    },
    {
      key:    "consignee",
      label:  "CONSIGNEE",
      render: () => (
        <PartyBlock
          role="CONSIGNEE"
          req={false}
          codeProps={register("consigneeCode")}
          nameProps={register("consigneeName")}
        />
      ),
    },
    {
      key:    "notify",
      label:  "NOTIFY",
      render: () => (
        <PartyBlock
          role="NOTIFY"
          req={false}
          codeProps={register("notifyCode")}
          nameProps={register("notifyName")}
        />
      ),
    },
    {
      key:    "settle-partner",
      label:  "SETTLE PARTNER",
      render: () => (
        <PartyBlock
          role="SETTLE PARTNER"
          req={false}
          codeProps={register("settlePartnerCode")}
          nameProps={register("settlePartnerName")}
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
