"use client";

import type { MasterVariantConfig } from "@/lib/bl-variants";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

interface Props { variant: MasterVariantConfig }

function LiField({ label, value }: { label: string; value: string }) {
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input">
        <input defaultValue={value} style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} />
      </div>
    </div>
  );
}

export function MasterCargoDocPanel({ variant }: Props) {
  const isSea      = variant.mode === "SEA";
  const panelScope = `master-cargo-doc.${variant.key}`;

  const cargoBase: FieldItemDef[] = [
    { key: "main-item", render: () => <LiField label="Main Item" value="ELECTRONIC GOODS" /> },
    { key: "hs-code",   render: () => <LiField label="HS Code"   value="8517.13" /> },
    { key: "package",   render: () => <LiField label="Package"   value="1300 CTN" /> },
    { key: "gw",        render: () => <LiField label="G/W"       value="30,600 KGS" /> },
    { key: "cbm",       render: () => <LiField label="CBM"       value="87.5" /> },
  ];
  const cargoExtras: FieldItemDef[] = isSea
    ? [{ key: "r-ton", render: () => <LiField label="R/Ton" value="" /> }]
    : [
        { key: "vol-wt",    render: () => <LiField label="Volume W/T" value="14,583" /> },
        { key: "charge-wt", render: () => <LiField label="Charge W/T" value="30,600" /> },
        { key: "rate-class",render: () => <LiField label="Rate Class" value="GCR" /> },
      ];
  const cargoItems = [...cargoBase, ...cargoExtras];

  const seaDoc: FieldItemDef[] = [
    { key: "settle",    render: () => <LiField label="Settle Partner" value="" /> },
    { key: "co-load",   render: () => <LiField label="Co-Load Agent"  value="" /> },
    { key: "operator",  render: () => <LiField label="Operator"       value="KYS" /> },
    { key: "team",      render: () => <LiField label="Team"           value="SEA-EXP" /> },
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
          <div style={{ marginTop: 8 }}><button className="btn btn--sm">Apply Say</button></div>
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
