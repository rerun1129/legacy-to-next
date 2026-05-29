"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import type { CodeMasterFilter } from "@/domain/code-master";
import { codeMasterUseCases } from "@/application/code-master/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

interface Props {
  form: UseFormReturn<CodeMasterFilter>;
}

const ACTIVE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
] as const;

export function CodeMasterListFilter({ form }: Props) {
  const { register, setValue } = form;

  const [acQuery, setAcQuery] = useState("");
  const { data: suggestions = [] } = useQuery({
    queryKey: ["admin-code-master", "autocomplete", acQuery],
    queryFn: () => codeMasterUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("masterCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label="Master Code"
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: "Master Code", ...register("masterCode") }}
          />
          <div className="lcn">
            <span className="lcn__label">Master Name</span>
            <input
              className="box-panel"
              placeholder="Master Name"
              {...register("masterName")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Status</span>
            <Controller
              name="active"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...ACTIVE_OPTIONS]}
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
