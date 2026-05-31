"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import { NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

function TruckCargoFields() {
  // Rules of Hooks: unconditionally at top
  const tf = useTranslations("fms.truckBl.entry.fields");
  const { register, control, setValue } = useFormContext<TruckBlFormValues>();
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");
  const pkgUnit = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  const hsCode  = useCodeAutocomplete(CODE_SOURCES.hsCode);

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("package")}</span>
          <div className="li__input li__input--tight">
            <NumberBox name="pkgQty" variant="panel" decimalPlaces={0} placeholder="0" />
            {/* pkgUnit: Non B/L §10 정책 준용 — CodeBox code-only (자유 텍스트) */}
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
      key: "gw",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("grossWt")}</span>
          <div className="li__input li__input--tight">
            <NumberBox name="grossWeightKg" variant="panel" decimalPlaces={3} />
            <Controller
              name="weightUnit"
              control={control}
              render={({ field }) => (
                <ComboBox variant="panel" options={weightUnitOptions} value={field.value} onChange={field.onChange} style={{ flex: "0 0 80px" }} />
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
            <NumberBox name="cbm" variant="panel" decimalPlaces={3} />
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
            <NumberBox name="chargeWeightKg" variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
  ];

  const HS_CODE_ITEMS: FieldItemDef[] = [
    {
      key: "hs-code",
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
    <>
      <FieldItemGrid itemScope="truck-cargo-panel" items={CARGO_ITEMS} />
      <FieldItemGrid itemScope="truck-cargo-panel.hs" items={HS_CODE_ITEMS} cols={1} />
    </>
  );
}

export function TruckCargoPanel() {
  // Rules of Hooks: unconditionally at top
  const tp = useTranslations("fms.truckBl.entry.panels");

  return (
    <div className="panel truck-cargo-panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tp("cargo")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <TruckCargoFields />
      </div>
    </div>
  );
}
