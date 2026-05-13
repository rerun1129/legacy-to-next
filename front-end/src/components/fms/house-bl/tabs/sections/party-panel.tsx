import { useFormContext, Controller } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { CodeBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

interface Props { variant?: AnyVariantConfig; isExp?: boolean }

type PartyRole = "SHIPPER" | "CONSIGNEE" | "NOTIFY" | "DOC PARTNER";

interface PartyConfig {
  key:      string;
  role:     PartyRole;
  required: boolean;
}

/** role → (codeField, nameField, addrField) 매핑 */
const ROLE_FIELDS: Record<
  PartyRole,
  [keyof HouseBlFormValues, keyof HouseBlFormValues, keyof HouseBlFormValues]
> = {
  "SHIPPER":     ["shipperCode",    "shipperName",    "shipperAddr"],
  "CONSIGNEE":   ["consigneeCode",  "consigneeName",  "consigneeAddr"],
  "NOTIFY":      ["notifyCode",     "notifyName",     "notifyAddr"],
  "DOC PARTNER": ["docPartnerCode", "docPartnerName", "docPartnerAddress"],
};

function PartyBlock({ cfg, isExp }: { cfg: PartyConfig; isExp: boolean }) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const isRequired = cfg.role === "CONSIGNEE" ? !isExp : cfg.required;
  const [codeField, nameField, addrField] = ROLE_FIELDS[cfg.role];

  return (
    <>
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="party-cn"
            variant="panel"
            label={cfg.role}
            required={isRequired}
            codeProps={{ ...register(codeField) }}
            nameProps={{ ...register(nameField) }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          />
        </div>
        {cfg.role === "CONSIGNEE" && (
          <button type="button" className="party-block__head-btn">To Order</button>
        )}
        {cfg.role === "NOTIFY" && (
          <button type="button" className="party-block__head-btn">Same as Cne.</button>
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
            placeholder="Address (free text)"
            style={{ height: 100, marginTop: 4 }}
          />
        )}
      />
    </>
  );
}

export function PartyPanel({ variant, isExp = false }: Props) {
  const panelScope = variant ? `party-panel.${variant.key}` : "party-panel";
  const PARTY_CONFIGS: PartyConfig[] = [
    { key: "shipper",     role: "SHIPPER",     required: false },
    { key: "consignee",   role: "CONSIGNEE",   required: !isExp },
    { key: "notify",      role: "NOTIFY",      required: false  },
    { key: "doc-partner", role: "DOC PARTNER", required: true   },
  ];

  const fields: FieldWidgetDef[] = PARTY_CONFIGS.map(cfg => ({
    key:   cfg.key,
    label: cfg.role,
    render: () => <PartyBlock cfg={cfg} isExp={isExp} />,
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
