"use client";

import { Controller } from "react-hook-form";
import type { UseFormRegister, Control } from "react-hook-form";
import { TextBox } from "@/components/shared/inputs/text-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { ButtonFormRow, ButtonFormValues } from "./button-list-helpers";

export type { ButtonFormRow, ButtonFormValues };

// 컬럼 정의 단일 소스 — TreeRow 셀 width 및 헤더 행과 동기화
export const BUTTON_COLUMNS = [
  { key: "buttonCode", label: "Code",       width: 160 },
  { key: "label",      label: "Label",      width: 140 },
  { key: "actionType", label: "Action",     width: 110 },
  { key: "apiMethod",  label: "API Method", width: 90  },
  { key: "apiPath",    label: "API Path",   width: 180 },
  { key: "sortOrder",  label: "Order",      width: 54  },
  { key: "active",     label: "Status",     width: 80  },
] as const;

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export const ACTION_TYPE_OPTIONS = [
  { value: "CREATE", label: "CREATE" },
  { value: "UPDATE", label: "UPDATE" },
  { value: "DELETE", label: "DELETE" },
  { value: "EXPORT", label: "EXPORT" },
  { value: "CUSTOM", label: "CUSTOM" },
] as const;

interface ButtonCellsProps {
  row: ButtonFormRow;
  idx: number;
  register: UseFormRegister<ButtonFormValues>;
  control: Control<ButtonFormValues>;
}

/**
 * 트리 행 셀 렌더러.
 * buttonCode: 신규행(entityId<0)만 편집 가능, 기존행은 read-only span.
 * 나머지 필드: 신규/기존 모두 편집 가능.
 */
export function ButtonRowCells({ row, idx, register, control }: ButtonCellsProps) {
  const isNew = row.entityId < 0;

  return (
    <>
      {/* hidden: entityId, id, menuId */}
      <input type="hidden" {...register(`rows.${idx}.entityId`, { valueAsNumber: true })} />
      <input type="hidden" {...register(`rows.${idx}.id`, { valueAsNumber: true })} />
      <input type="hidden" {...register(`rows.${idx}.menuId`, { valueAsNumber: true })} />

      {/* buttonCode: 신규만 편집 */}
      {isNew ? (
        <TextBox
          variant="cell"
          {...register(`rows.${idx}.buttonCode`)}
          placeholder="BTN_CODE"
          style={{
            fontFamily: "var(--font-mono)",
            fontWeight: 600,
            width: 160,
            textTransform: "uppercase",
          }}
        />
      ) : (
        <span
          style={{
            fontFamily: "var(--font-mono)",
            fontWeight: 600,
            fontSize: 12,
            color: "var(--ink-2)",
            width: 160,
            display: "inline-block",
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap",
          }}
        >
          {row.buttonCode}
        </span>
      )}

      {/* label */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.label`)}
        placeholder="Label"
        style={{ width: 140 }}
      />

      {/* actionType */}
      <Controller
        name={`rows.${idx}.actionType`}
        control={control}
        render={({ field }) => (
          <ComboBox
            variant="cell"
            options={[...ACTION_TYPE_OPTIONS]}
            value={field.value}
            onChange={field.onChange}
            style={{ width: 110 }}
          />
        )}
      />

      {/* apiMethod */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.apiMethod`)}
        placeholder="GET"
        style={{ width: 90, fontFamily: "var(--font-mono)", fontSize: 11 }}
      />

      {/* apiPath */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.apiPath`)}
        placeholder="/api/..."
        style={{ width: 180, fontFamily: "var(--font-mono)", fontSize: 11 }}
      />

      {/* sortOrder */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.sortOrder`, {
          valueAsNumber: true,
          setValueAs: (v) =>
            v === "" || v === null || Number.isNaN(Number(v)) ? null : Number(v),
        })}
        type="number"
        placeholder="#"
        style={{ width: 54, textAlign: "right" }}
      />

      {/* active */}
      <Controller
        name={`rows.${idx}.active`}
        control={control}
        render={({ field }) => (
          <ComboBox
            variant="cell"
            options={[...ACTIVE_OPTIONS]}
            value={String(field.value)}
            onChange={(e) => field.onChange(e.target.value === "true")}
            style={{ width: 80 }}
          />
        )}
      />
    </>
  );
}
