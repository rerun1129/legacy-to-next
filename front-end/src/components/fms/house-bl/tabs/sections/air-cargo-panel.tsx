import { PackageField } from "@/components/shared/panel-fields";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)
// NOTE: 이 패널의 cargo 수량/중량 필드는 house-bl-schema에 미포함 — 추후 스키마 확장 시 register 전환

interface Props { variant?: AnyVariantConfig }

const LI_ST: React.CSSProperties = { width: "100%", height: 24, padding: "0 6px", fontSize: 10 };
const UNIT_SEL: React.CSSProperties = {
  height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

function LiField({ label, value, type = "text" }: { label: string; value: string; type?: string }) {
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input"><input type={type} step={type === "number" ? "any" : undefined} defaultValue={value} style={LI_ST} /></div>
    </div>
  );
}

function GWField() {
  return (
    <div className="li">
      <span className="li__label">Gross W/T</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input type="number" step="any" defaultValue="30600" style={{ ...LI_ST, flex: 1, width: undefined }} />
        <select style={UNIT_SEL}><option>KGS</option><option>LBS</option></select>
      </div>
    </div>
  );
}

const CARGO_ITEMS: FieldItemDef[] = [
  { key: "packages",  render: () => <PackageField height={24} /> },
  { key: "gross-wt",  render: () => <GWField /> },
  { key: "volume-wt", render: () => <LiField label="Volume W/T" value="14583" type="number" /> },
  { key: "charge-wt", render: () => <LiField label="Charge W/T" value="30600" type="number" /> },
  { key: "rate-class", render: () => <LiField label="Rate Class" value="GCR" /> },
  { key: "cbm",       render: () => <LiField label="CBM"        value="87.5"  type="number" /> },
];

export function AirCargoPanel({ variant }: Props) {
  const panelScope = variant ? `air-cargo-panel.${variant.key}` : "air-cargo-panel";

  const fields: FieldWidgetDef[] = [
    {
      key:   "cargo",
      label: "Cargo",
      render: () => (
        <FieldItemGrid itemScope={`${panelScope}.cargo`} items={CARGO_ITEMS} cols={1} shouldShowRowControls={false} />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
