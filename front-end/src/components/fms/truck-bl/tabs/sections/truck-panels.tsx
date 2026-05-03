import { useFormContext } from "react-hook-form";
import { Search } from "lucide-react";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { PanelDateInput }     from "@/components/shared/grid-cell-inputs";
import { PackageField }       from "@/components/shared/panel-fields";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

// ── 공통 헬퍼 ──────────────────────────────────────────────
const WT_SEL: React.CSSProperties = {
  height: 22, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

function LiField({
  label, req, readOnly, numeric, registerKey,
}: {
  label: string;
  req?: boolean;
  readOnly?: boolean;
  numeric?: boolean;
  registerKey?: keyof HouseBlFormValues;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  const base: React.CSSProperties = { width: "100%", height: 22, padding: "0 8px", fontSize: 10 };
  const rhfProps = registerKey ? register(registerKey) : {};
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        {numeric
          ? <input type="number" step="any" {...rhfProps} style={base} />
          : <input
              {...rhfProps}
              placeholder={label}
              readOnly={readOnly}
              style={{ ...base, ...(readOnly ? { background: "var(--bg-sunken)", color: "var(--ink-3)", fontFamily: "var(--font-mono)" } : {}) }}
            />
        }
      </div>
    </div>
  );
}

function GWField() {
  const { register } = useFormContext<HouseBlFormValues>();
  return (
    <div className="li">
      <span className="li__label">G/W</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input
          type="number"
          step="any"
          {...register("grossWeightKg")}
          style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }}
        />
        <select style={WT_SEL}><option>KGS</option><option>LBS</option></select>
      </div>
    </div>
  );
}

// ── Party ──────────────────────────────────────────────────
type PartyDef = {
  key:     string;
  role:    string;
  btn:     string | null;
  codeKey: keyof HouseBlFormValues;
};

const TRUCK_PARTIES: PartyDef[] = [
  { key: "shipper",     role: "SHIPPER",     btn: null,           codeKey: "shipperCode"    },
  { key: "consignee",   role: "CONSIGNEE",   btn: "To Order",     codeKey: "consigneeCode"  },
  { key: "notify",      role: "NOTIFY",      btn: "Same as Cne.", codeKey: "notifyCode"     },
  { key: "doc-partner", role: "DOC PARTNER", btn: null,           codeKey: "docPartnerCode" },
];

function TruckPartyBlock({ party }: { party: PartyDef }) {
  const { register, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <div className="party-block">
      <div className="party-block__head">
        <span style={{ fontSize: 11, color: "var(--ink)", minWidth: 90, flexShrink: 0 }}>{party.role}</span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input {...register(party.codeKey)} placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          {/* Company name은 별도 RHF 필드 없음 — 주소 첫 줄에 포함되는 자유 텍스트 */}
          <input className="party-cn__name" placeholder="Company Name" />
        </div>
        <div className="party-block__head-actions">
          {party.btn && <button className="party-block__head-btn">{party.btn}</button>}
          <button
            type="button"
            className="party-block__head-btn"
            onClick={() => setValue(party.codeKey, "")}
          >
            Clear
          </button>
        </div>
      </div>
      {/*
        LineNumberTextarea는 onChange: (value: string) 시그니처를 사용하는 자체 관리 컴포넌트.
        RHF register와 직접 연결 불가 — 비제어 상태 유지.
      */}
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
function TruckScheduleVesselDates() {
  const ITEMS: FieldItemDef[] = [
    { key: "vessel", render: () => <LiField label="Vessel" readOnly /> },
    /*
      PanelDateInput은 내부 상태를 직접 관리하는 비제어 컴포넌트.
      register spread 불가 — defaultValue="" 유지.
    */
    { key: "etd",    render: () => (
      <div className="li">
        <span className="li__label is-required">ETD</span>
        <div className="li__input"><PanelDateInput defaultValue="" required /></div>
      </div>
    )},
    { key: "eta",    render: () => (
      <div className="li">
        <span className="li__label is-required">ETA</span>
        <div className="li__input"><PanelDateInput defaultValue="" required /></div>
      </div>
    )},
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.vessel-dates" items={ITEMS} />;
}

function TruckSchedulePorts() {
  const { register } = useFormContext<HouseBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    { key: "pol", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">POL</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input {...register("pol")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        </div>
        <input className="lcn__name" {...register("polName")} placeholder="Location" />
      </div>
    )},
    { key: "pod", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">POD</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input {...register("pod")} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        </div>
        <input className="lcn__name" {...register("podName")} placeholder="Location" />
      </div>
    )},
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.ports" items={ITEMS} shouldShowRowControls={false} />;
}

export function TruckSchedulePanel() {
  const fields: FieldWidgetDef[] = [
    { key: "vessel-dates", label: "Vessel & Dates", render: () => <TruckScheduleVesselDates /> },
    { key: "ports",        label: "Ports",          render: () => <TruckSchedulePorts /> },
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
/*
  PackageField는 qty/unit을 defaultValue로만 받는 비제어 컴포넌트.
  register spread 불가 — 비제어 상태 유지.
*/
const CARGO_ITEMS: FieldItemDef[] = [
  { key: "package",   render: () => <PackageField qty="" unit="" /> },
  { key: "gw",        render: () => <GWField /> },
  { key: "cbm",       render: () => <LiField label="CBM"        numeric registerKey="cbm" /> },
  { key: "charge-wt", render: () => <LiField label="Charge W/T" numeric registerKey="chargeWeightKg" /> },
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
function TruckDocumentItems() {
  const { register } = useFormContext<HouseBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    { key: "pickup-date", render: () => (
      <div className="li">
        <span className="li__label">Pick-up Date</span>
        {/*
          PanelDateInput은 내부 상태를 직접 관리하는 비제어 컴포넌트.
          register spread 불가 — defaultValue="" 유지.
        */}
        <div className="li__input"><PanelDateInput defaultValue="" /></div>
      </div>
    )},
    { key: "trucker", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label">Trucker</span>
        <div className="lcn__code">
          <input {...register("truckerCode")} placeholder="Code" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
        </div>
        <input className="lcn__name" {...register("truckerName")} placeholder="Trucker Name" />
      </div>
    )},
    { key: "trucker-pic", render: () => <LiField label="Trucker PIC" registerKey="truckerPic" /> },
  ];
  return <FieldItemGrid itemScope="truck-document-panel" items={ITEMS} />;
}

export function TruckDocumentPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <TruckDocumentItems />
      </div>
    </div>
  );
}

// ── Performance ────────────────────────────────────────────
const PERF_ITEMS: FieldItemDef[] = [
  { key: "actual-customer", render: () => <LiField label="Actual Customer" req  registerKey="actualCustomerCode" /> },
  { key: "customer-pic",    render: () => <LiField label="Customer PIC"         registerKey="customerPic"        /> },
  { key: "settle-partner",  render: () => <LiField label="Settle Partner"       registerKey="settlePartnerCode"  /> },
  { key: "sales-man",       render: () => <LiField label="Sales Man"       req  registerKey="salesManCode"       /> },
  { key: "operator",        render: () => <LiField label="Operator"        req  registerKey="operatorCode"       /> },
  { key: "team",            render: () => <LiField label="Team"            req  registerKey="teamCode"           /> },
];

export function TruckPerformancePanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="truck-performance-panel" items={PERF_ITEMS} />
      </div>
    </div>
  );
}
