import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import { TextBox, CodeBox, DateBox, ComboBox } from "@/components/shared/inputs";
import { type FieldItemDef } from "@/components/widget/field-item-grid";
import { FieldItemGrid } from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

// 라벨 → RHF 필드명 매핑 (Issue Information 섹션)
export const ISSUE_LABEL_TO_FIELD: Record<string, FieldPath<HouseBlFormValues>> = {
  "Issue Date":  "seaDetail.issueDate",
  "No. of B/L":  "seaDetail.noOfBl",
  "Issue Place": "seaDetail.issuePlace",
  "D/O Date":    "seaDetail.doDate",
  "Signature":   "seaDetail.signature",
};

// ── 공통 헬퍼 ──────────────────────────────────────────────
export function SchedField({
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

export function IssueSection({ issueFields, panelScope, noOfBlOptions, noOfBlPlaceholder }: IssueSectionProps) {
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();
  const issuePlace = useCodeAutocomplete(CODE_SOURCES.portSea);
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
            onSearch={issuePlace.onSearch}
            suggestions={issuePlace.suggestions}
            suggestionsLoading={issuePlace.suggestionsLoading}
            onSelect={(it) => { setValue("seaDetail.issuePlace", it.code); setValue("seaDetail.issuePlaceName", it.name); }}
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
