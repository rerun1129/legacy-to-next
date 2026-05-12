"use client";

import { useState, useMemo, useRef }              from "react";
import { useFormContext, useFieldArray, Controller } from "react-hook-form";
import { Plus, Minus }                           from "lucide-react";
import { GridList, type GridColumn }             from "@/components/shared/grid-list";
import { ComboBox, NumberBox }                    from "@/components/shared/inputs";
import { useEnumOptions }                        from "@/application/enums/use-enum";
import type { NonBlFormValues }                  from "../../non-bl-schema";
import { EMPTY_DIM_ROW }                         from "../../non-bl-schema";

interface DimRow { id: number; length: string; width: string; height: string; qty: string; cbm: string; volWt: string; }

export function NonBLDimensionPanel() {
  const { control } = useFormContext<NonBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "dimensions" });
  const { options: volumeDivisorOptions, placeholder: volumeDivisorPlaceholder } = useEnumOptions("VolumeDivisor");

  const cols = useMemo<GridColumn<DimRow>[]>(() => [
    { key: "_no",    label: "#",           width: 50, className: "row-num", render: (_, __, i) => i + 1 },
    { key: "length", label: "Length",     width: 80, render: (_, __, i) => <NumberBox name={`dimensions.${i}.length`} variant="cell" valueAsNumber={false} /> },
    { key: "width",  label: "Width",      width: 80, render: (_, __, i) => <NumberBox name={`dimensions.${i}.width`} variant="cell" valueAsNumber={false} /> },
    { key: "height", label: "Height",     width: 80, render: (_, __, i) => <NumberBox name={`dimensions.${i}.height`} variant="cell" valueAsNumber={false} /> },
    { key: "qty",    label: "Qty",        width: 80, render: (_, __, i) => <NumberBox name={`dimensions.${i}.qty`} variant="cell" valueAsNumber={false} /> },
    { key: "cbm",    label: "CBM",        width: 80, render: (_, __, i) => <NumberBox name={`dimensions.${i}.cbm`} variant="cell" valueAsNumber={false} decimalPlaces={3} /> },
    { key: "volWt",  label: "Volume Wt.", width: 80, render: (_, __, i) => <NumberBox name={`dimensions.${i}.volWt`} variant="cell" valueAsNumber={false} decimalPlaces={3} /> },
  ], []);
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
    append({ ...EMPTY_DIM_ROW, id: nextId });
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
        <span className="panel__title">Dimension</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Controller
            name="dimensionDivisor"
            control={control}
            render={({ field }) => (
              <ComboBox
                variant="panel"
                options={volumeDivisorOptions}
                placeholder={volumeDivisorPlaceholder}
                value={field.value}
                onChange={field.onChange}
              />
            )}
          />
          <button type="button" className="btn btn--sm btn--icon btn--success" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--icon btn--danger" onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={cols}
        data={fields as unknown as DimRow[]}
        rowKey={(r) => r.id}
        onRowClick={(row) => setSelectedKey(String(row.id))}
        rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey("")}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
