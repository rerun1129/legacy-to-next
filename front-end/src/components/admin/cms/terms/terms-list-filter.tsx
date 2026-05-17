"use client";

import type { UseFormReturn } from "react-hook-form";
import type { TermsFilter } from "@/domain/terms";

interface Props {
  form: UseFormReturn<TermsFilter>;
}

const TYPE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "TOS", label: "서비스 이용약관" },
  { value: "PRIVACY", label: "개인정보처리방침" },
  { value: "MARKETING", label: "마케팅 수신동의" },
] as const;

const SCOPE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "DELETED", label: "삭제됨" },
] as const;

export function TermsListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">약관 유형</span>
            <select className="text-box text-box--panel" {...register("type")}>
              {TYPE_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
          <div className="lcn">
            <span className="lcn__label">상태</span>
            <select className="text-box text-box--panel" {...register("scope")}>
              {SCOPE_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
          <div className="lcn">
            <span className="lcn__label">버전</span>
            <input
              type="number"
              className="text-box text-box--panel"
              placeholder="전체"
              min={1}
              {...register("version", { setValueAs: (v) => (v === "" ? "" : Number(v)) })}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">요약</span>
            <input
              className="text-box text-box--panel"
              placeholder="요약 (부분일치)"
              {...register("summary")}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
