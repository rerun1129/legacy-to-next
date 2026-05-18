"use client";

import type { UseFormReturn } from "react-hook-form";
import type { CodeMasterFilter } from "@/domain/code-master";

interface Props {
  form: UseFormReturn<CodeMasterFilter>;
}

const ACTIVE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "INACTIVE", label: "비활성" },
] as const;

export function CodeMasterListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">마스터 코드</span>
            <input
              className="text-box text-box--panel"
              placeholder="마스터 코드"
              {...register("masterCode")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">마스터 명</span>
            <input
              className="text-box text-box--panel"
              placeholder="마스터 명"
              {...register("masterName")}
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
