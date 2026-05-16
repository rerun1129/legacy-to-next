"use client";

import { useFormContext, Controller } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { CodeBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props { variant?: AnyVariantConfig }

type PartyRole = "SHIPPER" | "CONSIGNEE" | "NOTIFY";

const ROLE_CODE_FIELD: Record<PartyRole, "shipperCode" | "consigneeCode" | "notifyCode"> = {
  SHIPPER:   "shipperCode",
  CONSIGNEE: "consigneeCode",
  NOTIFY:    "notifyCode",
};

const ROLE_ADDR_FIELD: Record<PartyRole, "shipperAddress" | "consigneeAddress" | "notifyAddress"> = {
  SHIPPER:   "shipperAddress",
  CONSIGNEE: "consigneeAddress",
  NOTIFY:    "notifyAddress",
};

function PartyBlock({ role, isImp }: { role: PartyRole; isImp: boolean }) {
  const { register, control } = useFormContext<MasterBlFormValues>();
  const codeField = ROLE_CODE_FIELD[role];
  const addrField = ROLE_ADDR_FIELD[role];
  // CONSIGNEE는 IMP에서 required (BE SSOT — HTML5/zodResolver required 금지)
  const isRequired = role === "CONSIGNEE" ? isImp : false;

  return (
    <>
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="party-cn"
            variant="panel"
            label={role}
            required={isRequired}
            codeProps={{ ...register(codeField) }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          />
        </div>
        {role === "CONSIGNEE" && (
          <button type="button" className="party-block__head-btn">To Order</button>
        )}
        {role === "NOTIFY" && (
          <button type="button" className="party-block__head-btn">Same as Cne.</button>
        )}
      </div>
      <Controller
        control={control}
        name={addrField}
        render={({ field }) => (
          <LineNumberTextarea
            name={field.name}
            value={field.value ?? ""}
            onChange={field.onChange}
            onBlur={field.onBlur}
            placeholder="Address (free text)"
            style={{ height: 100, marginTop: 4 }}
          />
        )}
      />
    </>
  );
}

// DOC PARTNER 제외 — 3슬롯(SHIPPER/CONSIGNEE/NOTIFY)
export function MasterAirPartyPanel({ variant }: Props) {
  const panelScope = variant ? `master-air-party-panel.${variant.key}` : "master-air-party-panel";
  const isImp      = variant?.direction === "IMP";

  const fields: FieldWidgetDef[] = (["SHIPPER", "CONSIGNEE", "NOTIFY"] as const).map(role => ({
    key:    role.toLowerCase(),
    label:  role,
    render: () => <PartyBlock role={role} isImp={isImp} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Party</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
