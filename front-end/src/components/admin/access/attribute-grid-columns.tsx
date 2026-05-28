"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import type { AttributeFormRow, AttributeFormValues } from "./attribute-list-helpers";

export { type AttributeFormRow, type AttributeFormValues };

const VALUE_TYPE_OPTIONS = [
  { value: "STRING", label: "STRING" },
  { value: "NUMBER", label: "NUMBER" },
  { value: "BOOLEAN", label: "BOOLEAN" },
  { value: "ENUM", label: "ENUM" },
] as const;

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildAttributeColumns(
  register: UseFormRegister<AttributeFormValues>,
  control: Control<AttributeFormValues>,
  onKeyDoubleClick?: (entityId: number, allowMulti: boolean) => void,
): GridColumn<AttributeFormRow>[] {
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
      key: "attributeKey",
      label: "Attribute Key",
      width: 200,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        if (isNew) {
          return (
            <TextBox
              variant="cell"
              {...register(`rows.${i}.attributeKey`)}
              placeholder="my_attr_key"
              style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
            />
          );
        }
        return (
          <span
            style={
              row.allowMulti
                ? { cursor: "pointer", userSelect: "none", fontFamily: "var(--font-mono)", fontWeight: 600 }
                : { fontFamily: "var(--font-mono)", fontWeight: 600 }
            }
            onDoubleClick={
              row.allowMulti
                ? (e) => {
                    e.stopPropagation();
                    onKeyDoubleClick?.(row.entityId, row.allowMulti);
                  }
                : undefined
            }
          >
            {row.attributeKey}
          </span>
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
      key: "valueType",
      label: "Value Type",
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.valueType`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={[...VALUE_TYPE_OPTIONS]}
              value={field.value}
              onChange={field.onChange}
            />
          )}
        />
      ),
    },
    {
      key: "allowMulti",
      label: "Allow Multi",
      width: 110,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.allowMulti`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={[
                { value: "true", label: "Yes" },
                { value: "false", label: "No" },
              ]}
              value={String(field.value)}
              onChange={(e) => field.onChange(e.target.value === "true")}
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

export function getAttributeRowClassName(
  row: AttributeFormRow,
  original: AttributeFormRow[],
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.valueType !== row.valueType ||
    orig.allowMulti !== row.allowMulti ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
