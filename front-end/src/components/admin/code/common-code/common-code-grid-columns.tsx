"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox } from "@/components/shared/inputs/text-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { NumberBox } from "@/components/shared/inputs/number-box";

export interface CommonCodeFormRow {
  entityId: number;
  // code는 신규 행에서만 입력 가능 — entityId < 0이면 신규
  code: string;
  label: string;
  labelKo: string;
  sortOrder: number | null;
  active: boolean;
}

export interface CommonCodeFormValues {
  rows: CommonCodeFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildCommonCodeColumns(
  register: UseFormRegister<CommonCodeFormValues>,
  control: Control<CommonCodeFormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<CommonCodeFormRow>[] {
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
      key: "code",
      label: tCols("code"),
      width: 160,
      render: (_v, row, i) => (
        <TextBox
          variant="cell"
          {...register(`rows.${i}.code`)}
          // BE가 code 수정을 차단하므로 기존 행에서는 읽기 전용으로 표시
          readOnly={row.entityId > 0}
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
        />
      ),
    },
    {
      key: "label",
      label: tCols("label"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.label`)} />
      ),
    },
    {
      key: "labelKo",
      label: tCols("labelKo"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.labelKo`)} />
      ),
    },
    {
      key: "sortOrder",
      label: tCols("sortOrder"),
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

export function getCommonCodeRowClassName(
  row: CommonCodeFormRow,
  original: CommonCodeFormRow[],
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.label !== row.label ||
    orig.labelKo !== row.labelKo ||
    orig.sortOrder !== row.sortOrder ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
