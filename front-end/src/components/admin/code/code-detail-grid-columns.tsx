"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox } from "@/components/shared/inputs/text-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { NumberBox } from "@/components/shared/inputs/number-box";

export interface CodeDetailFormRow {
  entityId: number;
  codeValue: string;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string;
}

export interface CodeDetailFormValues {
  rows: CodeDetailFormRow[];
}

const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildCodeDetailColumns(
  register: UseFormRegister<CodeDetailFormValues>,
  control: Control<CodeDetailFormValues>,
): GridColumn<CodeDetailFormRow>[] {
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
      key: "codeValue",
      label: "Code Value",
      width: 130,
      render: (_v, row, i) => (
        <TextBox
          variant="cell"
          {...register(`rows.${i}.codeValue`)}
          readOnly={row.entityId > 0}
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
        />
      ),
    },
    {
      key: "codeLabel",
      label: "Code Label",
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.codeLabel`)} />
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
    {
      key: "remark",
      label: "Remark",
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.remark`)} />
      ),
    },
  ];
}

export function getCodeDetailRowClassName(
  row: CodeDetailFormRow,
  original: CodeDetailFormRow[],
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.codeLabel !== row.codeLabel ||
    orig.sortOrder !== row.sortOrder ||
    orig.active !== row.active ||
    orig.remark !== row.remark;
  return changed ? "is-modified" : undefined;
}
