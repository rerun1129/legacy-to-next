"use client";

import { Search } from "lucide-react";
import { Controller, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

const PARTIES = ["SHIPPER", "CONSIGNEE", "NOTIFY"] as const;
type PartyRole = typeof PARTIES[number];

const PARTY_BTNS: Partial<Record<PartyRole, string>> = {
  CONSIGNEE: "To Order",
  NOTIFY:    "Same as Cne.",
};

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

function PartyBlock({ role, form }: { role: PartyRole; form?: UseFormReturn<MasterBlFormValues> }) {
  const codeField = PARTY_CODE_FIELD[role];
  const addrField = PARTY_ADDR_FIELD[role];

  return (
    <div className="party-block">
      <div className="party-block__head">
        <span style={{ fontSize: 11, minWidth: 90, flexShrink: 0 }}>{role}</span>
        <div className="party-cn">
          <div className="party-cn__code">
            {form
              ? <input className="text-mono" placeholder="Code" {...form.register(codeField)} />
              : <input className="text-mono" placeholder="Code" />
            }
            <Search size={12} className="party-cn__icon" />
          </div>
        </div>
        <div className="party-block__head-actions">
          {PARTY_BTNS[role] && <button type="button" className="party-block__head-btn">{PARTY_BTNS[role]}</button>}
          <button type="button" className="party-block__head-btn">Clear</button>
        </div>
      </div>
      {form
        ? <Controller
            control={form.control}
            name={addrField}
            render={({ field }) => (
              <LineNumberTextarea
                placeholder="Address (free text)"
                style={{ height: 108 }}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        : <LineNumberTextarea placeholder="Address (free text)" style={{ height: 108 }} />
      }
    </div>
  );
}

export function MasterPartyPanel({ form }: Props) {
  const fields: FieldWidgetDef[] = PARTIES.map(role => ({
    key:    role.toLowerCase(),
    label:  role,
    render: () => <PartyBlock role={role} form={form} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="master-party-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Marks ──────────────────────────────────────────────────
export function MasterMarksPanel({ form }: Props) {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Marks &amp; Numbers</span></div>
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
                />
              )}
            />
          : <LineNumberTextarea defaultValue="" style={{ flex: 1, minHeight: 0 }} />
        }
      </div>
    </div>
  );
}

// ── Goods Description ──────────────────────────────────────
export function MasterGoodsDescPanel({ variant, form }: Props) {
  if (!variant) return null;
  const isSea = variant.mode === "SEA";
  const title = isSea ? "Description of Goods" : "Nature of Goods";

  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{title}</span></div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        {isSea && (
          <div className="li" style={{ marginBottom: 8, flexShrink: 0 }}>
            <span className="li__label">Clause</span>
            <div className="li__input">
              {form
                ? <select style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...form.register("desc.descClause1")}>
                    <option value="">-- 부지약관 --</option>
                    <option value="SAID TO CONTAIN">SAID TO CONTAIN</option>
                  </select>
                : <select style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}>
                    <option>-- 부지약관 --</option>
                    <option>SAID TO CONTAIN</option>
                  </select>
              }
            </div>
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
                />
              )}
            />
          : <LineNumberTextarea
              defaultValue=""
              style={{ flex: 1, minHeight: 0 }}
            />
        }
      </div>
    </div>
  );
}

// ── Remark ─────────────────────────────────────────────────
export function MasterRemarkPanel({ form }: Props) {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Remark</span></div>
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
                />
              )}
            />
          : <LineNumberTextarea style={{ flex: 1, minHeight: 0 }} />
        }
      </div>
    </div>
  );
}
