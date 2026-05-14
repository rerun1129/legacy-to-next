"use client";

import { useFormContext, Controller } from "react-hook-form";
import { NumberBox, CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

interface Props { variant?: AnyVariantConfig }

export function AirCargoPanel({ variant }: Props) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const panelScope = variant ? `air-cargo-panel.${variant.key}` : "air-cargo-panel";
  const { options: weightUnitOptions }                               = useEnumOptions("WeightUnit");
  const { options: rateClassOptions, placeholder: rateClassPlaceholder } = useEnumOptions("RateClass");

  const cargoItems: FieldItemDef[] = [
    {
      key: "packages",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={0} placeholder="0" {...register("pkgQty")} />
            {/* pkgUnit: §6.14 정책 — 자유 텍스트(비표준 단위 가능), SEA 동일 패턴 */}
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
          <span className="li__label">Volume W/T</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("volumeWeightKg")} />
          </div>
        </div>
      ),
    },
    {
      key: "charge-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Charge W/T</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("chargeWeightKg")} />
          </div>
        </div>
      ),
    },
    {
      key: "rate-class",
      render: () => (
        <div className="li">
          <span className="li__label">Rate Class</span>
          <div className="li__input">
            <Controller
              name="rateClass"
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
          <span className="li__label">CBM</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} {...register("cbm")} />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex air-cargo-panel">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldItemGrid itemScope={`${panelScope}.cargo`} items={cargoItems} cols={1} shouldShowRowControls={false} />
      </div>
    </div>
  );
}
