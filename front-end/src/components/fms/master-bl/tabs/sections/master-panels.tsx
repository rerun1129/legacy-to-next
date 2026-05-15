"use client";

import { Controller, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { CodeBox }            from "@/components/shared/inputs/code-box";
import { ComboBox }           from "@/components/shared/inputs/combo-box";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { useEnumOptions } from "@/application/enums/use-enum";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

// ── Party ──────────────────────────────────────────────────────────────────
const PARTIES = ["SHIPPER", "CONSIGNEE", "NOTIFY"] as const;
type PartyRole = typeof PARTIES[number];

const PARTY_CODE_FIELD: Record<PartyRole, "shipperCode" | "consigneeCode" | "notifyCode"> = {
  SHIPPER:   "shipperCode",
  CONSIGNEE: "consigneeCode",
  NOTIFY:    "notifyCode",
};

const PARTY_ADDR_FIELD: Record<PartyRole, "shipperAddress" | "consigneeAddress" | "notifyAddress"> = {
  SHIPPER:   "shipperAddress",
  CONSIGNEE: "consigneeAddress",
  NOTIFY:    "notifyAddress",
};

const PARTY_BTNS: Partial<Record<PartyRole, string>> = {
  CONSIGNEE: "To Order",
  NOTIFY:    "Same as Cne.",
};

function PartyBlockConnected({
  role,
  form,
}: {
  role: PartyRole;
  form: UseFormReturn<MasterBlFormValues>;
}) {
  const codeField = PARTY_CODE_FIELD[role];
  const addrField = PARTY_ADDR_FIELD[role];

  return (
    <div className="party-block">
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="party-cn"
            variant="panel"
            label={role}
            codeProps={{ ...form.register(codeField) }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          />
        </div>
        {PARTY_BTNS[role] && (
          <button type="button" className="party-block__head-btn">{PARTY_BTNS[role]}</button>
        )}
      </div>
      <Controller
        control={form.control}
        name={addrField}
        render={({ field }) => (
          <LineNumberTextarea
            placeholder="Address (free text)"
            style={{ height: 108 }}
            value={field.value ?? ""}
            onChange={field.onChange}
            onBlur={field.onBlur}
          />
        )}
      />
    </div>
  );
}

function PartyBlockStub({ role }: { role: PartyRole }) {
  return (
    <div className="party-block">
      <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CodeBox
            kind="party-cn"
            variant="panel"
            label={role}
            codeProps={{}}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          />
        </div>
        {PARTY_BTNS[role] && (
          <button type="button" className="party-block__head-btn">{PARTY_BTNS[role]}</button>
        )}
      </div>
      <LineNumberTextarea placeholder="Address (free text)" style={{ height: 108 }} />
    </div>
  );
}

export function MasterPartyPanel({ form }: Props) {
  const fields: FieldWidgetDef[] = PARTIES.map(role => ({
    key:    role.toLowerCase(),
    label:  role,
    render: () => form
      ? <PartyBlockConnected role={role} form={form} />
      : <PartyBlockStub role={role} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Party</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="master-party-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Marks ──────────────────────────────────────────────────────────────────
export function MasterMarksPanel({ form }: Props) {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Marks &amp; Numbers</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        {form
          ? <Controller
              control={form.control}
              name="desc.marks"
              render={({ field }) => (
                <LineNumberTextarea
                  style={{ flex: 1, minHeight: 0 }}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          : <LineNumberTextarea defaultValue="" style={{ flex: 1, minHeight: 0 }} />
        }
      </div>
    </div>
  );
}

// ── Goods Description ──────────────────────────────────────────────────────
export function MasterGoodsDescPanel({ variant, form }: Props) {
  const { options: clause1Options, placeholder: clause1Placeholder } = useEnumOptions("DescClause1");
  const { options: clause2Options, placeholder: clause2Placeholder } = useEnumOptions("DescClause2");

  if (!variant) return null;
  const isSea = variant.mode === "SEA";
  const title = isSea ? "Description of Goods" : "Nature of Goods";

  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{title}</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        {isSea && (
          <div style={{ display: "flex", gap: 4, marginBottom: 8, flexShrink: 0 }}>
            {form
              ? <>
                  <Controller name="desc.descClause1" control={form.control} render={({ field }) => (
                    <ComboBox variant="panel" options={clause1Options} placeholder={clause1Placeholder ?? "-- 부지약관 --"} value={field.value ?? ""} onChange={field.onChange} onBlur={field.onBlur} style={{ flex: 1 }} />
                  )} />
                  <Controller name="desc.descClause2" control={form.control} render={({ field }) => (
                    <ComboBox variant="panel" options={clause2Options} placeholder={clause2Placeholder ?? "-- 부지약관 --"} value={field.value ?? ""} onChange={field.onChange} onBlur={field.onBlur} style={{ flex: 1 }} />
                  )} />
                </>
              : <>
                  <ComboBox variant="panel" options={clause1Options} placeholder={clause1Placeholder ?? "-- 부지약관 --"} style={{ flex: 1 }} />
                  <ComboBox variant="panel" options={clause2Options} placeholder={clause2Placeholder ?? "-- 부지약관 --"} style={{ flex: 1 }} />
                </>
            }
          </div>
        )}
        {form
          ? <Controller
              control={form.control}
              name="desc.description"
              render={({ field }) => (
                <LineNumberTextarea
                  style={{ flex: 1, minHeight: 0 }}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          : <LineNumberTextarea defaultValue="" style={{ flex: 1, minHeight: 0 }} />
        }
      </div>
    </div>
  );
}

// ── Remark ─────────────────────────────────────────────────────────────────
export function MasterRemarkPanel({ form }: Props) {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Remark</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        {form
          ? <Controller
              control={form.control}
              name="desc.remark"
              render={({ field }) => (
                <LineNumberTextarea
                  style={{ flex: 1, minHeight: 0 }}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          : <LineNumberTextarea style={{ flex: 1, minHeight: 0 }} />
        }
      </div>
    </div>
  );
}
