"use client";

import type { UseFormReturn } from "react-hook-form";
import type { CodeFilter } from "@/domain/code";

interface Props {
  form: UseFormReturn<CodeFilter>;
}

const ACTIVE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "INACTIVE", label: "비활성" },
] as const;

export function CodeListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">코드 그룹</span>
            <input
              className="text-box text-box--panel"
              placeholder="코드 그룹"
              {...register("codeGroup")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">코드 값</span>
            <input
              className="text-box text-box--panel"
              placeholder="코드 값"
              {...register("codeValue")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">코드 라벨</span>
            <input
              className="text-box text-box--panel"
              placeholder="코드 라벨"
              {...register("codeLabel")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">활성 여부</span>
            <select className="text-box text-box--panel" {...register("active")}>
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
