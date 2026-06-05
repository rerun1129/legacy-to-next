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
  portKindOptions: LabelOption[];
  shipmentTypeOptionsWithAll: LabelOption[];
  shipmentType: EnumState;
  liner: AutocompleteState;
  port: AutocompleteState;
  operator: AutocompleteState;
  team: AutocompleteState;
}

export function SeaHouseFilterLogisticsFields({
  control,
  register,
  setValue,
  t,
  portKindOptions,
  shipmentTypeOptionsWithAll,
  shipmentType,
  liner,
  port,
  operator,
  team,
}: Props) {
  return (
    <>
      {/* 7. Liner */}
      <CodeBox
        kind="lcn"
        label={t("liner")}
        codeProps={{ ...register("linerCode"), placeholder: "Code" }}
        nameProps={{ ...register("linerName"), placeholder: "Name" }}
        onLookup={() => {}}
        onSearch={liner.onSearch}
        suggestions={liner.suggestions}
        suggestionsLoading={liner.suggestionsLoading}
        onSelect={(it) => {
          setValue("linerCode", it.code);
          setValue("linerName", it.name);
        }}
      />

      {/* 8. POL/POD */}
      <Controller
        control={control}
        name="portKind"
        render={({ field: kindField }) => (
          <CodeBox
            kind="lcn"
            labelOptions={portKindOptions}
            labelValue={kindField.value}
            onLabelChange={kindField.onChange}
            codeProps={{ ...register("portCode"), placeholder: "Code" }}
            nameProps={{ ...register("portName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={port.onSearch}
            suggestions={port.suggestions}
            suggestionsLoading={port.suggestionsLoading}
            onSelect={(it) => {
              setValue("portCode", it.code);
              setValue("portName", it.name);
            }}
          />
        )}
      />

      {/* 9. Vessel Name */}
      <div className="lcn">
        <span className="lcn__label">{t("vesselName")}</span>
        <input
          {...register("vesselName")}
          placeholder="Vessel Name"
          className="lcn__name"
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* 10. Voyage */}
      <div className="lcn">
        <span className="lcn__label">{t("voyageNo")}</span>
        <input
          {...register("voyageNo")}
          placeholder="Voyage No"
          className="lcn__name"
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* 11. Shipment Type */}
      <div className="lcn">
        <span className="lcn__label">{t("shipmentType")}</span>
        <Controller
          control={control}
          name="shipmentType"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={shipmentTypeOptionsWithAll}
              disabled={shipmentType.isLoading}
              placeholder={shipmentType.placeholder}
              style={{ gridColumn: "2 / span 2" }}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
            />
          )}
        />
      </div>

      {/* 12. Team */}
      <CodeBox
        kind="lcn"
        label={t("team")}
        codeProps={{ ...register("teamCode"), placeholder: "Code" }}
        nameProps={{ ...register("teamName"), placeholder: "Name" }}
        onLookup={() => {}}
        onSearch={team.onSearch}
        suggestions={team.suggestions}
        suggestionsLoading={team.suggestionsLoading}
        onSelect={(it) => {
          setValue("teamCode", it.code);
          setValue("teamName", it.name);
        }}
      />

      {/* 13. Operator */}
      <CodeBox
        kind="lcn"
        label={t("operator")}
        codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
        nameProps={{ ...register("operatorName"), placeholder: "Name" }}
        onLookup={() => {}}
        onSearch={operator.onSearch}
        suggestions={operator.suggestions}
        suggestionsLoading={operator.suggestionsLoading}
        onSelect={(it) => {
          setValue("operatorCode", it.code);
          setValue("operatorName", it.name);
        }}
      />
    </>
  );
}
