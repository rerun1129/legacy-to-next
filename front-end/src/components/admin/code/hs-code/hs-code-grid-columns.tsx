"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";

export interface HsCodeFormRow {
  entityId: number;
  hsCode: string;
  name: string;
  nameEn: string;
  countryCode: string;
  active: boolean;
}

export interface FormValues {
  rows: HsCodeFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildHsCodeColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<HsCodeFormRow>[] {
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
      key: "hsCode",
      label: tCols("hsCode"),
      width: 140,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.hsCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
          />
        );
      },
    },
    {
      key: "name",
      label: tCols("name"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.name`)} />
      ),
    },
    {
      key: "nameEn",
      label: tCols("nameEn"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.nameEn`)} />
      ),
    },
    {
      key: "countryCode",
      label: tCols("countryCode"),
      width: 120,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.countryCode`)} />
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

export function getHsCodeRowClassName(
  row: HsCodeFormRow,
  original: HsCodeFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.countryCode !== row.countryCode ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
