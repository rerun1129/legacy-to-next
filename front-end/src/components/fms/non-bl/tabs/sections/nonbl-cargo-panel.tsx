"use client";

import { useFormContext, Controller } from "react-hook-form";
import { TextBox, NumberBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLCargoPanel() {
  const { register, control } = useFormContext<NonBlFormValues>();
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "main-item",
      render: () => (
        <div className="li">
          <span className="li__label">Main Item</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Main Item" {...register("mainItem")} />
          </div>
        </div>
      ),
    },
    {
      key: "hs-code",
      render: () => (
        <div className="li">
          <span className="li__label">HS Code</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="HS Code" {...register("hsCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input li__input--tight">
            <NumberBox
              name="cargoQty"
              variant="panel"
              decimalPlaces={0}
              placeholder="0"
            />
            <Controller
              name="cargoUnit"
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
      key: "gross-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Gross W/T</span>
          <div className="li__input">
            <NumberBox name="grossWt" variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
    {
      key: "volume-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Volume W/T</span>
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
          <span className="li__label">CBM</span>
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
          <span className="li__label">R/Ton</span>
          <div className="li__input">
            <NumberBox name="rton" variant="panel" decimalPlaces={3} />
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
        <FieldItemGrid itemScope="nonbl-cargo-panel" items={CARGO_ITEMS} />
      </div>
    </div>
  );
}
