"use client";

import type { UseFormReturn } from "react-hook-form";
import type { UserFilter } from "@/domain/user";

interface Props {
  form: UseFormReturn<UserFilter>;
}

const ROLE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ADMIN", label: "ADMIN" },
  { value: "USER", label: "USER" },
] as const;

const SCOPE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "INACTIVE", label: "비활성" },
  { value: "DELETED", label: "삭제됨" },
] as const;

export function UserListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">사용자명</span>
            <input
              className="text-box text-box--panel"
              placeholder="사용자명"
              {...register("username")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">역할</span>
            <select className="text-box text-box--panel" {...register("role")}>
              {ROLE_OPTIONS.map((o) => (
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
        </div>
      </div>
    </div>
  );
}
