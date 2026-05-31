"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { TextBox, NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function NonBLCargoPanel() {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tf = useTranslations("fms.nonBl.entry.fields");
  const tp = useTranslations("fms.nonBl.entry.panels");

  const { register, control, setValue } = useFormContext<NonBlFormValues>();
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");
  const pkgUnit  = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  const hsCodeAc = useCodeAutocomplete(CODE_SOURCES.hsCode);

  const cargoItems: FieldItemDef[] = [
    {
      key: "main-item",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("mainItem")}</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Main Item" {...register("mainItem")} />
          </div>
        </div>
      ),
    },
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("package")}</span>
          <div className="li__input li__input--tight">
            <NumberBox
              name="cargoQty"
              variant="panel"
              decimalPlaces={0}
              placeholder="0"
            />
            <div style={{ flex: "0 0 80px" }}>
              <CodeBox
                kind="code-only"
                variant="panel"
                codeProps={register("pkgUnit")}
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
          <span className="li__label">{tf("grossWt")}</span>
          <div className="li__input li__input--tight">
            <NumberBox name="grossWt" variant="panel" decimalPlaces={3} />
            <Controller
              name="weightUnit"
              control={control}
              render={({ field }) => (
                <ComboBox variant="panel" options={weightUnitOptions} value={field.value} onChange={field.onChange} />
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
            <NumberBox name="volWt" variant="panel" decimalPlaces={3} />
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
            <NumberBox name="totalCbm" variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
    {
      key: "rton",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("rton")}</span>
          <div className="li__input">
            <NumberBox name="rton" variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex nonbl-cargo-panel">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("cargo")}</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldItemGrid itemScope="nonbl-cargo-panel" items={cargoItems} />
        <FieldItemGrid
          itemScope="nonbl-cargo-panel.hs"
          items={[{
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
          }]}
          cols={1}
        />
      </div>
    </div>
  );
}
