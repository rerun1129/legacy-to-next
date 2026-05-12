"use client";

import { useMemo, useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { EMPTY_TRUCK_ORDER_ROW } from "@/components/fms/truck-bl/truck-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";

type TruckOrderRow = NonNullable<TruckBlFormValues["truckOrders"]>[number];

export function TruckOrderGridPanel() {
  const { control, register } = useFormContext<TruckBlFormValues>();
  const { options: truckTypeOptions }     = useEnumOptions("TruckType");
  const { options: containerTypeOptions } = useEnumOptions("ContainerType");
  const { fields, append, remove } = useFieldArray({ control, name: "truckOrders" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  const columns = useMemo<GridColumn<TruckOrderRow>[]>(() => [
    { key: "_no",           label: "#",              width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
    { key: "truckOrderNo",  label: "Truck Order No", width: 130, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.truckOrderNo`)}  style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
    { key: "pkgQty",        label: "Package",        width: 70,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`truckOrders.${i}.pkgQty`}        decimalPlaces={0} valueAsNumber={false} /> },
    { key: "pkgUnit",       label: "Unit",           width: 60,  render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.pkgUnit`)} /> },
    { key: "grossWeightKg", label: "Gross W/T",      width: 90,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`truckOrders.${i}.grossWeightKg`} decimalPlaces={3} valueAsNumber={false} /> },
    { key: "cbm",           label: "CBM",            width: 80,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`truckOrders.${i}.cbm`}           decimalPlaces={3} valueAsNumber={false} /> },
    { key: "truckNo",       label: "Truck No.",      width: 110, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.truckNo`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "truckType",     label: "Type",           width: 70,  render: (_, __, i) => <ComboBox  variant="cell" options={truckTypeOptions} {...register(`truckOrders.${i}.truckType`)} /> },
    { key: "driver",        label: "Driver",         width: 120, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.driver`)} /> },
    { key: "mobileNo",      label: "Mobile No",      width: 120, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.mobileNo`)} /> },
    { key: "containerNo",   label: "Container No.",  width: 130, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.containerNo`)}   style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "containerType", label: "Cont. Type",     width: 70,  render: (_, __, i) => <ComboBox  variant="cell" options={containerTypeOptions} {...register(`truckOrders.${i}.containerType`)} /> },
    { key: "sealNo1",       label: "Seal No.1",      width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.sealNo1`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "sealNo2",       label: "Seal No.2",      width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.sealNo2`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "sealNo3",       label: "Seal No.3",      width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`truckOrders.${i}.sealNo3`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
  ], [register, truckTypeOptions, containerTypeOptions]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    append({ ...EMPTY_TRUCK_ORDER_ROW });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    remove(targetIdx);
    setSelectedKey(null);
  }

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Truck Information</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm btn--icon btn--success" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--icon btn--danger" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
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
