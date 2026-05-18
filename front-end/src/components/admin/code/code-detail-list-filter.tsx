"use client";

import type { UseFormReturn } from "react-hook-form";
import type { CodeDetailFilter } from "@/domain/code-detail";

interface Props {
  form: UseFormReturn<CodeDetailFilter>;
  disabled?: boolean;
}

const ACTIVE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "INACTIVE", label: "비활성" },
] as const;

export function CodeDetailListFilter({ form, disabled }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">코드 값</span>
            <input
              className="text-box text-box--panel"
              placeholder="코드 값"
              disabled={disabled}
              {...register("codeValue")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">코드 라벨</span>
            <input
              className="text-box text-box--panel"
              placeholder="코드 라벨"
              disabled={disabled}
              {...register("codeLabel")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">활성 여부</span>
            <select
              className="text-box text-box--panel"
              disabled={disabled}
              {...register("active")}
            >
              {ACTIVE_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
    </div>
  );
}
