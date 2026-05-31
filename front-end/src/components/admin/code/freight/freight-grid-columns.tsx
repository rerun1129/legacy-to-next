"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import type { FreightGroup } from "@/domain/code/freight";

export interface FreightFormRow {
  entityId: number;
  freightCode: string;
  name: string;
  nameEn: string;
  description: string;
  freightUnit: string;
  freightGroup: FreightGroup | null;
  active: boolean;
}

export interface FormValues {
  rows: FreightFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildFreightColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<FreightFormRow>[] {
  const freightGroupOptions = [
    { value: "", label: "—" },
    { value: "OTHER", label: tOptions("groupOther") },
    { value: "FREIGHT", label: tOptions("groupFreight") },
    { value: "SURCHARGE", label: tOptions("groupSurcharge") },
    { value: "WHARFAGE", label: tOptions("groupWharfage") },
  ];

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
      key: "freightCode",
      label: tCols("freightCode"),
      width: 140,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.freightCode`)}
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
      key: "description",
      label: tCols("description"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.description`)} />
      ),
    },
    {
      key: "freightUnit",
      label: tCols("freightUnit"),
      width: 100,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.freightUnit`)} />
      ),
    },
    {
      key: "freightGroup",
      label: tCols("freightGroup"),
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.freightGroup`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={freightGroupOptions}
              value={field.value ?? ""}
              onChange={(e) => field.onChange(e.target.value || null)}
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

export function getFreightRowClassName(
  row: FreightFormRow,
  original: FreightFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.description !== row.description ||
    orig.freightUnit !== row.freightUnit ||
    orig.freightGroup !== row.freightGroup ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
