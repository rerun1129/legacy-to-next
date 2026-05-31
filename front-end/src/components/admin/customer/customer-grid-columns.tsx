"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import type { CustomerType } from "@/domain/customer";

export interface CustomerFormRow {
  entityId: number;
  customerCode: string;
  customerType: CustomerType;
  name: string;
  nameEn: string;
  businessNo: string;
  representative: string;
  phone: string;
  email: string;
  customerLocalAddress: string;
  customerEnglishAddress: string;
  memo: string;
  countryCode: string;
  active: boolean;
}

export interface FormValues {
  rows: CustomerFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildCustomerColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<CustomerFormRow>[] {
  const customerTypeOptions = [
    { value: "CUSTOMER", label: "CUSTOMER" },
    { value: "PARTNER", label: "PARTNER" },
    { value: "AIRCARRIER", label: "AIRCARRIER" },
    { value: "LINER", label: "LINER" },
    { value: "TRUCKER", label: "TRUCKER" },
    { value: "WAREHOUSE", label: "WAREHOUSE" },
    { value: "OTHER", label: "OTHER" },
  ];

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
      key: "customerCode",
      label: tCols("customerCode"),
      width: 140,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.customerCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)" }}
          />
        );
      },
    },
    {
      key: "customerType",
      label: tCols("customerType"),
      width: 120,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.customerType`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={customerTypeOptions}
              value={field.value}
              onChange={(e) => field.onChange(e.target.value)}
            />
          )}
        />
      ),
    },
    {
      key: "name",
      label: tCols("name"),
      width: 180,
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
      width: 120,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.phone`)} />
      ),
    },
    {
      key: "email",
      label: tCols("email"),
      width: 180,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.email`)} />
      ),
    },
    {
      key: "customerLocalAddress",
      label: tCols("localAddress"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.customerLocalAddress`)} />
      ),
    },
    {
      key: "customerEnglishAddress",
      label: tCols("englishAddress"),
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.customerEnglishAddress`)} />
      ),
    },
    {
      key: "memo",
      label: tCols("memo"),
      width: 150,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.memo`)} />
      ),
    },
    {
      key: "countryCode",
      label: tCols("country"),
      width: 80,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.countryCode`)} />
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

export function getCustomerRowClassName(
  row: CustomerFormRow,
  original: CustomerFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.customerType !== row.customerType ||
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.businessNo !== row.businessNo ||
    orig.representative !== row.representative ||
    orig.phone !== row.phone ||
    orig.email !== row.email ||
    orig.customerLocalAddress !== row.customerLocalAddress ||
    orig.customerEnglishAddress !== row.customerEnglishAddress ||
    orig.memo !== row.memo ||
    orig.countryCode !== row.countryCode ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
