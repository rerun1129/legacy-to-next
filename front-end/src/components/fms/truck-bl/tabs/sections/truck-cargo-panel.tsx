"use client";

import { useFormContext, Controller } from "react-hook-form";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import { NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

function TruckCargoFields() {
  const { register, control } = useFormContext<TruckBlFormValues>();
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input li__input--tight">
            <NumberBox name="pkgQty" variant="panel" decimalPlaces={0} placeholder="0" />
            {/* pkgUnit: Non B/L §10 정책 준용 — CodeBox code-only (자유 텍스트) */}
            <div style={{ flex: "0 0 80px" }}>
              <CodeBox kind="code-only" variant="panel" codeProps={{ ...register("pkgUnit") }} onLookup={() => {}} />
            </div>
          </div>
        </div>
      ),
    },
    {
      key: "gw",
      render: () => (
        <div className="li">
          <span className="li__label">G/W</span>
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
          <span className="li__label">CBM</span>
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
          <span className="li__label">Charge W/T</span>
          <div className="li__input">
            <NumberBox name="chargeWeightKg" variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
  ];

  return <FieldItemGrid itemScope="truck-cargo-panel" items={CARGO_ITEMS} />;
}

export function TruckCargoPanel() {
  return (
    <div className="panel truck-cargo-panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <TruckCargoFields />
      </div>
    </div>
  );
}
