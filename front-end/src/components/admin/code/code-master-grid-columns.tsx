"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox } from "@/components/shared/inputs/text-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { NumberBox } from "@/components/shared/inputs/number-box";

export interface CodeMasterFormRow {
  entityId: number;
  masterCode: string;
  masterName: string;
  description: string;
  sortOrder: number | null;
  active: boolean;
}

export interface CodeMasterFormValues {
  rows: CodeMasterFormRow[];
}

const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildCodeMasterColumns(
  register: UseFormRegister<CodeMasterFormValues>,
  control: Control<CodeMasterFormValues>,
): GridColumn<CodeMasterFormRow>[] {
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
      key: "masterCode",
      label: "Master Code",
      width: 140,
      render: (_v, row, i) => (
        <TextBox
          variant="cell"
          {...register(`rows.${i}.masterCode`)}
          readOnly={row.entityId > 0}
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
        />
      ),
    },
    {
      key: "masterName",
      label: "Master Name",
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.masterName`)} />
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
      key: "sortOrder",
      label: "Sort Order",
      width: 90,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.sortOrder`}
          control={control}
          render={({ field }) => (
            <NumberBox
              variant="cell"
              value={field.value ?? ""}
              onChange={(e) => {
                const v = (e.target as HTMLInputElement).value;
                field.onChange(v === "" ? null : Number(v));
              }}
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

export function getCodeMasterRowClassName(
  row: CodeMasterFormRow,
  original: CodeMasterFormRow[],
  selectedMasterId: number | null,
): string | undefined {
  let cls: string | undefined;
  if (row.entityId < 0) {
    cls = "is-new";
  } else {
    const orig = original.find((o) => o.entityId === row.entityId);
    if (orig) {
      const changed =
        orig.masterName !== row.masterName ||
        orig.description !== row.description ||
        orig.sortOrder !== row.sortOrder ||
        orig.active !== row.active;
      if (changed) cls = "is-modified";
    }
  }
  if (selectedMasterId !== null && selectedMasterId === row.entityId) {
    return cls ? `${cls} is-selected` : "is-selected";
  }
  return cls;
}
