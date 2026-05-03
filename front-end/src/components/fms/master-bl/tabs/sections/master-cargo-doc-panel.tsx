"use client";

import type { UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

const UNIT_SEL: React.CSSProperties = {
  height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

// form 없을 때 fallback용 uncontrolled 입력
function LiFieldStatic({ label, value, numeric }: { label: string; value: string; numeric?: boolean }) {
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input">
        {numeric
          ? <input type="number" step="any" defaultValue={value.replace(/,/g, "")}
              style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} />
          : <input defaultValue={value} style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} />
        }
      </div>
    </div>
  );
}

export function MasterCargoDocPanel({ variant, form }: Props) {
  if (!variant) return null;
  const isSea      = variant.mode === "SEA";
  const panelScope = `master-cargo-doc.${variant.key}`;

  const cargoBase: FieldItemDef[] = form ? [
    {
      key: "main-item",
      render: () => (
        <div className="li">
          <span className="li__label">Main Item</span>
          <div className="li__input">
            <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
              {...form.register("mainItemName")} />
          </div>
        </div>
      ),
    },
    {
      key: "hs-code",
      render: () => (
        <div className="li">
          <span className="li__label">HS Code</span>
          <div className="li__input">
            <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
              {...form.register("hsCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input" style={{ display: "flex", gap: 4 }}>
            <input type="number" step="1"
              style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }}
              {...form.register("pkgQty", { valueAsNumber: true })} />
            <select style={UNIT_SEL} {...form.register("pkgUnit")}>
              <option value="CTN">CTN</option>
              <option value="PKG">PKG</option>
              <option value="BAG">BAG</option>
              <option value="PLT">PLT</option>
              <option value="BOX">BOX</option>
              <option value="PCS">PCS</option>
              <option value="ROL">ROL</option>
            </select>
          </div>
        </div>
      ),
    },
    {
      key: "gw",
      render: () => (
        <div className="li">
          <span className="li__label">G/W</span>
          <div className="li__input" style={{ display: "flex", gap: 4 }}>
            <input type="number" step="any"
              style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }}
              {...form.register("grossWeightKg", { valueAsNumber: true })} />
            <select style={UNIT_SEL}><option>KGS</option><option>LBS</option></select>
          </div>
        </div>
      ),
    },
    {
      key: "cbm",
      render: () => (
        <div className="li">
          <span className="li__label">CBM</span>
          <div className="li__input">
            <input type="number" step="any"
              style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
              {...form.register("cbm", { valueAsNumber: true })} />
          </div>
        </div>
      ),
    },
  ] : [
    { key: "main-item", render: () => <LiFieldStatic label="Main Item" value="" /> },
    { key: "hs-code",   render: () => <LiFieldStatic label="HS Code"   value="" /> },
    { key: "package",   render: () => (
      <div className="li">
        <span className="li__label">Package</span>
        <div className="li__input" style={{ display: "flex", gap: 4 }}>
          <input type="number" step="1" defaultValue="" style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }} />
          <select style={UNIT_SEL}><option>CTN</option><option>PKG</option></select>
        </div>
      </div>
    )},
    { key: "gw",  render: () => (
      <div className="li">
        <span className="li__label">G/W</span>
        <div className="li__input" style={{ display: "flex", gap: 4 }}>
          <input type="number" step="any" defaultValue="" style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }} />
          <select style={UNIT_SEL}><option>KGS</option><option>LBS</option></select>
        </div>
      </div>
    )},
    { key: "cbm", render: () => <LiFieldStatic label="CBM" value=""  numeric /> },
  ];

  const cargoExtras: FieldItemDef[] = isSea
    ? [{ key: "r-ton", render: () => form
        ? <div className="li">
            <span className="li__label">R/Ton</span>
            <div className="li__input">
              <input type="number" step="any"
                style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
                {...form.register("seaDetail.rton", { valueAsNumber: true })} />
            </div>
          </div>
        : <LiFieldStatic label="R/Ton" value="" numeric />
    }]
    : [
        { key: "vol-wt",     render: () => <LiFieldStatic label="Volume W/T" value="" numeric /> },
        { key: "charge-wt",  render: () => <LiFieldStatic label="Charge W/T" value="" numeric /> },
        { key: "rate-class", render: () => <LiFieldStatic label="Rate Class" value="GCR" /> },
      ];

  const cargoItems = [...cargoBase, ...cargoExtras];

  const seaDoc: FieldItemDef[] = form ? [
    { key: "settle", render: () => (
      <div className="li">
        <span className="li__label">Settle Partner</span>
        <div className="li__input">
          <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
            {...form.register("settlePartnerCode")} />
        </div>
      </div>
    )},
    { key: "operator", render: () => (
      <div className="li">
        <span className="li__label">Operator</span>
        <div className="li__input">
          <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
            {...form.register("operatorCode")} />
        </div>
      </div>
    )},
    { key: "co-load",  render: () => <LiFieldStatic label="Co-Load Agent" value="" /> },
    { key: "team",     render: () => <LiFieldStatic label="Team"          value="" /> },
  ] : [
    { key: "settle",   render: () => <LiFieldStatic label="Settle Partner" value="" /> },
    { key: "co-load",  render: () => <LiFieldStatic label="Co-Load Agent"  value="" /> },
    { key: "operator", render: () => <LiFieldStatic label="Operator"       value="" /> },
    { key: "team",     render: () => <LiFieldStatic label="Team"           value="" /> },
  ];

  const airDocBase: FieldItemDef[] = [
    { key: "co-load-type",  render: () => <LiFieldStatic label="Co-Load Type"  value="" /> },
    { key: "co-load-agent", render: () => <LiFieldStatic label="Co-Load Agent" value="" /> },
    { key: "flight-type",   render: () => <LiFieldStatic label="Flight Type"   value="Passenger" /> },
  ];
  const airDocSec: FieldItemDef[] = variant.direction === "EXP"
    ? [{ key: "security", render: () => <LiFieldStatic label="Security Status" value="SPX" /> }]
    : [];
  const airDocTail: FieldItemDef[] = form ? [
    { key: "settle",   render: () => (
      <div className="li">
        <span className="li__label">Settle Partner</span>
        <div className="li__input">
          <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
            {...form.register("settlePartnerCode")} />
        </div>
      </div>
    )},
    { key: "operator", render: () => (
      <div className="li">
        <span className="li__label">Operator</span>
        <div className="li__input">
          <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }}
            {...form.register("operatorCode")} />
        </div>
      </div>
    )},
    { key: "team", render: () => <LiFieldStatic label="Team" value="" /> },
  ] : [
    { key: "settle",   render: () => <LiFieldStatic label="Settle Partner" value="" /> },
    { key: "operator", render: () => <LiFieldStatic label="Operator"       value="" /> },
    { key: "team",     render: () => <LiFieldStatic label="Team"           value="" /> },
  ];

  const docItems = isSea ? seaDoc : [...airDocBase, ...airDocSec, ...airDocTail];

  const fields: FieldWidgetDef[] = [
    {
      key: "cargo", label: "Cargo",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Cargo</div>
          <FieldItemGrid itemScope={`${panelScope}.cargo`} items={cargoItems} />
        </>
      ),
    },
    {
      key: "document", label: "Document",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Document</div>
          <FieldItemGrid itemScope={`${panelScope}.document`} items={docItems} />
        </>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo &amp; Document</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
