"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { NoticeFilter } from "@/domain/notice";
import { ComboBox } from "@/components/shared/inputs/combo-box";

interface Props {
  form: UseFormReturn<NoticeFilter>;
}

const PINNED_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "PINNED", label: "Pinned" },
  { value: "UNPINNED", label: "Unpinned" },
] as const;

const SCOPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "DELETED", label: "Deleted" },
] as const;

export function NoticeListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">Title</span>
            <input
              className="box-panel"
              placeholder="Title (partial)"
              {...register("title")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Pinned</span>
            <Controller
              name="pinned"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...PINNED_OPTIONS]}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Status</span>
            <Controller
              name="scope"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...SCOPE_OPTIONS]}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Published Only</span>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input type="checkbox" {...register("publishedOnly")} />
              Published
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}
