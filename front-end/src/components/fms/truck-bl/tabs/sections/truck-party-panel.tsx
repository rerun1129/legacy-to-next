"use client";

import { useFormContext, Controller } from "react-hook-form";
import type { Path } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { CodeBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

type PartyRole = "SHIPPER" | "CONSIGNEE" | "NOTIFY" | "DOC PARTNER";

type PartyDef = {
  key:     string;
  role:    PartyRole;
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

/** role → autocomplete 소스 매핑 */
const ROLE_SOURCE: Record<PartyRole, typeof CODE_SOURCES.customer | typeof CODE_SOURCES.partner> = {
  "SHIPPER":     CODE_SOURCES.customer,
  "CONSIGNEE":   CODE_SOURCES.customer,
  "NOTIFY":      CODE_SOURCES.customer,
  "DOC PARTNER": CODE_SOURCES.partner,
};

function TruckPartyBlock({ party }: { party: PartyDef }) {
  const { register, control, setValue } = useFormContext<TruckBlFormValues>();
  const src = useCodeAutocomplete(ROLE_SOURCE[party.role]);
  return (
    <>
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        {/* CodeBox party-cn이 자체적으로 .party-block + .party-block__head + 라벨 span을 렌더 */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="party-cn"
            variant="panel"
            label={party.role}
            codeProps={{ ...register(party.codeKey) }}
            nameProps={{ ...register(party.nameKey) }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
            onSearch={src.onSearch}
            suggestions={src.suggestions}
            suggestionsLoading={src.suggestionsLoading}
            onSelect={(it) => { setValue(party.codeKey, it.code); setValue(party.nameKey, it.name); setValue(party.addrKey, it.address ?? ""); }}
          />
        </div>
        {party.btn && (
          <button type="button" className="party-block__head-btn">{party.btn}</button>
        )}
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
            style={{ height: 100, marginTop: 4 }}
          />
        )}
      />
    </>
  );
}

export function TruckPartyPanel() {
  const fields: FieldWidgetDef[] = TRUCK_PARTIES.map(p => ({
    key:   p.key,
    label: p.role,
    render: () => <TruckPartyBlock party={p} />,
  }));

  return (
    <div className="panel truck-party-panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="truck-party-panel" fields={fields} />
      </div>
    </div>
  );
}
