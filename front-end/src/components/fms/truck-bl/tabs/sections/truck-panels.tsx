import { Search } from "lucide-react";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { PanelDateInput }     from "@/components/shared/grid-cell-inputs";
import { PackageField }       from "@/components/shared/panel-fields";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

// ── 공통 헬퍼 ──────────────────────────────────────────────
const WT_SEL: React.CSSProperties = {
  height: 22, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

function LiField({ label, value, req, readOnly, numeric }: { label: string; value?: string; req?: boolean; readOnly?: boolean; numeric?: boolean }) {
  const base: React.CSSProperties = { width: "100%", height: 22, padding: "0 8px", fontSize: 10 };
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        {numeric
          ? <input type="number" step="any" defaultValue={(value ?? "").replace(/,/g, "")} style={base} />
          : <input defaultValue={value} placeholder={value || label} readOnly={readOnly}
              style={{ ...base, ...(readOnly ? { background: "var(--bg-sunken)", color: "var(--ink-3)", fontFamily: "var(--font-mono)" } : {}) }} />
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
          style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
        <select style={WT_SEL}><option>KGS</option><option>LBS</option></select>
      </div>
    </div>
  );
}

// ── Party ──────────────────────────────────────────────────
const TRUCK_PARTIES = [
  { key: "shipper",     role: "SHIPPER",     btn: null           },
  { key: "consignee",   role: "CONSIGNEE",   btn: "To Order"     },
  { key: "notify",      role: "NOTIFY",      btn: "Same as Cne." },
  { key: "doc-partner", role: "DOC PARTNER", btn: null           },
] as const;

function TruckPartyBlock({ party }: { party: typeof TRUCK_PARTIES[number] }) {
  return (
    <div className="party-block">
      <div className="party-block__head">
        <span style={{ fontSize: 11, color: "var(--ink)", minWidth: 90, flexShrink: 0 }}>{party.role}</span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" placeholder="Company Name" />
        </div>
        <div className="party-block__head-actions">
          {party.btn && <button className="party-block__head-btn">{party.btn}</button>}
          <button className="party-block__head-btn">Clear</button>
        </div>
      </div>
      <LineNumberTextarea placeholder="Address (free text)" style={{ height: 100 }} />
    </div>
  );
}

export function TruckPartyPanel() {
  const fields: FieldWidgetDef[] = TRUCK_PARTIES.map(p => ({
    key:   p.key,
    label: p.role,
    render: () => <TruckPartyBlock party={p} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="truck-party-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Schedule ───────────────────────────────────────────────
const VESSEL_DATE_ITEMS: FieldItemDef[] = [
  { key: "vessel", render: () => <LiField label="Vessel" value="TRUCK" readOnly /> },
  { key: "etd",    render: () => (
    <div className="li"><span className="li__label is-required">ETD</span><div className="li__input"><PanelDateInput defaultValue="2026-04-24" required /></div></div>
  )},
  { key: "eta",    render: () => (
    <div className="li"><span className="li__label is-required">ETA</span><div className="li__input"><PanelDateInput defaultValue="2026-04-25" required /></div></div>
  )},
];

const TRUCK_PORT_ITEMS: FieldItemDef[] = [
  { key: "pol", render: () => (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label is-required">POL</span>
      <div className="lcn__code" style={{ position: "relative" }}><input defaultValue="KRBSAN" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" defaultValue="Busan" placeholder="Location" />
    </div>
  )},
  { key: "pod", render: () => (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label is-required">POD</span>
      <div className="lcn__code" style={{ position: "relative" }}><input defaultValue="KRSEL" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" defaultValue="Seoul" placeholder="Location" />
    </div>
  )},
];

export function TruckSchedulePanel() {
  const fields: FieldWidgetDef[] = [
    { key: "vessel-dates", label: "Vessel & Dates", render: () => <FieldItemGrid itemScope="truck-schedule-panel.vessel-dates" items={VESSEL_DATE_ITEMS} /> },
    { key: "ports",        label: "Ports",          render: () => <FieldItemGrid itemScope="truck-schedule-panel.ports" items={TRUCK_PORT_ITEMS} showRowControls={false} /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="truck-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Cargo ──────────────────────────────────────────────────
const CARGO_ITEMS: FieldItemDef[] = [
  { key: "package",   render: () => <PackageField qty="1300" unit="CTN" /> },
  { key: "gw",        render: () => <GWField value="30600" /> },
  { key: "cbm",       render: () => <LiField label="CBM"        value="87.5"  numeric /> },
  { key: "charge-wt", render: () => <LiField label="Charge W/T" value="30600" numeric /> },
];

export function TruckCargoPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="truck-cargo-panel" items={CARGO_ITEMS} />
      </div>
    </div>
  );
}

// ── Document ───────────────────────────────────────────────
const DOC_ITEMS: FieldItemDef[] = [
  { key: "pickup-date", render: () => (
    <div className="li"><span className="li__label">Pick-up Date</span><div className="li__input"><PanelDateInput defaultValue="2026-04-23" /></div></div>
  )},
  { key: "trucker", render: () => (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label">Trucker</span>
      <div className="lcn__code"><input placeholder="Code" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" placeholder="Trucker Name" />
    </div>
  )},
  { key: "trucker-pic", render: () => <LiField label="Trucker PIC" value="" /> },
];

export function TruckDocumentPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="truck-document-panel" items={DOC_ITEMS} />
      </div>
    </div>
  );
}

// ── Performance ────────────────────────────────────────────
const TRUCK_PERF_ITEMS: FieldItemDef[] = [
  { key: "actual-customer", render: () => <LiField label="Actual Customer" req /> },
  { key: "customer-pic",    render: () => <LiField label="Customer PIC" /> },
  { key: "settle-partner",  render: () => <LiField label="Settle Partner" /> },
  { key: "sales-man",       render: () => <LiField label="Sales Man"      req /> },
  { key: "operator",        render: () => <LiField label="Operator"       req /> },
  { key: "team",            render: () => <LiField label="Team"           req /> },
];

export function TruckPerformancePanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="truck-performance-panel" items={TRUCK_PERF_ITEMS} />
      </div>
    </div>
  );
}
