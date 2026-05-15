"use client";

import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { BoxVariant } from "@/components/shared/inputs/_types";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

const UNIT_OPTIONS = [
  { value: "KG", label: "Kilogram (KG)" },
  { value: "LB", label: "Pound (LB)" },
  { value: "MT", label: "Metric Ton (MT)" },
];

const MANY_OPTIONS = Array.from({ length: 50 }, (_, i) => ({
  value: `V${String(i).padStart(2, "0")}`,
  label: `Option ${String(i).padStart(2, "0")} — item`,
}));

function ComboBoxEnumDemo({
  variant,
  required,
  readOnly,
  disabled,
}: {
  variant: BoxVariant;
  required: boolean;
  readOnly: boolean;
  disabled: boolean;
}) {
  const { options, placeholder, isLoading } = useEnumOptions("WorkDivision");
  return (
    <ComboBox
      variant={variant}
      required={required}
      readOnly={readOnly}
      disabled={disabled || isLoading}
      options={options}
      placeholder={placeholder}
    />
  );
}

// §6.59 — Air House Entry enum ComboBox 시각 카탈로그
function AirEnumComboRow({
  label,
  enumName,
  variant,
  disabled,
}: {
  label: string;
  enumName: string;
  variant: BoxVariant;
  disabled: boolean;
}) {
  const { options, placeholder, isLoading } = useEnumOptions(enumName);
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 6 }}>
      <span style={{ width: 120, fontSize: 10, color: "#555", flexShrink: 0 }}>{label}</span>
      <div style={{ flex: 1 }}>
        <ComboBox
          variant={variant}
          disabled={disabled || isLoading}
          options={options}
          placeholder={placeholder ?? `${label} 선택`}
        />
      </div>
    </div>
  );
}

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function ComboSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  return (
    <>
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>ComboBox — 기본 (타이핑으로 LIKE 검색)</div>
        <ComboBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          options={UNIT_OPTIONS}
          placeholder="단위 선택"
          {...register("unit")}
        />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>ComboBox — 다수 옵션 (50개, 검색 효율)</div>
        <ComboBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          options={MANY_OPTIONS}
          placeholder="옵션 검색..."
        />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>ComboBox — cell variant (그리드 셀 시뮬레이션)</div>
        <div style={{ display: "grid", gridTemplateColumns: "120px 120px", border: "1px solid var(--border)" }}>
          <div style={{ borderRight: "1px solid var(--border)", padding: 0, height: 24 }}>
            <ComboBox variant="cell" options={UNIT_OPTIONS} placeholder="선택" />
          </div>
          <div style={{ padding: 0, height: 24 }}>
            <ComboBox variant="cell" options={UNIT_OPTIONS} placeholder="선택" />
          </div>
        </div>
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>ComboBox — useEnumOptions (ENUM 바인딩)</div>
        <ComboBoxEnumDemo variant={variant} required={required} readOnly={readOnly} disabled={disabled} />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 8 }}>ComboBox — Air House Entry ENUMs (§6.59 시각 검증)</div>
        <AirEnumComboRow label="WeightUnit"       enumName="WeightUnit"       variant={variant} disabled={disabled} />
        <AirEnumComboRow label="FreightTerm"      enumName="FreightTerm"      variant={variant} disabled={disabled} />
        <AirEnumComboRow label="Per"              enumName="Per"              variant={variant} disabled={disabled} />
        <AirEnumComboRow label="RateClass"        enumName="RateClass"        variant={variant} disabled={disabled} />
        <AirEnumComboRow label="HandlingInfoCode" enumName="HandlingInfoCode" variant={variant} disabled={disabled} />
        <AirEnumComboRow label="SalesClass"       enumName="SalesClass"       variant={variant} disabled={disabled} />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 8 }}>ComboBox — Sea Master Entry ENUMs (Phase 6 시각 검증)</div>
        <AirEnumComboRow label="LoadType"    enumName="LoadType"    variant={variant} disabled={disabled} />
        <AirEnumComboRow label="ServiceTerm" enumName="ServiceTerm" variant={variant} disabled={disabled} />
        <AirEnumComboRow label="BlType"      enumName="BlType"      variant={variant} disabled={disabled} />
      </div>
    </>
  );
}
