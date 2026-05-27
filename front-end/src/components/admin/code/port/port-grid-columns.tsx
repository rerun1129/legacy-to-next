"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import type { PortType } from "@/domain/code/port";

export interface PortFormRow {
  entityId: number;
  portCode: string;
  name: string;
  nameEn: string;
  countryCode: string;
  portType: PortType;
  active: boolean;
}

export interface FormValues {
  rows: PortFormRow[];
}

export const PORT_TYPE_OPTIONS = [
  { value: "SEA", label: "SEA" },
  { value: "AIR", label: "AIR" },
] as const;

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildPortColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>
): GridColumn<PortFormRow>[] {
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
      key: "portCode",
      label: "Port Code",
      width: 140,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.portCode`)}
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
      key: "countryCode",
      label: "Country",
      width: 120,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.countryCode`)} />
      ),
    },
    {
      key: "portType",
      label: "Type",
      width: 90,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.portType`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={[...PORT_TYPE_OPTIONS]}
              value={field.value ?? ""}
              onChange={(e) => field.onChange(e.target.value)}
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

export function getPortRowClassName(
  row: PortFormRow,
  original: PortFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.countryCode !== row.countryCode ||
    orig.portType !== row.portType ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
