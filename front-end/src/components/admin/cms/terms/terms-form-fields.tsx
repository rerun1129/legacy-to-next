"use client";

import { useForm } from "react-hook-form";
import type { TermsType } from "@/domain/terms";

export interface TermsFormValues {
  type: TermsType;
  version: number | "";
  effectiveAt: string; // "YYYY-MM-DDTHH:mm" or ""
  summary: string;
  content: string;
}

export const TERMS_TYPE_OPTIONS: { value: TermsType; label: string }[] = [
  { value: "TOS", label: "서비스 이용약관" },
  { value: "PRIVACY", label: "개인정보처리방침" },
  { value: "MARKETING", label: "마케팅 수신동의" },
];

export const TERMS_DEFAULT_FORM: TermsFormValues = {
  type: "TOS",
  version: "",
  effectiveAt: "",
  summary: "",
  content: "",
};

interface Props {
  register: ReturnType<typeof useForm<TermsFormValues>>["register"];
  isReadOnly: boolean;
  isEdit: boolean;
}

export function TermsFormFields({ register, isReadOnly, isEdit }: Props) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="lcn">
        <span className="lcn__label">약관 유형 *</span>
        <select
          className="text-box text-box--panel"
          disabled={isEdit}
          {...register("type")}
        >
          {TERMS_TYPE_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>
      <div className="lcn">
        <span className="lcn__label">버전 *</span>
        <input
          type="number"
          className="text-box text-box--panel"
          placeholder="정수 (예: 1)"
          min={1}
          readOnly={isEdit}
          disabled={isEdit}
          {...register("version", { setValueAs: (v) => (v === "" ? "" : Number(v)) })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">적용일시 *</span>
        <input
          type="datetime-local"
          className="text-box text-box--panel"
          readOnly={isReadOnly}
          disabled={isReadOnly}
          {...register("effectiveAt")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">요약</span>
        <input
          className="text-box text-box--panel"
          placeholder="요약 (선택)"
          readOnly={isReadOnly}
          {...register("summary")}
        />
      </div>
      <div className="lcn" style={{ alignItems: "flex-start" }}>
        <span className="lcn__label" style={{ paddingTop: 4 }}>내용 *</span>
        <textarea
          className="text-box text-box--panel"
          rows={12}
          placeholder="약관 본문"
          style={{ resize: "vertical", whiteSpace: "pre-wrap" }}
          readOnly={isReadOnly}
          {...register("content")}
        />
      </div>
    </div>
  );
}
