"use client";

import { Search } from "lucide-react";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";

const NON_PARTIES = [
  { key: "actual-customer", role: "ACTUAL CUSTOMER", req: true,  btn: null           },
  { key: "shipper",         role: "SHIPPER",         req: false, btn: null           },
  { key: "consignee",       role: "CONSIGNEE",       req: false, btn: "To Order"     },
  { key: "notify",          role: "NOTIFY",          req: false, btn: "Same as Cne." },
  { key: "sales-partner",   role: "Sales Partner",   req: false, btn: null           },
] as const;

function PartyBlock({ party }: { party: typeof NON_PARTIES[number] }) {
  return (
    <div className="party-block" style={{ paddingBottom: 8 }}>
      <div className="party-block__head">
        <span className={party.req ? "is-required" : undefined} style={{ fontSize: 11, minWidth: 120, flexShrink: 0 }}>
          {party.role}
        </span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" placeholder="Company Name" />
        </div>
        {party.btn && (
          <div className="party-block__head-actions">
            <button className="party-block__head-btn">{party.btn}</button>
          </div>
        )}
      </div>
    </div>
  );
}

export function NonBLPartyPanel() {
  const fields: FieldWidgetDef[] = NON_PARTIES.map(p => ({
    key:    p.key,
    label:  p.role,
    render: () => <PartyBlock party={p} />,
  }));

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
