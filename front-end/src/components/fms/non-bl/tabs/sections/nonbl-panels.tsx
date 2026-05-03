"use client";

import { useState }              from "react";
import { useFormContext }        from "react-hook-form";
import { Search }                from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { NonBlFormValues }  from "@/components/fms/non-bl/non-bl-schema";

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

type PartyKey = typeof NON_PARTIES[number]["key"];

// 파티 키 → RHF 필드명 매핑
const PARTY_CODE_FIELD: Record<PartyKey, keyof NonBlFormValues> = {
  "actual-customer": "actualCustomerCode",
  "shipper":         "shipperCode",
  "consignee":       "consigneeCode",
  "notify":          "notifyCode",
  "settle-partner":  "settlePartnerCode",
};
const PARTY_NAME_FIELD: Record<PartyKey, keyof NonBlFormValues> = {
  "actual-customer": "actualCustomerName",
  "shipper":         "shipperName",
  "consignee":       "consigneeName",
  "notify":          "notifyName",
  "settle-partner":  "settlePartnerName",
};

function PartyBlock({ party }: { party: typeof NON_PARTIES[number] }) {
  const { register } = useFormContext<NonBlFormValues>();
  return (
    <div className="party-block" style={{ paddingBottom: 8 }}>
      <div className="party-block__head">
        <span className={party.req ? "is-required" : undefined} style={{ fontSize: 11, minWidth: 120, flexShrink: 0 }}>
          {party.role}
        </span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input {...register(PARTY_CODE_FIELD[party.key])} placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" {...register(PARTY_NAME_FIELD[party.key])} placeholder="Company Name" />
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

// ── Schedule 개별 필드 컴포넌트 ────────────────────────────
function SchedEtd()    { const { register } = useFormContext<NonBlFormValues>(); return <div className="li"><span className="li__label is-required">ETD</span><div className="li__input"><input type="date" {...register("etd")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>; }
function SchedEta()    { const { register } = useFormContext<NonBlFormValues>(); return <div className="li"><span className="li__label is-required">ETA</span><div className="li__input"><input type="date" {...register("eta")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>; }
function SchedLiner()  { const { register } = useFormContext<NonBlFormValues>(); return <div className="li"><span className="li__label is-required">Liner</span><div className="li__input" style={{ gap: 4 }}><input {...register("linerCode")} placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /><input {...register("linerName")} placeholder="Liner Name" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>; }
function SchedVessel() { const { register } = useFormContext<NonBlFormValues>(); return <div className="li"><span className="li__label is-required">Vessel</span><div className="li__input"><input {...register("vesselName")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>; }
function SchedVoy()    { const { register } = useFormContext<NonBlFormValues>(); return <div className="li"><span className="li__label is-required">Voy</span><div className="li__input"><input {...register("voyNo")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>; }

function SchedPol() {
  const { register } = useFormContext<NonBlFormValues>();
  return (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label is-required">POL</span>
      <div className="lcn__code"><input {...register("polCode")} placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" {...register("polName")} placeholder="Port Name" />
    </div>
  );
}
function SchedPod() {
  const { register } = useFormContext<NonBlFormValues>();
  return (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label is-required">POD</span>
      <div className="lcn__code"><input {...register("podCode")} placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" {...register("podName")} placeholder="Port Name" />
    </div>
  );
}
function SchedFinalDest() {
  const { register } = useFormContext<NonBlFormValues>();
  return (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className="lcn__label">Final Dest</span>
      <div className="lcn__code"><input {...register("finalDestCode")} placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
      <input className="lcn__name" {...register("finalDestName")} placeholder="Port Name" />
    </div>
  );
}
function SchedFinalEta() { const { register } = useFormContext<NonBlFormValues>(); return <div className="li"><span className="li__label">Final ETA</span><div className="li__input"><input type="date" {...register("finalEta")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>; }

const SCHED_LINER_ITEMS: FieldItemDef[] = [
  { key: "liner",  render: () => <SchedLiner /> },
  { key: "vessel", render: () => <SchedVessel /> },
  { key: "voy",    render: () => <SchedVoy /> },
];
const SCHED_DATE_ITEMS: FieldItemDef[] = [
  { key: "etd", render: () => <SchedEtd /> },
  { key: "eta", render: () => <SchedEta /> },
];
const SCHED_PORT_ITEMS: FieldItemDef[] = [
  { key: "pol",        render: () => <SchedPol /> },
  { key: "pod",        render: () => <SchedPod /> },
  { key: "final-dest", render: () => <SchedFinalDest /> },
  { key: "final-eta",  render: () => <SchedFinalEta /> },
];

export function NonBLSchedulePanel() {
  const fields: FieldWidgetDef[] = [
    { key: "liner", label: "Liner & Vessel", render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.liner" items={SCHED_LINER_ITEMS} shouldShowRowControls={false} /> },
    { key: "dates", label: "Dates",          render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.dates" items={SCHED_DATE_ITEMS}  shouldShowRowControls={false} /> },
    { key: "ports", label: "Ports",          render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.ports" items={SCHED_PORT_ITEMS}  shouldShowRowControls={false} /> },
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

interface ContainerRow { id: number; cno: string; type: string; pkg: string; gw: string; cbm: string; }
const CONTAINER_ROWS: ContainerRow[] = [
  { id: 1, cno: "CSNU1234567", type: "20GP", pkg: "500 CTN", gw: "12,400", cbm: "22.5" },
  { id: 2, cno: "TCKU9876543", type: "40HC", pkg: "800 CTN", gw: "18,200", cbm: "65.0" },
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
        {workDiv === "Sea" && <GridList columns={CONTAINER_COLS} data={CONTAINER_ROWS} rowKey={(row) => row.id} />}
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
