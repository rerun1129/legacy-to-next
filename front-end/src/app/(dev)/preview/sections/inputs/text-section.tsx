"use client";

import { TextBox } from "@/components/shared/inputs/text-box";
import { TextArea } from "@/components/shared/inputs/text-area";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function TextSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  return (
    <>
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>TextBox</div>
        <TextBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          placeholder="텍스트 입력"
          {...register("text")}
        />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>TextArea (lineNumbers=false)</div>
        <TextArea
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          placeholder="텍스트 에리어"
          rows={3}
          {...register("area")}
        />
        <div style={{ fontWeight: 600, margin: "10px 0 6px" }}>TextArea (lineNumbers=true)</div>
        <TextArea
          lineNumbers
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          style={{ minHeight: 80 }}
          placeholder="줄 번호 포함 텍스트 에리어"
          {...register("area")}
        />
      </div>
    </>
  );
}
