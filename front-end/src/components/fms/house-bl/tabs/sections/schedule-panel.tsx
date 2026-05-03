import type React from "react";
import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import { Search } from "lucide-react";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

interface Props { variant?: AnyVariantConfig }

// 라벨 → RHF 필드명 매핑 (Issue Information 섹션)
const ISSUE_LABEL_TO_FIELD: Record<string, FieldPath<HouseBlFormValues>> = {
  "Issue Date":  "seaDetail.issueDate",
  "No. of B/L":  "seaDetail.noOfBl",
  "Issue Place": "seaDetail.issuePlace",
  "D/O Date":    "seaDetail.doDate",
  "Signature":   "seaDetail.signature",
};

// ── 공통 헬퍼 ──────────────────────────────────────────────
function SchedField({
  label,
  name,
  req,
  type = "text",
}: {
  label: string;
  name: FieldPath<HouseBlFormValues>;
  req?: boolean;
  type?: string;
}) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        {type === "date" ? (
          <Controller
            control={control}
            name={name}
            render={({ field }) => (
              <PanelDateInput
                required={req}
                value={field.value as string}
                onChange={field.onChange}
                onBlur={field.onBlur}
                ref={field.ref}
              />
            )}
          />
        ) : (
          <input type={type} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...register(name)} />
        )}
      </div>
    </div>
  );
}

function LcnField({
  label,
  req,
  codeField,
  nameField,
}: {
  label: string;
  req?: boolean;
  codeField: FieldPath<HouseBlFormValues>;
  nameField: FieldPath<HouseBlFormValues>;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  return (
    <div className="lcn">
      <span className={`lcn__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register(codeField)} />
        <Search size={12} className="lcn__icon" />
      </div>
      <input className="lcn__name" placeholder="Port Name" {...register(nameField)} />
    </div>
  );
}

function IssueSection({ issueFields, panelScope }: { issueFields: string[]; panelScope: string }) {
  const issueItems: FieldItemDef[] = issueFields.map(f => {
    const fieldName = ISSUE_LABEL_TO_FIELD[f];
    return {
      key:    f.toLowerCase().replace(/[^a-z0-9]/g, "-"),
      render: () => fieldName
        ? <SchedField label={f} name={fieldName} type={f.includes("Date") ? "date" : "text"} />
        : <div className="li"><span className="li__label">{f}</span></div>,
    };
  });
  return (
    <>
      <div className="subhead"><div className="subhead__bar" />Issue Information</div>
      <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} />
    </>
  );
}

function DoDateSection() {
  return <SchedField label="D/O Date" name="seaDetail.doDate" type="date" />;
}

// ── RHF-bound liner row ─────────────────────────────────────
function LinerRow({ codeProps, nameProps }: { codeProps: React.InputHTMLAttributes<HTMLInputElement>; nameProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label is-required">Liner</span>
      <div className="li__input" style={{ gap: 4 }}>
        <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...codeProps} />
        <input style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} {...nameProps} />
      </div>
    </div>
  );
}

function VesselRow({ inputProps }: { inputProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label is-required">Vessel</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...inputProps} />
      </div>
    </div>
  );
}

function VoyageRow({ inputProps }: { inputProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label is-required">Voyage</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...inputProps} />
      </div>
    </div>
  );
}

// ── Schedule Panel ──────────────────────────────────────────
export function SchedulePanel({ variant }: Props) {
  const { register } = useFormContext<HouseBlFormValues>();

  if (!variant) return null;
  const panelScope = `schedule-panel.${variant.key}`;

  const PORT_ITEMS: FieldItemDef[] = [
    { key: "pol",      render: () => <LcnField label="POL"      req  codeField="pol"                    nameField="seaDetail.polName" /> },
    { key: "pod",      render: () => <LcnField label="POD"      req  codeField="pod"                    nameField="seaDetail.podName" /> },
    { key: "delivery", render: () => <LcnField label="Delivery"      codeField="seaDetail.deliveryCode" nameField="seaDetail.deliveryName" /> },
  ];

  const linerItems: FieldItemDef[] = [
    { key: "liner",    render: () => <LinerRow codeProps={register("linerCode")} nameProps={register("linerName")} /> },
    { key: "vessel",   render: () => <VesselRow inputProps={register("vesselName")} /> },
    { key: "voyage",   render: () => <VoyageRow inputProps={register("voyNo")} /> },
    { key: "etd",      render: () => <SchedField label="ETD"      name="etd"                  req  type="date" /> },
    { key: "eta",      render: () => <SchedField label="ETA"      name="eta"                  req  type="date" /> },
    { key: "on-board", render: () => <SchedField label="On Board" name="seaDetail.onboardDate"     type="date" /> },
  ];

  const fields: FieldWidgetDef[] = [
    {
      key:   "liner",
      label: "Liner & Vessel",
      render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={linerItems} />,
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
