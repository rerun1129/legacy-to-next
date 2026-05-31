"use client";

import { useMemo, useState, useRef }                    from "react";
import { useFormContext, useFieldArray, Controller }    from "react-hook-form";
import { useTranslations }                              from "next-intl";
import { Plus, Minus }                                 from "lucide-react";
import type { TruckBlFormValues }                      from "@/components/fms/truck-bl/truck-bl-schema";
import { EMPTY_TRUCK_ORDER_ROW }                       from "@/components/fms/truck-bl/truck-bl-schema";
import { GridList, type GridColumn }                   from "@/components/shared/grid-list";
import { TextBox, NumberBox, ComboBox, CodeBox }       from "@/components/shared/inputs";
import { useEnumOptions }                              from "@/application/enums/use-enum";
import { Button }                                      from "@/components/shared/button";
import { useCodeAutocomplete }                         from "@/lib/use-code-autocomplete";
import { CODE_SOURCES }                                from "@/lib/autocomplete-sources";

function PkgUnitCell({ index }: { index: number }) {
  const { register, setValue } = useFormContext<TruckBlFormValues>();
  const pkgUnit = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  return (
    <CodeBox
      kind="code-only"
      variant="cell"
      codeProps={{ ...register(`truckOrders.${index}.pkgUnit`) }}
      onLookup={() => {}}
      onSearch={pkgUnit.onSearch}
      suggestions={pkgUnit.suggestions}
      suggestionsLoading={pkgUnit.suggestionsLoading}
      onSelect={(it) => { setValue(`truckOrders.${index}.pkgUnit`, it.code); }}
    />
  );
}

type TruckOrderRow = NonNullable<TruckBlFormValues["truckOrders"]>[number];

export function TruckOrderGridPanel() {
  // Rules of Hooks: unconditionally at top
  const tf = useTranslations("fms.truckBl.entry.fields");
  const tp = useTranslations("fms.truckBl.entry.panels");

  const { control, register } = useFormContext<TruckBlFormValues>();
  const { options: truckTypeOptions }            = useEnumOptions("TruckType");
  const { options: containerTypeOptionsRaw }     = useEnumOptions("ContainerType");
  const containerTypeOptions = useMemo(
    () => containerTypeOptionsRaw.map(o => ({ value: o.value, label: o.label })),
    [containerTypeOptionsRaw]
  );
  const { fields, append, remove } = useFieldArray({ control, name: "truckOrders" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);

  const columns = useMemo<GridColumn<TruckOrderRow>[]>(() => [
    { key: "_no",           label: "#",                    width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
    { key: "truckOrderNo",  label: tf("truckOrderNo"),     width: 130, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.truckOrderNo`)}  style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
    { key: "pkgQty",        label: tf("package"),          width: 70,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`truckOrders.${i}.pkgQty`}        decimalPlaces={0} valueAsNumber={false} /> },
    { key: "pkgUnit",       label: tf("unit"),             width: 60,  render: (_, __, i) => <PkgUnitCell index={i} /> },
    { key: "grossWeightKg", label: tf("grossWT"),          width: 90,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`truckOrders.${i}.grossWeightKg`} decimalPlaces={3} valueAsNumber={false} /> },
    { key: "cbm",           label: tf("cbm"),              width: 80,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`truckOrders.${i}.cbm`}           decimalPlaces={3} valueAsNumber={false} /> },
    { key: "truckNo",       label: tf("truckNo"),          width: 110, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.truckNo`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "truckType",     label: tf("type"),             width: 70,  render: (_, __, i) => (
      <Controller
        name={`truckOrders.${i}.truckType`}
        control={control}
        render={({ field }) => (
          <ComboBox variant="cell" options={truckTypeOptions} value={field.value} onChange={field.onChange} />
        )}
      />
    ) },
    { key: "driver",        label: tf("driver"),           width: 120, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.driver`)} /> },
    { key: "mobileNo",      label: tf("mobileNo"),         width: 120, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.mobileNo`)} /> },
    { key: "containerNo",   label: tf("containerNo"),      width: 130, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.containerNo`)}   style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "containerType", label: tf("contType"),         width: 70,  render: (_, __, i) => (
      <Controller
        name={`truckOrders.${i}.containerType`}
        control={control}
        render={({ field }) => (
          <ComboBox variant="cell" options={containerTypeOptions} value={field.value} onChange={field.onChange} />
        )}
      />
    ) },
    { key: "sealNo1",       label: tf("sealNo1"),          width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.sealNo1`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "sealNo2",       label: tf("sealNo2"),          width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.sealNo2`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "sealNo3",       label: tf("sealNo3"),          width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.sealNo3`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
  ], [register, control, truckTypeOptions, containerTypeOptions, tf]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    append({ ...EMPTY_TRUCK_ORDER_ROW });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => (f as unknown as { id: string }).id === focused);
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
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("truckInformation")}</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
          <Button variant="danger" size="sm" iconOnly onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
        </div>
      </div>
      <GridList
        columns={columns}
        data={fields as unknown as TruckOrderRow[]}
        rowKey={(row) => (row as unknown as { id: string }).id}
        onRowClick={(row) => setSelectedKey((row as unknown as { id: string }).id)}
        rowClassName={(row) => (row as unknown as { id: string }).id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
        onClearRow={() => setSelectedKey(null)}
      />
    </div>
  );
}
