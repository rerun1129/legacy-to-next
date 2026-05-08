"use client";

import { LinkBox } from "@/components/shared/inputs/link-box";
import { RadioBox } from "@/components/shared/inputs/radio-box";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function LinkRadioSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  return (
    <>
      <div style={sectionStyle}>
        <LinkBox
          label="LinkBox (External URL)"
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          onLink={() => alert("Navigate to: https://docs.example.com")}
          inputProps={{ placeholder: "Display Name", ...register("linkUrl") }}
        />
      </div>

      <div style={sectionStyle}>
        <LinkBox
          label="LinkBox (Menu Route)"
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          onLink={() => alert("Navigate to: /admin/user-management")}
          inputProps={{ placeholder: "Display Name", ...register("linkMenu") }}
        />
      </div>

      <div style={sectionStyle}>
        <RadioBox
          label="RadioBox (Mode)"
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          options={[
            { value: "A", label: "Option A" },
            { value: "B", label: "Option B" },
            { value: "C", label: "Option C" },
          ]}
          {...register("radioMode")}
        />
      </div>
    </>
  );
}
