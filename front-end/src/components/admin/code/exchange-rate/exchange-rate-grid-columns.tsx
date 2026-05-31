"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import { NumberBox } from "@/components/shared/inputs/number-box";

export interface ExchangeRateFormRow {
  entityId: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  exchangeDate: string;
  cashSellExchangeRate: string;
  cashBuyExchangeRate: string;
  wireSendExchangeRate: string;
  wireReceiveExchangeRate: string;
  standardExchangeRate: string;
  name: string;
  nameEn: string;
  active: boolean;
}

export interface FormValues {
  rows: ExchangeRateFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildExchangeRateColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<ExchangeRateFormRow>[] {
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
      key: "fromCurrencyCode",
      label: tCols("fromCurrencyCode"),
      width: 100,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.fromCurrencyCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)" }}
          />
        );
      },
    },
    {
      key: "toCurrencyCode",
      label: tCols("toCurrencyCode"),
      width: 100,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.toCurrencyCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)" }}
          />
        );
      },
    },
    {
      key: "exchangeDate",
      label: tCols("exchangeDate"),
      width: 100,
      render: (_v, _row, i) => (
        <TextBox
          variant="cell"
          {...register(`rows.${i}.exchangeDate`)}
          style={{ fontFamily: "var(--font-mono)" }}
        />
      ),
    },
    {
      key: "cashSellExchangeRate",
      label: tCols("cashSellExchangeRate"),
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.cashSellExchangeRate`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={field.onChange}
              decimalPlaces={4}
            />
          )}
        />
      ),
    },
    {
      key: "cashBuyExchangeRate",
      label: tCols("cashBuyExchangeRate"),
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.cashBuyExchangeRate`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={field.onChange}
              decimalPlaces={4}
            />
          )}
        />
      ),
    },
    {
      key: "wireSendExchangeRate",
      label: tCols("wireSendExchangeRate"),
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.wireSendExchangeRate`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={field.onChange}
              decimalPlaces={4}
            />
          )}
        />
      ),
    },
    {
      key: "wireReceiveExchangeRate",
      label: tCols("wireReceiveExchangeRate"),
      width: 140,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.wireReceiveExchangeRate`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={field.onChange}
              decimalPlaces={4}
            />
          )}
        />
      ),
    },
    {
      key: "standardExchangeRate",
      label: tCols("standardExchangeRate"),
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.standardExchangeRate`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={field.onChange}
              decimalPlaces={4}
            />
          )}
        />
      ),
    },
    {
      key: "name",
      label: tCols("name"),
      width: 160,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.name`)} />
      ),
    },
    {
      key: "nameEn",
      label: tCols("nameEn"),
      width: 160,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.nameEn`)} />
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

export function getExchangeRateRowClassName(
  row: ExchangeRateFormRow,
  original: ExchangeRateFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.exchangeDate !== row.exchangeDate ||
    orig.cashSellExchangeRate !== row.cashSellExchangeRate ||
    orig.cashBuyExchangeRate !== row.cashBuyExchangeRate ||
    orig.wireSendExchangeRate !== row.wireSendExchangeRate ||
    orig.wireReceiveExchangeRate !== row.wireReceiveExchangeRate ||
    orig.standardExchangeRate !== row.standardExchangeRate ||
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
