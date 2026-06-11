"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { GroupFilterValues } from "./common-code-filter-types";

const MODULE_OPTIONS_KEY = ["ALL", "FMS", "BMS", "PMS"] as const;

interface Props {
  form: UseFormReturn<GroupFilterValues>;
}

export function CommonCodeGroupFilter({ form }: Props) {
  const t = useTranslations("admin.commonCode.filter");
  const { register } = form;

  const moduleOptions = MODULE_OPTIONS_KEY.map((v) => ({
    value: v,
    label: v === "ALL" ? t("all") : v,
  }));

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">{t("groupCode")}</span>
            <input
              className="box-panel"
              placeholder={t("groupCodePlaceholder")}
              {...register("groupCode")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("module")}</span>
            <Controller
              name="module"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={moduleOptions}
                  value={field.value}
                  onChange={(e) => field.onChange(e.target.value)}
                />
              )}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
