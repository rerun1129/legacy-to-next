"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import type { NoticeFilter } from "@/domain/notice";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<NoticeFilter>;
}

export function NoticeListFilter({ form }: Props) {
  const t = useTranslations("admin.notice.filter");
  useListFilterSync(form, "/admin/notice/list");
  const { register } = form;

  const pinnedOptions = [
    { value: "ALL", label: t("all") },
    { value: "PINNED", label: t("pinned") },
    { value: "UNPINNED", label: t("unpinned") },
  ];

  const scopeOptions = [
    { value: "ALL", label: t("all") },
    { value: "ACTIVE", label: t("active") },
    { value: "INACTIVE", label: t("inactive") },
    { value: "DELETED", label: t("deleted") },
  ];

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">{t("title")}</span>
            <input
              className="box-panel"
              placeholder={t("titlePlaceholder")}
              {...register("title")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("pinnedLabel")}</span>
            <Controller
              name="pinned"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={pinnedOptions}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("status")}</span>
            <Controller
              name="scope"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={scopeOptions}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("publishedOnlyLabel")}</span>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input type="checkbox" {...register("publishedOnly")} />
              {t("publishedCheckbox")}
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}
