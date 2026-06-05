"use client";

import { Controller } from "react-hook-form";
import type { Control, UseFormRegister, UseFormSetValue } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { SeaHouseFilter } from "@/domain/sea-house";
import type { LabelOption, CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import type { useTranslations } from "next-intl";

interface AutocompleteState {
  onSearch: (q: string) => void;
  suggestions: CodeBoxSuggestion[];
  suggestionsLoading: boolean;
}

interface EnumState {
  options: LabelOption[];
  isLoading: boolean;
  placeholder: string | undefined;
}

interface Props {
  control: Control<SeaHouseFilter>;
  register: UseFormRegister<SeaHouseFilter>;
  setValue: UseFormSetValue<SeaHouseFilter>;
  t: ReturnType<typeof useTranslations>;
  salesClassOptionsWithAll: LabelOption[];
  incotermsOptionsWithAll: LabelOption[];
  loadTypeOptionsWithAll: LabelOption[];
  salesClass: EnumState;
  incoterms: EnumState;
  loadType: EnumState;
  salesMan: AutocompleteState;
}

export function SeaHouseFilterClassFields({
  control,
  register,
  setValue,
  t,
  salesClassOptionsWithAll,
  incotermsOptionsWithAll,
  loadTypeOptionsWithAll,
  salesClass,
  incoterms,
  loadType,
  salesMan,
}: Props) {
  return (
    <>
      {/* 14. Sales Class */}
      <div className="lcn">
        <span className="lcn__label">{t("salesClass")}</span>
        <Controller
          control={control}
          name="salesClass"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={salesClassOptionsWithAll}
              disabled={salesClass.isLoading}
              placeholder={salesClass.placeholder}
              style={{ gridColumn: "2 / span 2" }}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
            />
          )}
        />
      </div>

      {/* 15. Sales Man */}
      <CodeBox
        kind="lcn"
        label={t("salesMan")}
        codeProps={{ ...register("salesManCode"), placeholder: "Code" }}
        nameProps={{ ...register("salesManName"), placeholder: "Name" }}
        onLookup={() => {}}
        onSearch={salesMan.onSearch}
        suggestions={salesMan.suggestions}
        suggestionsLoading={salesMan.suggestionsLoading}
        onSelect={(it) => {
          setValue("salesManCode", it.code);
          setValue("salesManName", it.name);
        }}
      />

      {/* 16. Incoterms */}
      <div className="lcn">
        <span className="lcn__label">{t("incoterms")}</span>
        <Controller
          control={control}
          name="incoterms"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={incotermsOptionsWithAll}
              disabled={incoterms.isLoading}
              placeholder={incoterms.placeholder}
              style={{ gridColumn: "2 / span 2" }}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
            />
          )}
        />
      </div>

      {/* 17. Load Type */}
      <div className="lcn">
        <span className="lcn__label">{t("loadType")}</span>
        <Controller
          control={control}
          name="loadType"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={loadTypeOptionsWithAll}
              disabled={loadType.isLoading}
              placeholder={loadType.placeholder}
              style={{ gridColumn: "2 / span 2" }}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
            />
          )}
        />
      </div>
    </>
  );
}
