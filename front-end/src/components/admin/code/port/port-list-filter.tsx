"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import type { PortFilter } from "@/domain/code/port";
import { portUseCases } from "@/application/code/port/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<PortFilter>;
}

const SCOPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "DELETED", label: "Deleted" },
] as const;

const PORT_TYPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "SEA", label: "SEA" },
  { value: "AIR", label: "AIR" },
] as const;

export function PortListFilter({ form }: Props) {
  useListFilterSync(form, "/admin/code/port/list");
  const { register, setValue } = form;

  const [acQuery, setAcQuery] = useState("");
  const { data: suggestions = [] } = useQuery({
    queryKey: ["admin-code-port", "autocomplete", acQuery],
    queryFn: () => portUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("portCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label="Port Code"
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: "Port Code", ...register("portCode") }}
          />
          <div className="lcn">
            <span className="lcn__label">Port Name</span>
            <input
              className="box-panel"
              placeholder="Port Name (partial)"
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Country Code</span>
            <input
              className="box-panel"
              placeholder="Country Code"
              {...register("countryCode")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Type</span>
            <Controller
              name="portType"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...PORT_TYPE_OPTIONS]}
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
        </div>
      </div>
    </div>
  );
}
