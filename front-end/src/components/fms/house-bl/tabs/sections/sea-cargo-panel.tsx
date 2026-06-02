"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function SeaCargoPanel() {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();
  const pkgUnit = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  const hsCode  = useCodeAutocomplete(CODE_SOURCES.hsCode);
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("package")}</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={0} placeholder="0" {...register("pkgQty")} />
            {/* pkgUnit: §6.14 정책 — 자유 텍스트(비표준 단위 가능) */}
            <div style={{ flex: "0 0 60px" }}>
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
          <span className="li__label">{tf("grossWt")}</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={3} {...register("grossWeightKg")} />
            <Controller
              name="weightUnit"
              control={control}
              render={({ field }) => (
                <ComboBox variant="panel" options={weightUnitOptions} value={field.value} onChange={field.onChange} style={{ flex: "0 0 60px" }} />
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
      key: "rton",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("rton")}</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("seaDetail.rton")} />
          </div>
        </div>
      ),
    },
  ];

  const HS_CODE_ITEMS: FieldItemDef[] = [
    {
      key: "hs-code",
      fullWidth: true,
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label={tf("hsCode")}
          codeProps={{ ...register("hsCode") }}
          nameProps={{ ...register("hsCodeName") }}
          onLookup={() => {}}
          onSearch={hsCode.onSearch}
          suggestions={hsCode.suggestions}
          suggestionsLoading={hsCode.suggestionsLoading}
          onSelect={(it) => { setValue("hsCode", it.code); setValue("hsCodeName", it.name); }}
        />
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex sea-cargo-panel">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("cargo")}</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldItemGrid itemScope="sea-cargo-panel" items={CARGO_ITEMS} />
        <FieldItemGrid itemScope="sea-cargo-panel.hs" items={HS_CODE_ITEMS} cols={2} />
      </div>
    </div>
  );
}
