"use client";

import { useFormContext, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

const UNIT_SEL: React.CSSProperties = {
  height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

function LiField({ label, name, numeric }: { label: string; name: string; numeric?: boolean }) {
  const { register } = useFormContext();
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input">
        {numeric
          ? <input type="number" step="any" style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} {...register(name)} />
          : <input style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} {...register(name)} />
        }
      </div>
    </div>
  );
}

function GWField() {
  const { register } = useFormContext();
  return (
    <div className="li">
      <span className="li__label">G/W</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input type="number" step="any"
          style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }}
          {...register("grossWeightKg", { valueAsNumber: true })}
        />
        <select defaultValue="" style={UNIT_SEL}><option value=""></option><option>KGS</option><option>LBS</option></select>
      </div>
    </div>
  );
}

function PackageFieldRegistered() {
  const { register } = useFormContext();
  const sel: React.CSSProperties = {
    height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
    border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
  };
  return (
    <div className="li">
      <span className="li__label">Package</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input type="number" step="1"
          style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }}
          {...register("pkgQty", { valueAsNumber: true })}
        />
        <select defaultValue="" style={sel}>
          <option value=""></option><option>CTN</option><option>PKG</option><option>BAG</option>
          <option>PLT</option><option>BOX</option><option>PCS</option><option>ROL</option>
        </select>
      </div>
    </div>
  );
}

export function MasterCargoDocPanel({ variant }: Props) {
  if (!variant) return null;
  const isSea      = variant.mode === "SEA";
  const panelScope = `master-cargo-doc.${variant.key}`;

  const cargoBase: FieldItemDef[] = [
    { key: "main-item", render: () => <LiField label="Main Item" name="cargoMainItem" /> },
    { key: "hs-code",   render: () => <LiField label="HS Code"   name="cargoHsCode" /> },
    { key: "package",   render: () => <PackageFieldRegistered /> },
    { key: "gw",        render: () => <GWField /> },
    { key: "cbm",       render: () => <LiField label="CBM"       name="cbm"  numeric /> },
  ];

  const cargoExtras: FieldItemDef[] = isSea
    ? [{ key: "r-ton", render: () => <LiField label="R/Ton" name="rTon" numeric /> }]
    : [
        { key: "vol-wt",     render: () => <LiField label="Volume W/T" name="volWeight"   numeric /> },
        { key: "charge-wt",  render: () => <LiField label="Charge W/T" name="chargeWeight" numeric /> },
        { key: "rate-class", render: () => <LiField label="Rate Class" name="rateClass" /> },
      ];

  const cargoItems = [...cargoBase, ...cargoExtras];

  const seaDoc: FieldItemDef[] = [
    { key: "settle",   render: () => <LiField label="Settle Partner" name="settlePartner" /> },
    { key: "co-load",  render: () => <LiField label="Co-Load Agent"  name="coLoadAgent" /> },
    { key: "operator", render: () => <LiField label="Operator"       name="operatorCode" /> },
    { key: "team",     render: () => <LiField label="Team"           name="teamCode" /> },
  ];

  const airDocBase: FieldItemDef[] = [
    { key: "co-load-type",  render: () => <LiField label="Co-Load Type"  name="coLoadType" /> },
    { key: "co-load-agent", render: () => <LiField label="Co-Load Agent" name="coLoadAgent" /> },
    { key: "flight-type",   render: () => <LiField label="Flight Type"   name="flightType" /> },
  ];
  const airDocSec: FieldItemDef[] = variant.direction === "EXP"
    ? [{ key: "security", render: () => <LiField label="Security Status" name="securityStatus" /> }]
    : [];
  const airDocTail: FieldItemDef[] = [
    { key: "settle",   render: () => <LiField label="Settle Partner" name="settlePartner" /> },
    { key: "operator", render: () => <LiField label="Operator"       name="operatorCode" /> },
    { key: "team",     render: () => <LiField label="Team"           name="teamCode" /> },
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
