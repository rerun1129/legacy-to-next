"use client";

import { useState } from "react";
import { MultiSelectBox } from "@/components/shared/inputs/multi-select-box";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

const MODULE_OPTIONS = [
  { value: "ADMIN", label: "Admin Console" },
  { value: "FMS", label: "FMS" },
];

export function MultiSelectSection({ variant, required, readOnly, disabled }: SectionProps) {
  const [selected, setSelected] = useState<string[]>(["ADMIN"]);

  return (
    <>
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>
          MultiSelectBox — 기본 (체크박스 멀티 선택, 드롭다운 유지)
        </div>
        <MultiSelectBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          options={MODULE_OPTIONS}
          value={selected}
          onChange={setSelected}
          placeholder="모듈 선택"
        />
        <div style={{ marginTop: 6, fontSize: 10, color: "#666" }}>
          선택값: [{selected.join(", ")}]
        </div>
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>
          MultiSelectBox — cell variant (그리드 셀 시뮬레이션)
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "200px 200px", border: "1px solid var(--border)" }}>
          <div style={{ borderRight: "1px solid var(--border)", padding: 0, height: 24 }}>
            <MultiSelectBox
              variant="cell"
              options={MODULE_OPTIONS}
              value={selected}
              onChange={setSelected}
              placeholder="모듈 선택"
            />
          </div>
          <div style={{ padding: 0, height: 24 }}>
            <MultiSelectBox
              variant="cell"
              options={MODULE_OPTIONS}
              value={[]}
              onChange={() => undefined}
              placeholder="(비어있음)"
            />
          </div>
        </div>
      </div>
    </>
  );
}
