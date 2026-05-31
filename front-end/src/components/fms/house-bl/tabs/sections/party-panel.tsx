"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { CodeBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

interface Props { variant?: AnyVariantConfig; isExp?: boolean }

type PartyRole = "SHIPPER" | "CONSIGNEE" | "NOTIFY" | "DOC PARTNER";

interface PartyConfig {
  key:      string;
  role:     PartyRole;
  label:    string;   // localised display label
  required: boolean;
}

/** role → (codeField, nameField, addrField) 매핑 */
const ROLE_FIELDS: Record<
  PartyRole,
  [keyof HouseBlFormValues, keyof HouseBlFormValues, keyof HouseBlFormValues]
> = {
  "SHIPPER":     ["shipperCode",    "shipperName",    "shipperAddress"],
  "CONSIGNEE":   ["consigneeCode",  "consigneeName",  "consigneeAddress"],
  "NOTIFY":      ["notifyCode",     "notifyName",     "notifyAddress"],
  "DOC PARTNER": ["docPartnerCode", "docPartnerName", "docPartnerAddress"],
};

/** role → autocomplete 소스 매핑 */
const ROLE_SOURCE = {
  "SHIPPER":     CODE_SOURCES.customer,
  "CONSIGNEE":   CODE_SOURCES.customer,
  "NOTIFY":      CODE_SOURCES.customer,
  "DOC PARTNER": CODE_SOURCES.partner,
} satisfies Record<PartyRole, typeof CODE_SOURCES.customer | typeof CODE_SOURCES.partner>;

function PartyBlock({ cfg, isExp, toOrderLabel, sameAsCneLabel, addrPlaceholder }: {
  cfg: PartyConfig;
  isExp: boolean;
  toOrderLabel: string;
  sameAsCneLabel: string;
  addrPlaceholder: string;
}) {
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();
  const isRequired = cfg.role === "CONSIGNEE" ? !isExp : cfg.required;
  const [codeField, nameField, addrField] = ROLE_FIELDS[cfg.role];
  const src = useCodeAutocomplete(ROLE_SOURCE[cfg.role]);

  return (
    <>
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="party-cn"
            variant="panel"
            label={cfg.label}
            required={isRequired}
            codeProps={{ ...register(codeField) }}
            nameProps={{ ...register(nameField) }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
            onSearch={src.onSearch}
            suggestions={src.suggestions}
            suggestionsLoading={src.suggestionsLoading}
            onSelect={(it) => { setValue(codeField, it.code); setValue(nameField, it.name); setValue(addrField, it.address ?? ""); }}
          />
        </div>
        {cfg.role === "CONSIGNEE" && (
          <button type="button" className="party-block__head-btn">{toOrderLabel}</button>
        )}
        {cfg.role === "NOTIFY" && (
          <button type="button" className="party-block__head-btn">{sameAsCneLabel}</button>
        )}
      </div>
      <Controller
        control={control}
        name={addrField}
        render={({ field }) => (
          <LineNumberTextarea
            name={field.name}
            value={field.value as string ?? ""}
            onChange={field.onChange}
            onBlur={field.onBlur}
            placeholder={addrPlaceholder}
            style={{ height: 100, marginTop: 4 }}
          />
        )}
      />
    </>
  );
}

export function PartyPanel({ variant, isExp = false }: Props) {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const panelScope = variant ? `party-panel.${variant.key}` : "party-panel";

  const PARTY_CONFIGS: PartyConfig[] = [
    { key: "shipper",     role: "SHIPPER",     label: tf("shipper"),    required: false },
    { key: "consignee",   role: "CONSIGNEE",   label: tf("consignee"),  required: !isExp },
    { key: "notify",      role: "NOTIFY",      label: tf("notify"),     required: false  },
    { key: "doc-partner", role: "DOC PARTNER", label: tf("docPartner"), required: true   },
  ];

  const toOrderLabel    = tf("toOrder");
  const sameAsCneLabel  = tf("sameAsCne");
  const addrPlaceholder = tf("addressPlaceholder");

  const fields: FieldWidgetDef[] = PARTY_CONFIGS.map(cfg => ({
    key:   cfg.key,
    label: cfg.label,
    render: () => (
      <PartyBlock
        cfg={cfg}
        isExp={isExp}
        toOrderLabel={toOrderLabel}
        sameAsCneLabel={sameAsCneLabel}
        addrPlaceholder={addrPlaceholder}
      />
    ),
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("party")}</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
