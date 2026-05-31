"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import { NumberBox } from "@/components/shared/inputs/number-box";

export interface CurrencyFormRow {
  entityId: number;
  currencyCode: string;
  name: string;
  nameEn: string;
  symbol: string;
  currencyUnit: string;
  active: boolean;
}

export interface FormValues {
  rows: CurrencyFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildCurrencyColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<CurrencyFormRow>[] {
  const activeOptions = [
    { value: "true", label: tOptions("active") },
    { value: "false", label: tOptions("inactive") },
  ];

  return [
    {
      key: "_no",
      label: tCols("no"),
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
      key: "currencyCode",
      label: tCols("currencyCode"),
      width: 130,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.currencyCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
          />
        );
      },
    },
    {
      key: "name",
      label: tCols("name"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.name`)} />
      ),
    },
    {
      key: "nameEn",
      label: tCols("nameEn"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.nameEn`)} />
      ),
    },
    {
      key: "symbol",
      label: tCols("symbol"),
      width: 80,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.symbol`)} />
      ),
    },
    {
      key: "currencyUnit",
      label: tCols("currencyUnit"),
      width: 90,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.currencyUnit`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={field.onChange}
              decimalPlaces={0}
            />
          )}
        />
      ),
    },
    {
      key: "active",
      label: tCols("status"),
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

export function getCurrencyRowClassName(
  row: CurrencyFormRow,
  original: CurrencyFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.symbol !== row.symbol ||
    orig.currencyUnit !== row.currencyUnit ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
