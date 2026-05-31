"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox, NumberBox } from "@/components/shared/inputs";
import type { AttributeValueFormRow, AttributeValueFormValues } from "./attribute-value-list-helpers";

export { type AttributeValueFormRow, type AttributeValueFormValues };

type T = ReturnType<typeof import("next-intl").useTranslations>;

export function buildAttributeValueColumns(
  register: UseFormRegister<AttributeValueFormValues>,
  control: Control<AttributeValueFormValues>,
  tCols: T,
  tOptions: T,
): GridColumn<AttributeValueFormRow>[] {
  const activeOptions = [
    { value: "true",  label: tOptions("active")   },
    { value: "false", label: tOptions("inactive") },
  ];

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
      key: "value",
      label: tCols("value"),
      width: 160,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        if (isNew) {
          return (
            <TextBox
              variant="cell"
              {...register(`rows.${i}.value`)}
              placeholder="VALUE_CODE"
              style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
            />
          );
        }
        return (
          <span style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}>{row.value}</span>
        );
      },
    },
    {
      key: "label",
      label: tCols("label"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.label`)} />
      ),
    },
    {
      key: "sortOrder",
      label: tCols("sortOrder"),
      width: 100,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.sortOrder`}
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
      label: tCols("active"),
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

export function getAttributeValueRowClassName(
  row: AttributeValueFormRow,
  original: AttributeValueFormRow[],
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.label !== row.label ||
    orig.sortOrder !== row.sortOrder ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
