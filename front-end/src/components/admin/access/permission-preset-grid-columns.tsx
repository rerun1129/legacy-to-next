"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";

export interface PresetFormRow {
  entityId: number;
  code: string;
  name: string;
  description: string;
  active: boolean;
}

export interface PresetFormValues {
  rows: PresetFormRow[];
}

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildPresetColumns(
  register: UseFormRegister<PresetFormValues>,
  control: Control<PresetFormValues>,
  onCodeDoubleClick?: (entityId: number) => void,
): GridColumn<PresetFormRow>[] {
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
      key: "code",
      label: "Code",
      width: 180,
      render: (_v, row, i) => (
        <TextBox
          variant="cell"
          {...register(`rows.${i}.code`)}
          placeholder="PRESET_XXX"
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
          onDoubleClick={() => onCodeDoubleClick?.(row.entityId)}
        />
      ),
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
      key: "description",
      label: "Description",
      width: 260,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.description`)} />
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

export function getPresetRowClassName(
  row: PresetFormRow,
  original: PresetFormRow[],
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.description !== row.description ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
