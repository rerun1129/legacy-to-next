"use client";

import { useState, useMemo, useRef }              from "react";
import { useFormContext, useFieldArray, Controller } from "react-hook-form";
import { Plus, Minus }                           from "lucide-react";
import { GridList, type GridColumn }             from "@/components/shared/grid-list";
import { TextBox, NumberBox, ComboBox, CodeBox }  from "@/components/shared/inputs";
import { useEnumOptions }                        from "@/application/enums/use-enum";
import type { NonBlFormValues }                  from "../../non-bl-schema";
import { EMPTY_CONTAINER_ROW }                   from "../../non-bl-schema";
import { Button }                                from "@/components/shared/button";
import { useCodeAutocomplete }                   from "@/lib/use-code-autocomplete";
import { CODE_SOURCES }                          from "@/lib/autocomplete-sources";

interface ContainerInfoRow {
  id: number;
  cno: string;
  contType: string;
  sealNo1: string;
  sealNo2: string;
  sealNo3: string;
  pkg: number;
  pkgUnit: string;
  grossWt: number;
  cbm: number;
}

function PkgUnitCell({ index }: { index: number }) {
  const { register, setValue } = useFormContext<NonBlFormValues>();
  const pkgUnit = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  return (
    <CodeBox
      kind="code-only"
      variant="cell"
      codeProps={{ ...register(`containers.${index}.pkgUnit`) }}
      onLookup={() => {}}
      onSearch={pkgUnit.onSearch}
      suggestions={pkgUnit.suggestions}
      suggestionsLoading={pkgUnit.suggestionsLoading}
      onSelect={(it) => { setValue(`containers.${index}.pkgUnit`, it.code); }}
    />
  );
}

export function NonBLContainerInfoPanel() {
  const { control, register } = useFormContext<NonBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "containers" });
  const { options: rawOptions } = useEnumOptions("ContainerType");
  const contTypeOptions = useMemo(() => rawOptions.map(o => ({ value: o.value, label: o.label })), [rawOptions]);

  const cols = useMemo<GridColumn<ContainerInfoRow>[]>(() => [
    { key: "_no",      width: 50, label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
    { key: "cno",      width: 80, label: "Container No.", render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.cno`)} /> },
    { key: "contType", width: 80, label: "Cont.Type",   render: (_, __, i) => (
      <Controller
        name={`containers.${i}.contType`}
        control={control}
        render={({ field }) => (
          <ComboBox variant="cell" options={contTypeOptions} value={field.value} onChange={field.onChange} />
        )}
      />
    )},
    { key: "sealNo1",  width: 80, label: "Seal No. 1",  render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.sealNo1`)} /> },
    { key: "sealNo2",  width: 80, label: "Seal No. 2",  render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.sealNo2`)} /> },
    { key: "sealNo3",  width: 80, label: "Seal No. 3",  render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.sealNo3`)} /> },
    { key: "pkg",      width: 80, label: "Package",     render: (_, __, i) => <NumberBox name={`containers.${i}.pkg`} variant="cell" decimalPlaces={0} /> },
    { key: "pkgUnit",  width: 80, label: "Unit",        render: (_, __, i) => <PkgUnitCell index={i} /> },
    { key: "grossWt",  width: 80, label: "Gross W/T",   render: (_, __, i) => <NumberBox name={`containers.${i}.grossWt`} variant="cell" decimalPlaces={3} /> },
    { key: "cbm",      width: 80, label: "CBM",         render: (_, __, i) => <NumberBox name={`containers.${i}.cbm`} variant="cell" decimalPlaces={3} /> },
  ], [register, control, contTypeOptions]);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);

  // id: z.number(), selectedKey: string state → 비교 시 명시 변환 필요 (가이드 §6.9)
  const selectedIdx = fields.findIndex(f => f.id === Number(selectedKey));

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    const nextId = Math.max(0, ...fields.map(f => f.id)) + 1;
    append({ ...EMPTY_CONTAINER_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => String((f as { id: number }).id) === focused);
    }
    if (targetIdx === -1 && selectedKey !== null && selectedIdx !== -1) {
      targetIdx = selectedIdx;
    }
    if (targetIdx === -1) targetIdx = fields.length - 1;
    remove(targetIdx);
    setSelectedKey(null);
    focusedRowKeyRef.current = null;
  }

  return (
    <div className="panel panel--col">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container Information</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
          <Button variant="danger" size="sm" iconOnly onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
        </div>
      </div>
      <GridList
        columns={cols}
        data={fields as unknown as ContainerInfoRow[]}
        rowKey={(r) => r.id}
        onRowClick={(row) => setSelectedKey(String(row.id))}
        rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey("")}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
