import type React from "react";
import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { TextBox, CodeBox, DateBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
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
              <DateBox
                variant="panel"
                required={req}
                ref={field.ref}
                name={field.name}
                value={field.value as string}
                onChange={field.onChange}
                onBlur={field.onBlur}
              />
            )}
          />
        ) : (
          <TextBox variant="panel" {...register(name)} />
        )}
      </div>
    </div>
  );
}

interface IssueSectionProps {
  issueFields: string[];
  panelScope: string;
  noOfBlOptions: { value: string; label: string }[];
  noOfBlPlaceholder: string | undefined;
}

function IssueSection({ issueFields, panelScope, noOfBlOptions, noOfBlPlaceholder }: IssueSectionProps) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const issueItems: FieldItemDef[] = issueFields.map(f => {
    const fieldName = ISSUE_LABEL_TO_FIELD[f];
    if (f === "No. of B/L") {
      return {
        key: "no-of-bl",
        render: () => (
          <div className="li">
            <span className="li__label">No. of B/L</span>
            <div className="li__input">
              <Controller
                name="seaDetail.noOfBl"
                control={control}
                render={({ field }) => (
                  <ComboBox
                    variant="panel"
                    options={noOfBlOptions}
                    placeholder={noOfBlPlaceholder}
                    value={field.value ?? ""}
                    onChange={field.onChange}
                  />
                )}
              />
            </div>
          </div>
        ),
      };
    }
    if (f === "Issue Place") {
      return {
        key: "issue-place",
        render: () => (
          <CodeBox
            kind="lcn"
            variant="panel"
            label="Issue Place"
            codeProps={{ ...register("seaDetail.issuePlace") }}
            nameProps={{ ...register("seaDetail.issuePlaceName") }}
            onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          />
        ),
      };
    }
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

// ── Schedule Panel ──────────────────────────────────────────
export function SchedulePanel({ variant }: Props) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: noOfBlOptions, placeholder: noOfBlPlaceholder } = useEnumOptions("NoOfBl");

  if (!variant) return null;
  const panelScope = `schedule-panel.${variant.key}`;

  const PORT_ITEMS: FieldItemDef[] = [
    {
      key: "pol",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="POL"
          required
          codeProps={{ ...register("pol"), placeholder: "UNLOC" }}
          nameProps={{ ...register("seaDetail.polName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
        />
      ),
    },
    {
      key: "pod",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="POD"
          required
          codeProps={{ ...register("pod"), placeholder: "UNLOC" }}
          nameProps={{ ...register("seaDetail.podName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
        />
      ),
    },
    {
      key: "delivery",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="Delivery"
          codeProps={{ ...register("seaDetail.deliveryCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("seaDetail.deliveryName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
        />
      ),
    },
  ];

  const linerItems: FieldItemDef[] = [
    {
      key: "liner",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="Liner"
          required
          codeProps={{ ...register("linerCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("linerName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
        />
      ),
    },
    {
      key: "vessel",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Vessel</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("vesselName")} />
          </div>
        </div>
      ),
    },
    {
      key: "voyage",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Voyage</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("voyNo")} />
          </div>
        </div>
      ),
    },
    {
      key: "etd",
      render: () => (
        <div className="li">
          <span className="li__label is-required">ETD</span>
          <div className="li__input">
            <Controller
              control={control}
              name="etd"
              render={({ field }) => (
                <DateBox
                  variant="panel"
                  required
                  ref={field.ref}
                  name={field.name}
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          </div>
        </div>
      ),
    },
    {
      key: "eta",
      render: () => (
        <div className="li">
          <span className="li__label is-required">ETA</span>
          <div className="li__input">
            <Controller
              control={control}
              name="eta"
              render={({ field }) => (
                <DateBox
                  variant="panel"
                  required
                  ref={field.ref}
                  name={field.name}
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          </div>
        </div>
      ),
    },
    {
      key: "on-board",
      render: () => (
        <div className="li">
          <span className="li__label">On Board</span>
          <div className="li__input">
            <Controller
              control={control}
              name="seaDetail.onboardDate"
              render={({ field }) => (
                <DateBox
                  variant="panel"
                  ref={field.ref}
                  name={field.name}
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          </div>
        </div>
      ),
    },
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
      ? [{ key: "issue", label: "Issue Information", render: () => <IssueSection issueFields={variant.issueFields} panelScope={panelScope} noOfBlOptions={noOfBlOptions} noOfBlPlaceholder={noOfBlPlaceholder} /> }]
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
