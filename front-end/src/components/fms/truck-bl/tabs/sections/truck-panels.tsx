import { useFormContext, Controller } from "react-hook-form";
import { Search } from "lucide-react";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { PanelDateInput }     from "@/components/shared/grid-cell-inputs";
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
  nameKey: keyof HouseBlFormValues;
  addrKey: keyof HouseBlFormValues;
};

const TRUCK_PARTIES: PartyDef[] = [
  { key: "shipper",     role: "SHIPPER",     btn: null,           codeKey: "shipperCode",    nameKey: "shipperName",    addrKey: "shipperAddr"       },
  { key: "consignee",   role: "CONSIGNEE",   btn: "To Order",     codeKey: "consigneeCode",  nameKey: "consigneeName",  addrKey: "consigneeAddr"     },
  { key: "notify",      role: "NOTIFY",      btn: "Same as Cne.", codeKey: "notifyCode",     nameKey: "notifyName",     addrKey: "notifyAddr"        },
  { key: "doc-partner", role: "DOC PARTNER", btn: null,           codeKey: "docPartnerCode", nameKey: "docPartnerName", addrKey: "docPartnerAddress" },
];

function TruckPartyBlock({ party }: { party: PartyDef }) {
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <div className="party-block">
      <div className="party-block__head">
        <span style={{ fontSize: 11, color: "var(--ink)", minWidth: 90, flexShrink: 0 }}>{party.role}</span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input {...register(party.codeKey)} placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input {...register(party.nameKey)} className="party-cn__name" placeholder="Company Name" />
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
      <Controller
        control={control}
        name={party.addrKey}
        render={({ field }) => (
          <LineNumberTextarea
            value={field.value as string}
            onChange={field.onChange}
            onBlur={field.onBlur}
            name={field.name}
            placeholder="Address (free text)"
            style={{ height: 100 }}
          />
        )}
      />
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
  const { control } = useFormContext<HouseBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    { key: "vessel", render: () => <LiField label="Vessel" registerKey="vesselName" /> },
    { key: "etd",    render: () => (
      <div className="li">
        <span className="li__label is-required">ETD</span>
        <div className="li__input">
          <Controller
            control={control}
            name="etd"
            render={({ field }) => (
              <PanelDateInput
                required
                value={field.value as string}
                onChange={field.onChange}
                onBlur={field.onBlur}
                ref={field.ref}
              />
            )}
          />
        </div>
      </div>
    )},
    { key: "eta",    render: () => (
      <div className="li">
        <span className="li__label is-required">ETA</span>
        <div className="li__input">
          <Controller
            control={control}
            name="eta"
            render={({ field }) => (
              <PanelDateInput
                required
                value={field.value as string}
                onChange={field.onChange}
                onBlur={field.onBlur}
                ref={field.ref}
              />
            )}
          />
        </div>
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
function TruckPackageField() {
  const { register } = useFormContext<HouseBlFormValues>();
  return (
    <div className="li">
      <span className="li__label">Package</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input
          type="number"
          step="1"
          {...register("pkgQty")}
          style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }}
        />
        <select {...register("pkgUnit")} style={WT_SEL}>
          <option value="CTN">CTN</option>
          <option value="PKG">PKG</option>
          <option value="BAG">BAG</option>
          <option value="PLT">PLT</option>
          <option value="BOX">BOX</option>
          <option value="PCS">PCS</option>
          <option value="ROL">ROL</option>
        </select>
      </div>
    </div>
  );
}

const CARGO_ITEMS: FieldItemDef[] = [
  { key: "package",   render: () => <TruckPackageField /> },
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
export function TruckDocumentPanel() {
  const { register, control } = useFormContext<HouseBlFormValues>();
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1, padding: "8px 0" }}>
        {/* Pick-up Date */}
        <div className="li">
          <span className="li__label">Pick-up Date</span>
          <div className="li__input">
            <Controller
              control={control}
              name="pickupDate"
              render={({ field }) => (
                <PanelDateInput
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  ref={field.ref}
                />
              )}
            />
          </div>
        </div>
        {/* Trucker */}
        <div className="lcn" style={{ marginBottom: 4 }}>
          <span className="lcn__label">Trucker</span>
          <div className="lcn__code">
            <input {...register("truckerCode")} placeholder="Code" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          </div>
          <input className="lcn__name" {...register("truckerName")} placeholder="Trucker Name" />
        </div>
        {/* Trucker PIC */}
        <LiField label="Trucker PIC" registerKey="truckerPic" />
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
