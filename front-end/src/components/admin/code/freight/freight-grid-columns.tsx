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

export const FREIGHT_GROUP_OPTIONS = [
  { value: "", label: "—" },
  { value: "OTHER", label: "OTHER" },
  { value: "FREIGHT", label: "FREIGHT" },
  { value: "SURCHARGE", label: "SURCHARGE" },
  { value: "WHARFAGE", label: "WHARFAGE" },
] as const;

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildFreightColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>
): GridColumn<FreightFormRow>[] {
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
      key: "freightCode",
      label: "Freight Code",
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
      label: "Name",
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.name`)} />
      ),
    },
    {
      key: "nameEn",
      label: "English Name",
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.nameEn`)} />
      ),
    },
    {
      key: "description",
      label: "Description",
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.description`)} />
      ),
    },
    {
      key: "freightUnit",
      label: "Unit",
      width: 100,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.freightUnit`)} />
      ),
    },
    {
      key: "freightGroup",
      label: "Group",
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.freightGroup`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={[...FREIGHT_GROUP_OPTIONS]}
              value={field.value ?? ""}
              onChange={(e) => field.onChange(e.target.value || null)}
            />
          )}
        />
      ),
    },
    {
      key: "active",
      label: "Status",
      width: 100,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.active`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={[...ACTIVE_OPTIONS]}
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
