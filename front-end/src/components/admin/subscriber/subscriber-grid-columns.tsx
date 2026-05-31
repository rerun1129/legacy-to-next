"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";

export interface SubscriberFormRow {
  entityId: number;
  subscriberCode: string;
  name: string;
  nameEn: string;
  businessNo: string;
  representative: string;
  phone: string;
  email: string;
  memo: string;
  active: boolean;
}

export interface FormValues {
  rows: SubscriberFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildSubscriberColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
  onCodeDoubleClick: (entityId: number) => void,
): GridColumn<SubscriberFormRow>[] {
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
      key: "subscriberCode",
      label: tCols("subscriberCode"),
      width: 160,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.subscriberCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)" }}
            onDoubleClick={() => onCodeDoubleClick(row.entityId)}
          />
        );
      },
    },
    {
      key: "name",
      label: tCols("name"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.name`)} />
      ),
    },
    {
      key: "nameEn",
      label: tCols("nameEn"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.nameEn`)} />
      ),
    },
    {
      key: "businessNo",
      label: tCols("businessNo"),
      width: 120,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.businessNo`)} />
      ),
    },
    {
      key: "representative",
      label: tCols("representative"),
      width: 120,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.representative`)} />
      ),
    },
    {
      key: "phone",
      label: tCols("phone"),
      width: 130,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.phone`)} />
      ),
    },
    {
      key: "email",
      label: tCols("email"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.email`)} />
      ),
    },
    {
      key: "memo",
      label: tCols("memo"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.memo`)} />
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

export function getSubscriberRowClassName(
  row: SubscriberFormRow,
  original: SubscriberFormRow[],
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.businessNo !== row.businessNo ||
    orig.representative !== row.representative ||
    orig.phone !== row.phone ||
    orig.email !== row.email ||
    orig.memo !== row.memo ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
