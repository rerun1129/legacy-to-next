"use client";

import { useMemo } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { DateCell } from "@/components/shared/grid-cell-inputs";

interface DimPreviewRow {
  id: number;
  length: string;
  width: string;
  height: string;
  qty: string;
  cbm: string;
  volWt: string;
  type: string;
  date: string;
  text: string;
  code: string;
  codeName: string;
}

export interface DimPreviewFormValues {
  dimensions: DimPreviewRow[];
}

const TYPE_OPTIONS = [
  { value: "BOX",    label: "BOX" },
  { value: "PALLET", label: "PALLET" },
  { value: "DRUM",   label: "DRUM" },
];

export function createDimPreviewDefaults(): DimPreviewFormValues {
  return {
    dimensions: [
      { id: 1, length: "60",  width: "40", height: "30", qty: "10",
        cbm: "0.072", volWt: "12.000",
        type: "BOX",    date: "20260504", text: "sample row 1",
        code: "KRPUS",  codeName: "Busan Port" },
      { id: 2, length: "100", width: "80", height: "60", qty: "5",
        cbm: "0.480", volWt: "80.000",
        type: "PALLET", date: "",         text: "",
        code: "",       codeName: "" },
      { id: 3, length: "",    width: "",   height: "",   qty: "",
        cbm: "",      volWt: "",
        type: "DRUM",   date: "20260601", text: "third row",
        code: "USNYC",  codeName: "New York" },
    ],
  };
}

export function GridPreviewPanel() {
  const { control, register } = useFormContext<DimPreviewFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "dimensions" });

  const cols = useMemo<GridColumn<DimPreviewRow>[]>(() => [
    {
      key: "_no", label: "#", width: 50, className: "row-num",
      render: (_v, _r, i) => i + 1,
    },
    {
      key: "length", label: "Length", width: 80, className: "is-num",
      render: (_v, _r, i) => (
        <input className="grid__cell-input is-num" {...register(`dimensions.${i}.length`)} />
      ),
    },
    {
      key: "width", label: "Width", width: 80, className: "is-num",
      render: (_v, _r, i) => (
        <input className="grid__cell-input is-num" {...register(`dimensions.${i}.width`)} />
      ),
    },
    {
      key: "height", label: "Height", width: 80, className: "is-num",
      render: (_v, _r, i) => (
        <input className="grid__cell-input is-num" {...register(`dimensions.${i}.height`)} />
      ),
    },
    {
      key: "qty", label: "Qty", width: 80, className: "is-num",
      render: (_v, _r, i) => (
        <input className="grid__cell-input is-num" {...register(`dimensions.${i}.qty`)} />
      ),
    },
    {
      key: "cbm", label: "CBM", width: 80, className: "is-num",
      render: (_v, _r, i) => (
        <input className="grid__cell-input is-num" {...register(`dimensions.${i}.cbm`)} />
      ),
    },
    {
      key: "volWt", label: "Volume Wt.", width: 80, className: "is-num",
      render: (_v, _r, i) => (
        <input className="grid__cell-input is-num" {...register(`dimensions.${i}.volWt`)} />
      ),
    },
    {
      key: "type", label: "TYPE", width: 90,
      render: (_v, _r, i) => (
        <DropBox variant="cell" options={TYPE_OPTIONS} {...register(`dimensions.${i}.type`)} />
      ),
    },
    {
      key: "date", label: "DATE", width: 110,
      // DateCell은 비제어 시연용이므로 defaultValue 없이 마운트
      render: () => <DateCell />,
    },
    {
      key: "text", label: "TEXT", width: 140,
      render: (_v, _r, i) => (
        <TextBox variant="cell" {...register(`dimensions.${i}.text`)} />
      ),
    },
    {
      key: "codeBox", label: "CODE_BOX", width: 220,
      render: (_v, _r, i) => (
        <CodeBox
          kind="lcn"
          codeProps={{
            className: "grid__cell-input",
            placeholder: "Code",
            ...register(`dimensions.${i}.code`),
          }}
          nameProps={{
            className: "grid__cell-input",
            placeholder: "Name",
            ...register(`dimensions.${i}.codeName`),
          }}
          onLookup={() => alert(`Lookup row ${i + 1}`)}
        />
      ),
    },
  ], [register]);

  function handleAdd() {
    const nextId = Math.max(0, ...fields.map((f) => f.id)) + 1;
    append({
      id: nextId,
      length: "", width: "", height: "", qty: "", cbm: "", volWt: "",
      type: "", date: "", text: "", code: "", codeName: "",
    });
  }

  function handleRemove() {
    if (fields.length === 0) return;
    remove(fields.length - 1);
  }

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Dimension</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleAdd}>
            <Plus size={12} />
          </button>
          <button
            type="button"
            className="btn btn--sm btn--ghost"
            onClick={handleRemove}
            disabled={fields.length === 0}
          >
            <Minus size={12} />
          </button>
        </div>
      </div>
      <GridList
        columns={cols}
        data={fields as unknown as DimPreviewRow[]}
        rowKey={(r) => r.id}
        className="grid--demo"
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
