import { Search } from "lucide-react";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

interface Props { variant?: AnyVariantConfig }

// ── 공통 헬퍼 ──────────────────────────────────────────────
function SchedField({ label, value, req, type = "text" }: { label: string; value: string; req?: boolean; type?: string }) {
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        {type === "date"
          ? <PanelDateInput defaultValue={value} required={req} />
          : <input type={type} defaultValue={value} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />}
      </div>
    </div>
  );
}

function LinerField() {
  return (
    <div className="li">
      <span className="li__label is-required">Liner</span>
      <div className="li__input" style={{ gap: 4 }}>
        <input placeholder="Code" defaultValue="COSCO" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        <input defaultValue="COSCO SHIPPING" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
      </div>
    </div>
  );
}

function LcnField({ label, req, code, name }: { label: string; req?: boolean; code: string; name: string }) {
  return (
    <div className="lcn">
      <span className={`lcn__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input defaultValue={code} placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        <Search size={12} className="lcn__icon" />
      </div>
      <input className="lcn__name" defaultValue={name} placeholder="Port Name" />
    </div>
  );
}

// ── Field item 정의 ─────────────────────────────────────────
const LINER_ITEMS: FieldItemDef[] = [
  { key: "liner",    render: () => <LinerField /> },
  { key: "vessel",   render: () => <SchedField label="Vessel"   value="COSCO EXCELLENCE" req /> },
  { key: "voyage",   render: () => <SchedField label="Voyage"   value="0412E" req /> },
  { key: "etd",      render: () => <SchedField label="ETD"      value="2026-04-24" req type="date" /> },
  { key: "eta",      render: () => <SchedField label="ETA"      value="2026-05-08" req type="date" /> },
  { key: "on-board", render: () => <SchedField label="On Board" value="" type="date" /> },
];

const PORT_ITEMS: FieldItemDef[] = [
  { key: "pol",      render: () => <LcnField label="POL"      req  code="KRBSAN" name="Busan" /> },
  { key: "pod",      render: () => <LcnField label="POD"      req  code="CNSHA"  name="Shanghai" /> },
  { key: "delivery", render: () => <LcnField label="Delivery"      code=""       name="" /> },
];

function IssueSection({ issueFields, panelScope }: { issueFields: string[]; panelScope: string }) {
  const issueItems: FieldItemDef[] = issueFields.map(f => ({
    key:    f.toLowerCase().replace(/[^a-z0-9]/g, "-"),
    render: () => (
      <SchedField
        label={f}
        value={f === "Issue Date" ? "2026-04-20" : f === "No. of B/L" ? "3" : f === "Issue Place" ? "BUSAN" : ""}
        type={f.includes("Date") ? "date" : "text"}
      />
    ),
  }));
  return (
    <>
      <div className="subhead"><div className="subhead__bar" />Issue Information</div>
      <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} />
    </>
  );
}

function DoDateSection() {
  return <SchedField label="D/O Date" value="" type="date" />;
}

// ── Schedule Panel ──────────────────────────────────────────
export function SchedulePanel({ variant }: Props) {
  if (!variant) return null;
  const panelScope = `schedule-panel.${variant.key}`;

  const fields: FieldWidgetDef[] = [
    {
      key:   "liner",
      label: "Liner & Vessel",
      render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={LINER_ITEMS} />,
    },
    {
      key:   "ports",
      label: "Ports",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Ports</div>
          <FieldItemGrid itemScope={`${panelScope}.ports`} items={PORT_ITEMS} />
        </>
      ),
    },
    ...(variant.issueFields.length > 0
      ? [{ key: "issue", label: "Issue Information", render: () => <IssueSection issueFields={variant.issueFields} panelScope={panelScope} /> }]
      : []),
    ...(variant.hasDoDate
      ? [{ key: "do-date", label: "D/O Date",          render: () => <DoDateSection /> }]
      : []),
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Schedule</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
