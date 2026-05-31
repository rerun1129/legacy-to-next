"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import type { CustomerFilter } from "@/domain/customer";
import { customerUseCases } from "@/application/customer/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<CustomerFilter>;
}

export function CustomerListFilter({ form }: Props) {
  const t = useTranslations("admin.customer.filter");
  useListFilterSync(form, "/admin/customer/list");
  const { register, setValue } = form;

  const customerTypeOptions = [
    { value: "ALL", label: t("typeAll") },
    { value: "CUSTOMER", label: "CUSTOMER" },
    { value: "PARTNER", label: "PARTNER" },
    { value: "AIRCARRIER", label: "AIRCARRIER" },
    { value: "LINER", label: "LINER" },
    { value: "TRUCKER", label: "TRUCKER" },
    { value: "WAREHOUSE", label: "WAREHOUSE" },
    { value: "OTHER", label: "OTHER" },
  ];

  const scopeOptions = [
    { value: "ALL", label: t("all") },
    { value: "ACTIVE", label: t("active") },
    { value: "INACTIVE", label: t("inactive") },
    { value: "DELETED", label: t("deleted") },
  ];

  const [acQuery, setAcQuery] = useState("");
  const { data: suggestions = [] } = useQuery({
    queryKey: ["admin-customer", "autocomplete", acQuery],
    queryFn: () => customerUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("customerCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label={t("customerCode")}
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: t("customerCodePlaceholder"), ...register("customerCode") }}
          />
          <div className="lcn">
            <span className="lcn__label">{t("name")}</span>
            <input
              className="box-panel"
              placeholder={t("namePlaceholder")}
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("type")}</span>
            <Controller
              name="customerType"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={customerTypeOptions}
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
        </div>
      </div>
    </div>
  );
}
