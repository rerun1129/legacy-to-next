"use client";

import { useState }        from "react";
import { Search }          from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { PanelDateInput }  from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

// ── 공통 헬퍼 ──────────────────────────────────────────────
function LiField({ label, value, req }: { label: string; value?: string; req?: boolean }) {
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        <input placeholder={label} defaultValue={value} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
      </div>
    </div>
  );
}

// ── Party ──────────────────────────────────────────────────
const NON_PARTIES = [
  { key: "actual-customer", role: "ACTUAL CUSTOMER", req: true,  btn: null           },
  { key: "shipper",         role: "SHIPPER",         req: false, btn: null           },
  { key: "consignee",       role: "CONSIGNEE",       req: false, btn: "To Order"     },
  { key: "notify",          role: "NOTIFY",          req: false, btn: "Same as Cne." },
  { key: "settle-partner",  role: "SETTLE PARTNER",  req: false, btn: null           },
] as const;

function PartyBlock({ party }: { party: typeof NON_PARTIES[number] }) {
  return (
    <div className="party-block" style={{ paddingBottom: 8 }}>
      <div className="party-block__head">
        <span className={party.req ? "is-required" : undefined} style={{ fontSize: 11, minWidth: 120, flexShrink: 0 }}>
          {party.role}
        </span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" placeholder="Company Name" />
        </div>
        {party.btn && <div className="party-block__head-actions"><button className="party-block__head-btn">{party.btn}</button></div>}
      </div>
    </div>
  );
}

export function NonBLPartyPanel() {
  const fields: FieldWidgetDef[] = NON_PARTIES.map(p => ({
    key:   p.key,
    label: p.role,
    render: () => <PartyBlock party={p} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="nonbl-party-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Schedule ───────────────────────────────────────────────
const DATE_ITEMS: FieldItemDef[] = [
  { key: "etd", render: () => (
    <div className="li"><span className="li__label is-required">ETD</span><div className="li__input"><PanelDateInput defaultValue="2026-04-24" required /></div></div>
  )},
  { key: "eta", render: () => (
    <div className="li"><span className="li__label is-required">ETA</span><div className="li__input"><PanelDateInput defaultValue="2026-05-08" required /></div></div>
  )},
];

const PORT_ITEMS: FieldItemDef[] = [
  { key: "pol", render: () => (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label is-required">POL</span>
      <div className="lcn__code"><input defaultValue="KRBSAN" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" defaultValue="Busan" />
    </div>
  )},
  { key: "pod", render: () => (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label is-required">POD</span>
      <div className="lcn__code"><input defaultValue="CNSHA" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" defaultValue="Shanghai" />
    </div>
  )},
];

export function NonBLSchedulePanel() {
  const fields: FieldWidgetDef[] = [
    { key: "dates", label: "Dates", render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.dates" items={DATE_ITEMS} /> },
    { key: "ports", label: "Ports", render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.ports" items={PORT_ITEMS} showRowControls={false} /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="nonbl-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Work Division (dynamic) ────────────────────────────────
type WorkDiv = "Sea" | "Air" | "Warehouse" | "Trucking";

interface ContainerRow { cno: string; type: string; pkg: string; gw: string; cbm: string; }
const CONTAINER_ROWS: ContainerRow[] = [
  { cno: "CSNU1234567", type: "20GP", pkg: "500 CTN", gw: "12,400", cbm: "22.5" },
  { cno: "TCKU9876543", type: "40HC", pkg: "800 CTN", gw: "18,200", cbm: "65.0" },
];
const CONTAINER_COLS: GridColumn<ContainerRow>[] = [
  { key: "_no", label: "#", render: (_, __, i) => i + 1 },
  { key: "cno", label: "Container No." }, { key: "type", label: "Type" },
  { key: "pkg", label: "Pkg", className: "is-num" }, { key: "gw", label: "G/W", className: "is-num" }, { key: "cbm", label: "CBM", className: "is-num" },
];

export function NonBLWorkDivPanel() {
  const [workDiv, setWorkDiv] = useState<WorkDiv>("Sea");
  const title = workDiv === "Sea" ? "Container Info" : workDiv === "Air" ? "Dimension / Cargo" : workDiv === "Warehouse" ? "Warehouse Info" : "Truck Info";
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{title}</span>
        <div className="panel__actions">
          <select value={workDiv} onChange={e => setWorkDiv(e.target.value as WorkDiv)} style={{ fontSize: "var(--fs-sm)", border: "1px solid var(--border)", borderRadius: 4, padding: "2px 6px", background: "var(--surface)" }}>
            <option>Sea</option><option>Air</option><option>Warehouse</option><option>Trucking</option>
          </select>
        </div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        {workDiv === "Sea" && <GridList columns={CONTAINER_COLS} data={CONTAINER_ROWS} />}
        {workDiv === "Air" && (
          <div className="sched-list">
            <div className="li"><span className="li__label">Package/Unit</span>
              <div className="li__input"><input placeholder="Package/Unit" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
            </div>
            <div className="li"><span className="li__label">Gross W/T</span>
              <div className="li__input" style={{ display: "flex", gap: 4 }}>
                <input type="number" step="any" placeholder="0" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
                <select style={{ height: 22, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none", border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)" }}>
                  <option>KGS</option><option>LBS</option>
                </select>
              </div>
            </div>
            <div className="li"><span className="li__label">Charge W/T</span>
              <div className="li__input"><input type="number" step="any" placeholder="0" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
            </div>
            <div className="li"><span className="li__label">Rate Class</span>
              <div className="li__input"><input placeholder="Rate Class" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
            </div>
            <div className="li"><span className="li__label">CBM</span>
              <div className="li__input"><input type="number" step="any" placeholder="0" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
            </div>
          </div>
        )}
        {workDiv === "Warehouse" && <div className="sched-list">{["창고 코드","입고일","출고 예정일","보관 위치","면적(m²)"].map(f => <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>)}</div>}
        {workDiv === "Trucking" && <div className="sched-list">{["Pick-up Date","Trucker","Trucker PIC","차량번호"].map(f => <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>)}</div>}
      </div>
    </div>
  );
}

// ── Reference Numbers ──────────────────────────────────────
const REF_ITEMS: FieldItemDef[] = [
  { key: "original-bl", render: () => <LiField label="원본 B/L No." /> },
  { key: "po-no",       render: () => <LiField label="PO No." /> },
  { key: "invoice-no",  render: () => <LiField label="Invoice No." /> },
  { key: "cust-ref",    render: () => <LiField label="Customer Ref" /> },
];

export function NonBLReferencePanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Reference Numbers</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="nonbl-reference-panel" items={REF_ITEMS} />
      </div>
    </div>
  );
}

// ── Performance ────────────────────────────────────────────
const PERF_ITEMS: FieldItemDef[] = [
  { key: "actual-customer", render: () => <LiField label="Actual Customer" req /> },
  { key: "operator",        render: () => <LiField label="Operator"        req /> },
  { key: "team",            render: () => <LiField label="Team"            req /> },
  { key: "sales-man",       render: () => <LiField label="Sales Man" /> },
];

export function NonBLPerformancePanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="nonbl-performance-panel" items={PERF_ITEMS} />
      </div>
    </div>
  );
}
