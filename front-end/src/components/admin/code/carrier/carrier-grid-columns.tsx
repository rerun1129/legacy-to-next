"use client";

import type { UseFormRegister, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import type { CarrierType } from "@/domain/code/carrier";

export interface CarrierFormRow {
  entityId: number;
  carrierCode: string;
  name: string;
  nameEn: string;
  carrierType: CarrierType;
  carrierAddress: string;
  ediCode: string;
  active: boolean;
}

export interface FormValues {
  rows: CarrierFormRow[];
}

type ColsT = (key: string) => string;
type OptionsT = (key: string) => string;

export function buildCarrierColumns(
  register: UseFormRegister<FormValues>,
  control: Control<FormValues>,
  tCols: ColsT,
  tOptions: OptionsT,
): GridColumn<CarrierFormRow>[] {
  const carrierTypeOptions = [
    { value: "SEA", label: tOptions("typeSea") },
    { value: "AIR", label: tOptions("typeAir") },
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
      key: "carrierCode",
      label: tCols("carrierCode"),
      width: 140,
      render: (_v, row, i) => {
        const isNew = row.entityId < 0;
        return (
          <TextBox
            variant="cell"
            {...register(`rows.${i}.carrierCode`)}
            readOnly={!isNew}
            style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
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
      width: 200,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.nameEn`)} />
      ),
    },
    {
      key: "carrierType",
      label: tCols("carrierType"),
      width: 90,
      render: (_v, _row, i) => (
        <Controller
          name={`rows.${i}.carrierType`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={carrierTypeOptions}
              value={field.value ?? ""}
              onChange={(e) => field.onChange(e.target.value)}
            />
          )}
        />
      ),
    },
    {
      key: "carrierAddress",
      label: tCols("carrierAddress"),
      width: 250,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.carrierAddress`)} />
      ),
    },
    {
      key: "ediCode",
      label: tCols("ediCode"),
      width: 120,
      render: (_v, _row, i) => (
        <TextBox variant="cell" {...register(`rows.${i}.ediCode`)} />
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

export function getCarrierRowClassName(
  row: CarrierFormRow,
  original: CarrierFormRow[]
): string | undefined {
  if (row.entityId < 0) return "is-new";
  const orig = original.find((o) => o.entityId === row.entityId);
  if (!orig) return undefined;
  const changed =
    orig.name !== row.name ||
    orig.nameEn !== row.nameEn ||
    orig.carrierType !== row.carrierType ||
    orig.carrierAddress !== row.carrierAddress ||
    orig.ediCode !== row.ediCode ||
    orig.active !== row.active;
  return changed ? "is-modified" : undefined;
}
