"use client";

// SEA Master Schedule 패널 개별 원자 필드 컴포넌트
// master-schedule-sea-fields.tsx에서 분리 (300줄 초과 기준, CLAUDE.md Critical)
import { useFormContext, Controller } from "react-hook-form";
import { TextBox }  from "@/components/shared/inputs/text-box";
import { CodeBox }  from "@/components/shared/inputs/code-box";
import { DateBox }  from "@/components/shared/inputs/date-box";
import type { MasterBlFormValues } from "../../master-bl-schema";

// ── Liner / Vessel / Voyage ─────────────────────────────────────────────────
export function LinerLcnField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="Liner"
      required
      codeProps={{ ...register("seaDetail.linerCode"), placeholder: "Code" }}
      nameProps={{ placeholder: "Liner Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

export function VesselField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <div className="li">
      <span className="li__label is-required">Vessel</span>
      <div className="li__input">
        <TextBox variant="panel" {...register("seaDetail.vesselName")} />
      </div>
    </div>
  );
}

export function VoyageField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <div className="li">
      <span className="li__label is-required">Voyage</span>
      <div className="li__input">
        <TextBox variant="panel" {...register("seaDetail.voyageNo")} />
      </div>
    </div>
  );
}

// ── ETD / ETA ──────────────────────────────────────────────────────────────
export function EtdField() {
  const { control } = useFormContext<MasterBlFormValues>();
  return (
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
  );
}

export function EtaField() {
  const { control } = useFormContext<MasterBlFormValues>();
  return (
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
  );
}

export function IssueDateField() {
  const { control } = useFormContext<MasterBlFormValues>();
  return (
    <div className="li">
      <span className="li__label">Issue Date</span>
      <div className="li__input">
        <Controller
          control={control}
          name="seaDetail.issueDate"
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
  );
}

// ── Port lcn 필드 (POR/POL/POD/FinalDest) ─────────────────────────────────
// schema에 name 필드 없음 → nameProps는 placeholder만 (LinerLcnField와 동일 패턴)
export function PorField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="POR"
      codeProps={{ ...register("seaDetail.porCode"), placeholder: "UNLOC" }}
      nameProps={{ placeholder: "Port Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

export function PolField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="POL"
      required
      codeProps={{ ...register("polCode"), placeholder: "UNLOC" }}
      nameProps={{ placeholder: "Port Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

export function PodField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="POD"
      required
      codeProps={{ ...register("podCode"), placeholder: "UNLOC" }}
      nameProps={{ placeholder: "Port Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

export function FinalDestField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="Final Dest."
      codeProps={{ ...register("seaDetail.finalDestCode"), placeholder: "UNLOC" }}
      nameProps={{ placeholder: "Port Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

