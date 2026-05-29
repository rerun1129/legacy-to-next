"use client";

import { Controller } from "react-hook-form";
import type { UseFormRegister, Control } from "react-hook-form";
import { TextBox } from "@/components/shared/inputs/text-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { MenuFormRow, MenuFormValues } from "./menu-list-helpers";

export type { MenuFormRow, MenuFormValues };

// 컬럼 정의 단일 소스 — TreeRow 셀 width 및 헤더 행과 동기화
export const MENU_COLUMNS = [
  { key: "menuCode",    label: "Code",       width: 140 },
  { key: "label",       label: "Label",      width: 140 },
  { key: "labelEn",     label: "Label (EN)", width: 120 },
  { key: "path",        label: "Path",       width: 160 },
  { key: "icon",        label: "Icon",       width: 80  },
  { key: "sortOrder",   label: "Order",      width: 54  },
  { key: "moduleCode",  label: "Module",     width: 140 },
  { key: "active",      label: "Status",     width: 80  },
] as const;

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

interface MenuCellsProps {
  row: MenuFormRow;
  idx: number;
  register: UseFormRegister<MenuFormValues>;
  control: Control<MenuFormValues>;
  moduleOptions: { value: string; label: string }[];
}

/**
 * 트리 행 셀 렌더러.
 * menuCode: 신규행(entityId<0)만 편집 가능, 기존행은 read-only span.
 * 나머지 필드: 신규/기존 모두 편집 가능.
 */
export function MenuRowCells({ row, idx, register, control, moduleOptions }: MenuCellsProps) {
  const isNew = row.entityId < 0;

  return (
    <>
      {/* hidden: entityId, id */}
      <input type="hidden" {...register(`rows.${idx}.entityId`, { valueAsNumber: true })} />
      <input type="hidden" {...register(`rows.${idx}.id`, { valueAsNumber: true })} />

      {/* menuCode: 신규만 편집 */}
      {isNew ? (
        <TextBox
          variant="cell"
          {...register(`rows.${idx}.menuCode`)}
          placeholder="MENU_CODE"
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600, width: 140, textTransform: "uppercase" }}
        />
      ) : (
        <span
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600, fontSize: 12, color: "var(--ink-2)" }}
        >
          {row.menuCode}
        </span>
      )}

      {/* label */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.label`)}
        placeholder="Label"
        style={{ width: 140 }}
      />

      {/* labelEn */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.labelEn`)}
        placeholder="Label EN"
        style={{ width: 120 }}
      />

      {/* path */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.path`)}
        placeholder="/path"
        style={{ width: 160, fontFamily: "var(--font-mono)", fontSize: 11 }}
      />

      {/* icon */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.icon`)}
        placeholder="icon"
        style={{ width: 80 }}
      />

      {/* sortOrder */}
      <TextBox
        variant="cell"
        {...register(`rows.${idx}.sortOrder`, { valueAsNumber: true, setValueAs: (v) => (v === "" || v === null || Number.isNaN(Number(v)) ? null : Number(v)) })}
        type="number"
        placeholder="#"
        style={{ width: 54, textAlign: "right" }}
      />

      {/* moduleCode */}
      <Controller
        name={`rows.${idx}.moduleCode`}
        control={control}
        render={({ field }) => (
          <ComboBox
            variant="cell"
            options={moduleOptions}
            value={field.value}
            onChange={field.onChange}
            style={{ width: 140 }}
          />
        )}
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
