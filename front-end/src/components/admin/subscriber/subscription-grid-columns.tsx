"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { ComboBox, DateBox } from "@/components/shared/inputs";

export interface SubscriptionFormRow {
  entityId: number;
  moduleCode: string;
  startDate: string;
  endDate: string;
  active: boolean;
}

export interface SubscriptionFormValues {
  rows: SubscriptionFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

interface ModuleOption {
  value: string;
  label: string;
}

export function buildSubscriptionColumns(
  register: UseFormRegister<SubscriptionFormValues>,
  control: Control<SubscriptionFormValues>,
  moduleOptions: ModuleOption[],
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<SubscriptionFormRow>[] {
  const activeOptions = [
    { value: "true", label: tOptions("active") },
    { value: "false", label: tOptions("inactive") },
  ];

  return [
    {
      key: "_no",
      label: "#",
      width: 36,
      className: "row-num",
      render: (_v, _row, i) => (
        <>
          <input type="hidden" {...register(`rows.${i}.entityId`, { valueAsNumber: true })} />
          {i + 1}
        </>
      ),
    },
    {
      key: "moduleCode",
      label: tCols("colModule"),
      width: 180,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.moduleCode`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={moduleOptions}
              value={field.value}
              onChange={(e) => field.onChange(e.target.value)}
            />
          )}
        />
      ),
    },
    {
      key: "startDate",
      label: tCols("colStartDate"),
      width: 140,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.startDate`}
          control={control}
          render={({ field }) => (
            <DateBox
              variant="cell"
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
            />
          )}
        />
      ),
    },
    {
      key: "endDate",
      label: tCols("colEndDate"),
      width: 140,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.endDate`}
          control={control}
          render={({ field }) => (
            <DateBox
              variant="cell"
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
            />
          )}
        />
      ),
    },
    {
      key: "active",
      label: tCols("colStatus"),
      width: 100,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.active`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={activeOptions}
              value={String(field.value)}
              onChange={(e) => field.onChange(e.target.value === "true")}
            />
          )}
        />
      ),
    },
  ];
}
