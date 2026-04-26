"use client";

import { Search } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { DateCell, TimeCell, PanelDateInput } from "@/components/shared/grid-cell-inputs";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

interface Props { variant: MasterVariantConfig }
interface LegRow { to: string; by: string; flight: string; onBoard: string; boardTime: string; arrival: string; arrTime: string; }

const LEG_COLS: GridColumn<LegRow>[] = [
  { key: "_no",       width: 32, align: "center", label: "#",        className: "row-num", render: (_, __, i) => i + 1 },
  { key: "to",        width: 40, align: "center", label: "To",       render: v => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "by",        width: 32, align: "center", label: "By",       render: v => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "flight",    width: 50, align: "center", label: "Flight",   render: v => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "onBoard",   width: 96, align: "center", label: "On Board", render: v => <DateCell defaultValue={String(v)} /> },
  { key: "boardTime", width: 58, align: "center", label: "Time",     render: v => <TimeCell defaultValue={String(v)} /> },
  { key: "arrival",   width: 96, align: "center", label: "Arrival",  render: v => <DateCell defaultValue={String(v)} /> },
  { key: "arrTime",   width: 58, align: "center", label: "Time",     render: v => <TimeCell defaultValue={String(v)} /> },
];
const LEG_DATA: LegRow[] = [
  { to: "PVG", by: "KE", flight: "KE851", onBoard: "2026-04-26", boardTime: "09:30", arrival: "2026-04-26", arrTime: "11:45" },
  { to: "NRT", by: "KE", flight: "KE701", onBoard: "2026-04-27", boardTime: "08:00", arrival: "2026-04-27", arrTime: "09:20" },
];

// ── 공통 헬퍼 ──────────────────────────────────────────────
function SchedField({ label, value, req, type = "text" }: { label: string; value?: string; req?: boolean; type?: string }) {
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        {type === "date"
          ? <PanelDateInput defaultValue={value} required={req} />
          : <input defaultValue={value} style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />}
      </div>
    </div>
  );
}

function CodeNameField({ label, code, name, req }: { label: string; code: string; name: string; req?: boolean }) {
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input" style={{ gap: 4 }}>
        <input defaultValue={code} style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        <input defaultValue={name} style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
      </div>
    </div>
  );
}

function LcnField({ label, req, code, name }: { label: string; req?: boolean; code: string; name: string }) {
  return (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className={`lcn__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input defaultValue={code} placeholder="UNLOC" style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        <Search size={10} className="lcn__icon" />
      </div>
      <input className="lcn__name" defaultValue={name} placeholder="Port" style={{ fontSize: 10 }} />
    </div>
  );
}

// ── Sea schedule ────────────────────────────────────────────
function buildSeaFields(panelScope: string, isExp: boolean): FieldWidgetDef[] {
  const linerItems: FieldItemDef[] = [
    { key: "liner",  render: () => <CodeNameField label="Liner"  code="COSCO" name="COSCO SHIPPING" req /> },
    { key: "vessel", render: () => <SchedField    label="Vessel" value="COSCO EXCELLENCE" req /> },
    { key: "voyage", render: () => <SchedField    label="Voyage" value="0412E" req /> },
    { key: "etd",    render: () => <SchedField    label="ETD"    value="2026-04-24" req type="date" /> },
    { key: "eta",    render: () => <SchedField    label="ETA"    value="2026-05-08" req type="date" /> },
  ];
  const portItems: FieldItemDef[] = [
    { key: "pol",      render: () => <LcnField label="POL"      req  code="KRBSAN" name="Busan" /> },
    { key: "pod",      render: () => <LcnField label="POD"      req  code="CNSHA"  name="Shanghai" /> },
    { key: "delivery", render: () => <LcnField label="Delivery"      code=""       name="" /> },
  ];
  const issueItems: FieldItemDef[] = [
    { key: "issue-date",   render: () => <SchedField label="Issue Date"   value="2026-04-20" type="date" /> },
    { key: "freight-term", render: () => <SchedField label="Freight Term" value="Prepaid" /> },
  ];

  return [
    { key: "liner-vessel", label: "Liner & Vessel", render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={linerItems} /> },
    { key: "ports",        label: "Ports",          render: () => <FieldItemGrid itemScope={`${panelScope}.ports`} items={portItems} showRowControls={false} /> },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];
}

// ── Air schedule ────────────────────────────────────────────
function buildAirFields(panelScope: string, isExp: boolean): FieldWidgetDef[] {
  const carrierItems: FieldItemDef[] = [
    { key: "carrier",   render: () => <CodeNameField label={isExp ? "Airline" : "Carrier"} code={isExp ? "KE" : "OZ"} name={isExp ? "Korean Air" : "Asiana Airlines"} req /> },
    { key: "departure", render: () => <CodeNameField label="Departure" code="ICN" name="Incheon Int'l" req /> },
  ];
  const issueItems: FieldItemDef[] = [
    { key: "issue-date",  render: () => <SchedField label="Issue Date"  value="2026-04-20" type="date" /> },
    { key: "signature",   render: () => <SchedField label="Signature"   value="" /> },
    { key: "issue-place", render: () => <SchedField label="Issue Place" value="INCHEON" /> },
  ];

  return [
    {
      key: "carrier", label: "Carrier",
      render: () => <FieldItemGrid itemScope={`${panelScope}.carrier`} items={carrierItems} />,
    },
    {
      key: "legs", label: "Schedule Legs",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Schedule Legs</div>
          <div style={{ overflow: "auto" }}>
            <GridList columns={LEG_COLS} data={LEG_DATA} rowKey={(_, i) => i} />
          </div>
        </>
      ),
    },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];
}

export function MasterSchedulePanel({ variant }: Props) {
  const panelScope = `master-schedule-panel.${variant.key}`;
  const isExp      = variant.direction === "EXP";
  const fields     = variant.mode === "SEA"
    ? buildSeaFields(panelScope, isExp)
    : buildAirFields(panelScope, isExp);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Schedule</span>
        <div className="panel__actions"><button className="btn btn--sm">Reset</button></div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
