"use client";

import type { UseFormReturn } from "react-hook-form";
import type { PartnerFilter } from "@/domain/partner";

interface Props {
  form: UseFormReturn<PartnerFilter>;
}

const PARTNER_TYPE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "FORWARDER", label: "FORWARDER" },
  { value: "SHIPPER", label: "SHIPPER" },
  { value: "CONSIGNEE", label: "CONSIGNEE" },
  { value: "CARRIER", label: "CARRIER" },
  { value: "AGENT", label: "AGENT" },
  { value: "CUSTOMS_BROKER", label: "CUSTOMS_BROKER" },
] as const;

const SCOPE_OPTIONS = [
  { value: "ALL", label: "전체" },
  { value: "ACTIVE", label: "활성" },
  { value: "INACTIVE", label: "비활성" },
  { value: "DELETED", label: "삭제됨" },
] as const;

export function PartnerListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">협력사 코드</span>
            <input
              className="text-box text-box--panel"
              placeholder="협력사 코드"
              {...register("partnerCode")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">협력사명</span>
            <input
              className="text-box text-box--panel"
              placeholder="협력사명 (부분일치)"
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">구분</span>
            <select className="text-box text-box--panel" {...register("partnerType")}>
              {PARTNER_TYPE_OPTIONS.map((o) => (
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
