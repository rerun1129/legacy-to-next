"use client";

import { useMemo, useRef, useState } from "react";
import { useFormContext, useFieldArray, useWatch, Controller, type Control } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { Button } from "@/components/shared/button";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

type ChargeRow = NonNullable<MasterBlFormValues["airCharges"]>[number];

const EMPTY_CHARGE_ROW: ChargeRow = {
  freightCode:    undefined,
  currencyCode:   undefined,
  per:            undefined,
  freightTerm:    undefined,
  grossWeightKg:  undefined,
  rateClass:      undefined,
  chargeWeightKg: undefined,
  rate:           undefined,
};

function CurrencyCell({ index }: { index: number }) {
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const currency = useCodeAutocomplete(CODE_SOURCES.currency);
  return (
    <CodeBox
      kind="code-only"
      variant="cell"
      codeProps={{ ...register(`airCharges.${index}.currencyCode`) }}
      onLookup={() => {}}
      onSearch={currency.onSearch}
      suggestions={currency.suggestions}
      suggestionsLoading={currency.suggestionsLoading}
      onSelect={(it) => { setValue(`airCharges.${index}.currencyCode`, it.code); }}
    />
  );
}

function TotalAmountCell({ control, index }: { control: Control<MasterBlFormValues>; index: number }) {
  const rate  = useWatch({ control, name: `airCharges.${index}.rate` });
  const chgWt = useWatch({ control, name: `airCharges.${index}.chargeWeightKg` });
  const total = (Number(rate) || 0) * (Number(chgWt) || 0);
  return <NumberBox variant="cell" readOnly value={total ? total.toFixed(2) : ""} />;
}

export function MasterAirChargeInfoPanel() {
  const { control, register } = useFormContext<MasterBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "airCharges" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);

  const { options: perOptions, placeholder: perPlaceholder }             = useEnumOptions("Per");
  const { options: rateClassOptions, placeholder: rateClassPlaceholder } = useEnumOptions("RateClass");

  const columns = useMemo<GridColumn<ChargeRow>[]>(() => [
    {
      key: "_no",
      label: "#",
      className: "row-num",
      width: 40,
      render: (_, __, i) => i + 1,
    },
    {
      key: "freightCode",
      label: "Freight",
      width: 70,
      render: (_, __, i) => <TextBox variant="cell" {...register(`airCharges.${i}.freightCode`)} />,
    },
    {
      key: "_freightName",
      label: "Freight Name",
      width: 160,
      render: () => <TextBox variant="cell" readOnly value="" />,
    },
    {
      key: "currencyCode",
      label: "Currency",
      width: 60,
      render: (_, __, i) => <CurrencyCell index={i} />,
    },
    {
      key: "per",
      label: "Per",
      width: 70,
      render: (_, __, i) => (
        <Controller
          name={`airCharges.${i}.per`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={perOptions}
              placeholder={perPlaceholder}
              value={field.value}
              onChange={field.onChange}
            />
          )}
        />
      ),
    },
    {
      key: "grossWeightKg",
      label: "Gross W/T",
      className: "is-num",
      width: 70,
      render: (_, __, i) => (
        <NumberBox variant="cell" name={`airCharges.${i}.grossWeightKg`} valueAsNumber={false} decimalPlaces={3} />
      ),
    },
    {
      key: "rateClass",
      label: "Rate Class",
      width: 60,
      render: (_, __, i) => (
        <Controller
          name={`airCharges.${i}.rateClass`}
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="cell"
              options={rateClassOptions}
              placeholder={rateClassPlaceholder}
              value={field.value}
              onChange={field.onChange}
            />
          )}
        />
      ),
    },
    {
      key: "chargeWeightKg",
      label: "Charge W/T",
      className: "is-num",
      width: 70,
      render: (_, __, i) => (
        <NumberBox variant="cell" name={`airCharges.${i}.chargeWeightKg`} valueAsNumber={false} decimalPlaces={3} />
      ),
    },
    {
      key: "rate",
      label: "Rate",
      className: "is-num",
      width: 70,
      render: (_, __, i) => (
        <NumberBox variant="cell" name={`airCharges.${i}.rate`} valueAsNumber={false} decimalPlaces={2} />
      ),
    },
    {
      key: "_total",
      label: "Total Amount",
      className: "is-num",
      width: 90,
      render: (_, __, i) => <TotalAmountCell control={control} index={i} />,
    },
  ], [register, control, perOptions, perPlaceholder, rateClassOptions, rateClassPlaceholder]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    append({ ...EMPTY_CHARGE_ROW });
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
        <span className="panel__title">Charge Information</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
          <Button variant="danger" size="sm" iconOnly onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
        </div>
      </div>
      <GridList
        columns={columns}
        data={fields as unknown as ChargeRow[]}
        rowKey={(r) => String((r as unknown as { id: string }).id)}
        onRowClick={(r) => setSelectedKey((r as unknown as { id: string }).id)}
        rowClassName={(r) => (r as unknown as { id: string }).id === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey(null)}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
