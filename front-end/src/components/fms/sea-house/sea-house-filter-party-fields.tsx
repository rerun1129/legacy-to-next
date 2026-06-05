"use client";

import { Controller } from "react-hook-form";
import type { Control, UseFormRegister, UseFormSetValue } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { LcnLabel } from "@/components/shared/inputs/lcn-label";
import type { SeaHouseFilter } from "@/domain/sea-house";
import type { LabelOption, CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import type { useTranslations } from "next-intl";

interface AutocompleteState {
  onSearch: (q: string) => void;
  suggestions: CodeBoxSuggestion[];
  suggestionsLoading: boolean;
}

interface Props {
  control: Control<SeaHouseFilter>;
  register: UseFormRegister<SeaHouseFilter>;
  setValue: UseFormSetValue<SeaHouseFilter>;
  t: ReturnType<typeof useTranslations>;
  masterBlKindOptions: LabelOption[];
  partyKindOptions: LabelOption[];
  partnerKindOptions: LabelOption[];
  party: AutocompleteState;
  actualCustomer: AutocompleteState;
  partner: AutocompleteState;
}

export function SeaHouseFilterPartyFields({
  control,
  register,
  setValue,
  t,
  masterBlKindOptions,
  partyKindOptions,
  partnerKindOptions,
  party,
  actualCustomer,
  partner,
}: Props) {
  return (
    <>
      {/* 2. Master B/L No / Master Reference No. */}
      <Controller
        control={control}
        name="masterBlKind"
        render={({ field: kindField }) => (
          <div className="lcn">
            <LcnLabel
              options={masterBlKindOptions}
              value={kindField.value}
              onChange={kindField.onChange}
            />
            <input
              {...register("masterBlValue")}
              placeholder="No"
              className="lcn__name"
            />
          </div>
        )}
      />

      {/* 3. House B/L No */}
      <div className="lcn">
        <span className="lcn__label">{t("hblNo")}</span>
        <input
          {...register("hblNo")}
          placeholder="House B/L No"
          className="lcn__name"
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* 4. Shipper/Consignee/Notify */}
      <Controller
        control={control}
        name="partyKind"
        render={({ field: kindField }) => (
          <CodeBox
            kind="lcn"
            labelOptions={partyKindOptions}
            labelValue={kindField.value}
            onLabelChange={kindField.onChange}
            codeProps={{ ...register("partyCode"), placeholder: "Code" }}
            nameProps={{ ...register("partyName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={party.onSearch}
            suggestions={party.suggestions}
            suggestionsLoading={party.suggestionsLoading}
            onSelect={(it) => {
              setValue("partyCode", it.code);
              setValue("partyName", it.name);
            }}
          />
        )}
      />

      {/* 5. Actual Customer */}
      <CodeBox
        kind="lcn"
        label={t("actualCustomer")}
        codeProps={{ ...register("actualCustomerCode"), placeholder: "Code" }}
        nameProps={{ ...register("actualCustomerName"), placeholder: "Name" }}
        onLookup={() => {}}
        onSearch={actualCustomer.onSearch}
        suggestions={actualCustomer.suggestions}
        suggestionsLoading={actualCustomer.suggestionsLoading}
        onSelect={(it) => {
          setValue("actualCustomerCode", it.code);
          setValue("actualCustomerName", it.name);
        }}
      />

      {/* 6. Settle Partner / Doc Partner */}
      <Controller
        control={control}
        name="partnerKind"
        render={({ field: kindField }) => (
          <CodeBox
            kind="lcn"
            labelOptions={partnerKindOptions}
            labelValue={kindField.value ?? ""}
            onLabelChange={kindField.onChange}
            codeProps={{ ...register("partnerCode"), placeholder: "Code" }}
            nameProps={{ ...register("partnerName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={partner.onSearch}
            suggestions={partner.suggestions}
            suggestionsLoading={partner.suggestionsLoading}
            onSelect={(it) => {
              setValue("partnerCode", it.code);
              setValue("partnerName", it.name);
            }}
          />
        )}
      />
    </>
  );
}
