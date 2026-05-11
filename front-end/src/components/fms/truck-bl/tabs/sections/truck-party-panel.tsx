"use client";

import { useFormContext, Controller } from "react-hook-form";
import type { Path } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { CodeBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

type PartyDef = {
  key:     string;
  role:    string;
  btn:     string | null;
  codeKey: Path<TruckBlFormValues>;
  nameKey: Path<TruckBlFormValues>;
  addrKey: Path<TruckBlFormValues>;
};

const TRUCK_PARTIES: PartyDef[] = [
  { key: "shipper",     role: "SHIPPER",     btn: null,           codeKey: "shipperCode",    nameKey: "shipperName",    addrKey: "shipperAddr"        },
  { key: "consignee",   role: "CONSIGNEE",   btn: "To Order",     codeKey: "consigneeCode",  nameKey: "consigneeName",  addrKey: "consigneeAddr"      },
  { key: "notify",      role: "NOTIFY",      btn: "Same as Cne.", codeKey: "notifyCode",     nameKey: "notifyName",     addrKey: "notifyAddr"         },
  { key: "doc-partner", role: "DOC PARTNER", btn: null,           codeKey: "docPartnerCode", nameKey: "docPartnerName", addrKey: "docPartnerAddress"  },
];

function TruckPartyBlock({ party }: { party: PartyDef }) {
  const { register, control, setValue } = useFormContext<TruckBlFormValues>();
  return (
    <div className="party-block">
      <div className="party-block__head">
        <span style={{ fontSize: 11, color: "var(--ink)", minWidth: 90, flexShrink: 0 }}>{party.role}</span>
        <CodeBox
          kind="party-cn"
          label={party.role}
          codeProps={{ ...register(party.codeKey) }}
          nameProps={{ ...register(party.nameKey) }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
        <div className="party-block__head-actions">
          {party.btn && (
            <button type="button" className="party-block__head-btn">{party.btn}</button>
          )}
          <button
            type="button"
            className="party-block__head-btn"
            onClick={() => setValue(party.codeKey, "")}
          >
            Clear
          </button>
        </div>
      </div>
      <Controller
        control={control}
        name={party.addrKey}
        render={({ field }) => (
          <LineNumberTextarea
            value={field.value as string}
            onChange={field.onChange}
            onBlur={field.onBlur}
            name={field.name}
            placeholder="Address (free text)"
            style={{ height: 100 }}
          />
        )}
      />
    </div>
  );
}

export function TruckPartyPanel() {
  const fields: FieldWidgetDef[] = TRUCK_PARTIES.map(p => ({
    key:   p.key,
    label: p.role,
    render: () => <TruckPartyBlock party={p} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="truck-party-panel" fields={fields} />
      </div>
    </div>
  );
}
