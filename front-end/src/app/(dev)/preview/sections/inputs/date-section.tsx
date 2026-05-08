"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import { DateBox, DateRangeBox, LcnLabel } from "@/components/shared/inputs";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function DateSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register, control } = form;
  const [lcnLabelKind, setLcnLabelKind] = useState("ETD");

  return (
    <>
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DateBox</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div>
            <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>register spread</div>
            <DateBox
              required={required}
              readOnly={readOnly}
              disabled={disabled}
              {...register("date")}
            />
          </div>
          <div>
            <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>Controller 사용 예시</div>
            <Controller
              control={control}
              name="date"
              render={({ field }) => (
                <DateBox
                  required={required}
                  readOnly={readOnly}
                  disabled={disabled}
                  ref={field.ref}
                  name={field.name}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          </div>
          <div>
            <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>
              variant=&quot;cell&quot; (Grid 셀용)
            </div>
            <Controller
              control={control}
              name="date"
              render={({ field }) => (
                <DateBox
                  variant="cell"
                  required={required}
                  readOnly={readOnly}
                  disabled={disabled}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>
        </div>
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DateRangeBox</div>
        <DateRangeBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          label="Date"
          fromProps={{ placeholder: "From" }}
          toProps={{ placeholder: "To" }}
        />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>LcnLabel (select)</div>
        <div className="lcn">
          {/* disabled prop 미지원 — Phase 3 가이드 갱신 대상 */}
          <LcnLabel
            options={[{ value: "ETD", label: "ETD" }, { value: "ETA", label: "ETA" }]}
            value={lcnLabelKind}
            onChange={setLcnLabelKind}
            required={required}
          />
          <span style={{ fontSize: 11, color: "#666", gridColumn: "2 / span 2" }}>
            selected: {lcnLabelKind}
          </span>
        </div>
      </div>
    </>
  );
}
