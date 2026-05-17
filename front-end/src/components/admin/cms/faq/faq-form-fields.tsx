"use client";

import { useForm } from "react-hook-form";
import type { FaqCategoryRow } from "@/domain/faq-category";

export interface FaqFormValues {
  faqCategoryId: number | "";
  question: string;
  answer: string;
  sortOrder: number | "";
  active: boolean;
}

export const FAQ_DEFAULT_FORM: FaqFormValues = {
  faqCategoryId: "",
  question: "",
  answer: "",
  sortOrder: 0,
  active: true,
};

interface Props {
  register: ReturnType<typeof useForm<FaqFormValues>>["register"];
  isReadOnly: boolean;
  categories: FaqCategoryRow[];
}

export function FaqFormFields({ register, isReadOnly, categories }: Props) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="lcn">
        <span className="lcn__label">카테고리 *</span>
        <select
          className="text-box text-box--panel"
          disabled={isReadOnly}
          {...register("faqCategoryId", { setValueAs: (v) => (v === "" ? "" : Number(v)) })}
        >
          <option value="">선택</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
      <div className="lcn">
        <span className="lcn__label">질문 *</span>
        <input
          className="text-box text-box--panel"
          placeholder="질문"
          readOnly={isReadOnly}
          {...register("question")}
        />
      </div>
      <div className="lcn" style={{ alignItems: "flex-start" }}>
        <span className="lcn__label" style={{ paddingTop: 4 }}>
          답변 *
        </span>
        <textarea
          className="text-box text-box--panel"
          rows={10}
          placeholder="답변 (줄바꿈 보존)"
          style={{ resize: "vertical", whiteSpace: "pre-wrap" }}
          readOnly={isReadOnly}
          {...register("answer")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">정렬순서</span>
        <input
          type="number"
          className="text-box text-box--panel"
          placeholder="0"
          readOnly={isReadOnly}
          disabled={isReadOnly}
          {...register("sortOrder", { setValueAs: (v) => (v === "" ? "" : Number(v)) })}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">활성</span>
        <input
          type="checkbox"
          disabled={isReadOnly}
          {...register("active")}
        />
      </div>
    </div>
  );
}
