"use client";

import type { UseFormReturn } from "react-hook-form";
import type { NoticeFilter } from "@/domain/notice";

interface Props {
  form: UseFormReturn<NoticeFilter>;
}

const PINNED_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "PINNED", label: "고정" },
  { value: "UNPINNED", label: "미고정" },
] as const;

const SCOPE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "INACTIVE", label: "비활성" },
  { value: "DELETED", label: "삭제됨" },
] as const;

export function NoticeListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">제목</span>
            <input
              className="text-box text-box--panel"
              placeholder="제목 (부분일치)"
              {...register("title")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">고정 여부</span>
            <select className="text-box text-box--panel" {...register("pinned")}>
              {PINNED_OPTIONS.map((o) => (
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
            <span className="lcn__label">게시 중만</span>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input type="checkbox" {...register("publishedOnly")} />
              게시 중
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}
