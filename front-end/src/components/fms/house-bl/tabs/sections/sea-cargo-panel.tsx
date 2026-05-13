"use client";

import { useFormContext, Controller } from "react-hook-form";
import { TextBox, NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function SeaCargoPanel() {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={0} placeholder="0" {...register("pkgQty")} />
            {/* pkgUnit: §6.14 정책 — 자유 텍스트(비표준 단위 가능) */}
            <div style={{ flex: "0 0 80px" }}>
              <CodeBox kind="code-only" variant="panel" codeProps={{ ...register("pkgUnit") }} onLookup={() => {}} />
            </div>
          </div>
        </div>
      ),
    },
    {
      key: "gross-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Gross W/T</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={3} {...register("grossWeightKg")} />
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
          <span className="li__label">CBM</span>
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
          <span className="li__label">R/Ton</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("seaDetail.rton")} />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldItemGrid itemScope="sea-cargo-panel" items={CARGO_ITEMS} />
      </div>
    </div>
  );
}
