"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { NumberBox, CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

interface Props { variant?: AnyVariantConfig }

export function MasterAirCargoPanel({ variant }: Props) {
  const { register, control, setValue } = useFormContext<MasterBlFormValues>();
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");
  const panelScope = variant ? `master-air-cargo-panel.${variant.key}` : "master-air-cargo-panel";
  const { options: weightUnitOptions }                               = useEnumOptions("WeightUnit");
  const { options: rateClassOptions, placeholder: rateClassPlaceholder } = useEnumOptions("RateClass");
  const pkgUnit  = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  const hsCodeAc = useCodeAutocomplete(CODE_SOURCES.hsCode);

  const cargoItems: FieldItemDef[] = [
    {
      key: "packages",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("package")}</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={0} placeholder="0" {...register("pkgQty")} />
            {/* pkgUnit: 자유 텍스트(비표준 단위 가능) */}
            <div style={{ flex: "0 0 80px" }}>
              <CodeBox
                kind="code-only"
                variant="panel"
                codeProps={{ ...register("pkgUnit") }}
                onLookup={() => {}}
                onSearch={pkgUnit.onSearch}
                suggestions={pkgUnit.suggestions}
                suggestionsLoading={pkgUnit.suggestionsLoading}
                onSelect={(it) => { setValue("pkgUnit", it.code); }}
              />
            </div>
          </div>
        </div>
      ),
    },
    {
      key: "gross-wt",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("grossWT")}</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={3} {...register("grossWeightKg")} />
            <Controller
              name="weightUnit"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={weightUnitOptions}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  style={{ flex: "0 0 80px" }}
                />
              )}
            />
          </div>
        </div>
      ),
    },
    {
      key: "volume-wt",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("volumeWt")}</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("airDetail.volumeWeightKg")} />
          </div>
        </div>
      ),
    },
    {
      key: "charge-wt",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("chargeWt")}</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("airDetail.chargeWeightKg")} />
          </div>
        </div>
      ),
    },
    {
      key: "rate-class",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("rateClass")}</span>
          <div className="li__input">
            <Controller
              name="airDetail.rateClass"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={rateClassOptions}
                  placeholder={rateClassPlaceholder}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
      ),
    },
    {
      key: "cbm",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("cbm")}</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("cbm")} />
          </div>
        </div>
      ),
    },
    {
      key: "hs-code",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label={tf("hsCode")}
          codeProps={{ ...register("hsCode"), placeholder: "Code" }}
          nameProps={{ ...register("hsCodeName"), placeholder: "HS Code Name" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={hsCodeAc.onSearch}
          suggestions={hsCodeAc.suggestions}
          suggestionsLoading={hsCodeAc.suggestionsLoading}
          onSelect={(it) => { setValue("hsCode", it.code); setValue("hsCodeName", it.name); }}
        />
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex air-cargo-panel">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("cargo")}</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldItemGrid itemScope={`${panelScope}.cargo`} items={cargoItems} cols={2} />
      </div>
    </div>
  );
}
