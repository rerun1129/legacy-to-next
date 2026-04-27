"use client";

import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { PackageField } from "@/components/shared/panel-fields";

interface Props { variant?: AnyVariantConfig }

const UNIT_SEL: React.CSSProperties = {
  height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

function LiField({ label, value, numeric }: { label: string; value: string; numeric?: boolean }) {
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

function GWField({ value = "30600" }: { value?: string }) {
  return (
    <div className="li">
      <span className="li__label">G/W</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input type="number" step="any" defaultValue={value.replace(/[^0-9.]/g, "")}
          style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }} />
        <select style={UNIT_SEL}><option>KGS</option><option>LBS</option></select>
      </div>
    </div>
  );
}

export function MasterCargoDocPanel({ variant }: Props) {
  if (!variant) return null;
  const isSea      = variant.mode === "SEA";
  const panelScope = `master-cargo-doc.${variant.key}`;

  const cargoBase: FieldItemDef[] = [
    { key: "main-item", render: () => <LiField label="Main Item" value="ELECTRONIC GOODS" /> },
    { key: "hs-code",   render: () => <LiField label="HS Code"   value="8517.13" /> },
    { key: "package",   render: () => <PackageField qty="1300" unit="CTN" height={24} /> },
    { key: "gw",        render: () => <GWField value="30600" /> },
    { key: "cbm",       render: () => <LiField label="CBM"       value="87.5"  numeric /> },
  ];
  const cargoExtras: FieldItemDef[] = isSea
    ? [{ key: "r-ton", render: () => <LiField label="R/Ton" value="" numeric /> }]
    : [
        { key: "vol-wt",     render: () => <LiField label="Volume W/T" value="14583" numeric /> },
        { key: "charge-wt",  render: () => <LiField label="Charge W/T" value="30600" numeric /> },
        { key: "rate-class", render: () => <LiField label="Rate Class" value="GCR" /> },
      ];
  const cargoItems = [...cargoBase, ...cargoExtras];

  const seaDoc: FieldItemDef[] = [
    { key: "settle",   render: () => <LiField label="Settle Partner" value="" /> },
    { key: "co-load",  render: () => <LiField label="Co-Load Agent"  value="" /> },
    { key: "operator", render: () => <LiField label="Operator"       value="KYS" /> },
    { key: "team",     render: () => <LiField label="Team"           value="SEA-EXP" /> },
  ];
  const airDocBase: FieldItemDef[] = [
    { key: "co-load-type",  render: () => <LiField label="Co-Load Type"  value="" /> },
    { key: "co-load-agent", render: () => <LiField label="Co-Load Agent" value="" /> },
    { key: "flight-type",   render: () => <LiField label="Flight Type"   value="Passenger" /> },
  ];
  const airDocSec: FieldItemDef[] = variant.direction === "EXP"
    ? [{ key: "security", render: () => <LiField label="Security Status" value="SPX" /> }]
    : [];
  const airDocTail: FieldItemDef[] = [
    { key: "settle",   render: () => <LiField label="Settle Partner" value="" /> },
    { key: "operator", render: () => <LiField label="Operator"       value="KYS" /> },
    { key: "team",     render: () => <LiField label="Team"           value="AIR-EXP" /> },
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
