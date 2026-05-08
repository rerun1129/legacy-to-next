"use client";

import { NumberBox } from "@/components/shared/inputs/number-box";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function NumberSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 6 }}>NumberBox</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        <div>
          <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>기본 (정수, step=1)</div>
          <NumberBox
            variant={variant}
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            placeholder="0"
            style={{ width: 160 }}
            {...register("amountInt")}
          />
        </div>
        <div>
          <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>
            decimalPlaces=0 + valueAsNumber:true
          </div>
          <NumberBox
            variant={variant}
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            decimalPlaces={0}
            style={{ width: 160 }}
            placeholder="0"
            {...register("amountIntNumber", { valueAsNumber: true })}
          />
        </div>
        <div>
          <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>decimalPlaces=2 (금액·환율)</div>
          <NumberBox
            variant={variant}
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            decimalPlaces={2}
            style={{ width: 160 }}
            {...register("amountDec2")}
          />
        </div>
        <div>
          <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>decimalPlaces=3 (중량·부피)</div>
          <NumberBox
            variant={variant}
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            decimalPlaces={3}
            style={{ width: 160 }}
            {...register("amountDec3")}
          />
        </div>
      </div>
    </div>
  );
}
