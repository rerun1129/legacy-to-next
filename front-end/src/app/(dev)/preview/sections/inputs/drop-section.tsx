"use client";

import { DropBox } from "@/components/shared/inputs/drop-box";
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

function DropBoxEnumDemo({
  variant,
  required,
  readOnly,
  disabled,
}: {
  variant: "panel" | "cell";
  required: boolean;
  readOnly: boolean;
  disabled: boolean;
}) {
  const { options, placeholder, isLoading } = useEnumOptions("WorkDivision");
  return (
    <DropBox
      variant={variant}
      required={required}
      readOnly={readOnly}
      disabled={disabled || isLoading}
      options={options}
      placeholder={placeholder}
    />
  );
}

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function DropSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  return (
    <>
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox — 기본 (타이핑으로 LIKE 검색)</div>
        <DropBox
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
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox — 다수 옵션 (50개, 검색 효율)</div>
        <DropBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          options={MANY_OPTIONS}
          placeholder="옵션 검색..."
        />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox — cell variant (그리드 셀 시뮬레이션)</div>
        <div style={{ display: "grid", gridTemplateColumns: "120px 120px", border: "1px solid var(--border)" }}>
          <div style={{ borderRight: "1px solid var(--border)", padding: 0, height: 24 }}>
            <DropBox variant="cell" options={UNIT_OPTIONS} placeholder="선택" />
          </div>
          <div style={{ padding: 0, height: 24 }}>
            <DropBox variant="cell" options={UNIT_OPTIONS} placeholder="선택" />
          </div>
        </div>
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox — useEnumOptions (ENUM 바인딩)</div>
        <DropBoxEnumDemo variant={variant} required={required} readOnly={readOnly} disabled={disabled} />
      </div>
    </>
  );
}
