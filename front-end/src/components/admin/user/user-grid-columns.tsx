"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import { MultiSelectBox } from "@/components/shared/inputs/multi-select-box";

export interface UserFormRow {
  entityId: number;
  username: string;
  email: string;
  password: string;
  role: string;
  modules: string;
  active: boolean;
  _originalAttributes: Record<string, string[]>;
}

export interface FormValues {
  rows: UserFormRow[];
}

export const ROLE_OPTIONS = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "USER", label: "USER" },
  { value: "MANAGER", label: "MANAGER" },
] as const;

export const ACTIVE_OPTIONS = [
  { value: "true", label: "Active" },
  { value: "false", label: "Inactive" },
] as const;

export function buildUserColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  moduleValueOptions: { value: string; label: string }[],
  onUsernameDoubleClick?: (entityId: number) => void,
): GridColumn<UserFormRow>[] {
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
      key: "username",
      label: "Username",
      width: 160,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.username`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
            onDoubleClick={() => onUsernameDoubleClick?.(row.entityId)}
          />
        );
      },
    },
    {
      key: "email",
      label: "Email",
      width: 220,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.email`)} />
      ),
    },
    {
      key: "password",
      label: "Password",
      width: 160,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        if (!isNew) {
          return (
            <TextBox
              variant="cell"
              readOnly
              placeholder="<Hide>"
              value=""
            />
          );
        }
        return (
          <TextBox
            variant="cell"
            type="password"
            {...register(`rows.${i}.password`)}
          />
        );
      },
    },
    {
      key: "role",
      label: "Role",
      width: 110,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.role`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={[...ROLE_OPTIONS]}
              value={field.value ?? ""}
              onChange={(e) => field.onChange(e.target.value)}
            />
          )}
        />
      ),
    },
    {
      key: "modules",
      label: "Modules",
      width: 160,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.modules`}
          control={control}
          render={({ field }) => (
            <MultiSelectBox
              variant="cell"
              options={moduleValueOptions}
              value={field.value ? field.value.split(",").filter(Boolean) : []}
              onChange={(values) => field.onChange(values.join(","))}
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

export function getUserRowClassName(
  row: UserFormRow,
  original: UserFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.email !== row.email ||
    orig.role !== row.role ||
    orig.modules !== row.modules ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
