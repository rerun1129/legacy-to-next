"use client";

import { useFormContext, Controller } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { CodeBox } from "@/components/shared/inputs";
import { Button } from "@/components/shared/button";
import type { SwitchBlFormValues } from "./switch-bl-modal";

type PartyRole = "SHIPPER" | "CONSIGNEE" | "NOTIFY";

interface PartyConfig {
  key: string;
  role: PartyRole;
  codeField: keyof SwitchBlFormValues;
  addrField: keyof SwitchBlFormValues;
}

const PARTY_CONFIGS: PartyConfig[] = [
  { key: "shipper",   role: "SHIPPER",   codeField: "shipperCode",   addrField: "shipperAddress"   },
  { key: "consignee", role: "CONSIGNEE", codeField: "consigneeCode", addrField: "consigneeAddress" },
  { key: "notify",    role: "NOTIFY",    codeField: "notifyCode",    addrField: "notifyAddress"    },
];

interface PartyBlockProps {
  cfg: PartyConfig;
  isExp: boolean;
}

function PartyBlock({ cfg, isExp }: PartyBlockProps) {
  const { register, control, getValues, setValue } = useFormContext<SwitchBlFormValues>();
  const isNotify = cfg.role === "NOTIFY";

  const isRequired =
    cfg.role === "SHIPPER"   ? isExp  :
    cfg.role === "CONSIGNEE" ? !isExp :
    false;

  function handleSameAsShipper() {
    setValue("notifyCode",    getValues("shipperCode"),    { shouldDirty: true });
    setValue("notifyAddress", getValues("shipperAddress"), { shouldDirty: true });
  }

  function handleSameAsConsignee() {
    setValue("notifyCode",    getValues("consigneeCode"),    { shouldDirty: true });
    setValue("notifyAddress", getValues("consigneeAddress"), { shouldDirty: true });
  }

  return (
    <div className="party-block">
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="code-only"
            variant="panel"
            label={cfg.role}
            required={isRequired}
            codeProps={{ ...register(cfg.codeField), placeholder: `${cfg.role} code` }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          />
        </div>
        {isNotify && (
          <div className="party-block__head-actions" style={{ display: "flex", gap: 4 }}>
            <Button type="button" size="sm" onClick={handleSameAsShipper}>
              Same as Shipper
            </Button>
            <Button type="button" size="sm" onClick={handleSameAsConsignee}>
              Same as Cne.
            </Button>
          </div>
        )}
      </div>
      <Controller
        control={control}
        name={cfg.addrField}
        render={({ field }) => (
          <LineNumberTextarea
            name={field.name}
            value={(field.value as string) ?? ""}
            onChange={field.onChange}
            onBlur={field.onBlur}
            placeholder="Address (free text)"
            style={{ height: 100, marginTop: 4 }}
          />
        )}
      />
    </div>
  );
}

interface SwitchBlPartyPanelProps {
  isExp: boolean;
}

export function SwitchBlPartyPanel({ isExp }: SwitchBlPartyPanelProps) {
  return (
    <div
      className="panel"
      style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden", minHeight: 0 }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Party</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        {PARTY_CONFIGS.map(cfg => (
          <PartyBlock key={cfg.key} cfg={cfg} isExp={isExp} />
        ))}
      </div>
    </div>
  );
}
