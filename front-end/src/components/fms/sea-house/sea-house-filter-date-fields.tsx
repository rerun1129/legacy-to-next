"use client";

import { Controller } from "react-hook-form";
import type { Control } from "react-hook-form";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import type { SeaHouseFilter } from "@/domain/sea-house";
import type { LabelOption } from "@/components/shared/inputs/_types";

interface Props {
  control: Control<SeaHouseFilter>;
  dateKindOptions: LabelOption[];
}

export function SeaHouseFilterDateFields({ control, dateKindOptions }: Props) {
  return (
    <>
      {/* 1. ETD/ETA */}
      <Controller
        control={control}
        name="dateKind"
        render={({ field: kindField }) => (
          <Controller
            control={control}
            name="dateFrom"
            render={({ field: fromField }) => (
              <Controller
                control={control}
                name="dateTo"
                render={({ field: toField }) => (
                  <DateRangeBox
                    labelOptions={dateKindOptions}
                    labelValue={kindField.value}
                    onLabelChange={kindField.onChange}
                    required
                    fromProps={{
                      name: fromField.name,
                      value: fromField.value ?? "",
                      onChange: fromField.onChange,
                      onBlur: fromField.onBlur,
                      placeholder: "From",
                    }}
                    toProps={{
                      name: toField.name,
                      value: toField.value ?? "",
                      onChange: toField.onChange,
                      onBlur: toField.onBlur,
                      placeholder: "To",
                    }}
                  />
                )}
              />
            )}
          />
        )}
      />
    </>
  );
}
