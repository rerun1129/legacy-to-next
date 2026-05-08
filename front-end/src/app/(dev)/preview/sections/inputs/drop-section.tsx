"use client";

import { DropBox } from "@/components/shared/inputs/drop-box";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

const UNIT_OPTIONS = [
  { value: "KG", label: "KG" },
  { value: "LB", label: "LB" },
  { value: "MT", label: "MT" },
];

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
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox</div>
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
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox (useEnumOptions)</div>
        <DropBoxEnumDemo variant={variant} required={required} readOnly={readOnly} disabled={disabled} />
      </div>
    </>
  );
}
