"use client";

import { TimeBox } from "@/components/shared/inputs";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function TimeSection({ form, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 6 }}>TimeBox</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        <div>
          <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>panel variant</div>
          <TimeBox
            variant="panel"
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            {...register("time")}
          />
        </div>
        <div>
          <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>cell variant</div>
          <TimeBox
            variant="cell"
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            {...register("timeCell")}
          />
        </div>
      </div>
    </div>
  );
}
