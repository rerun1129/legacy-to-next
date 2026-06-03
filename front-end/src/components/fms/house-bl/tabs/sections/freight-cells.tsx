"use client";

/**
 * Freight Selling/Buying 그리드 개별 셀 컴포넌트.
 * 각 셀은 useFormContext 의존 — freight-panels.tsx에서만 사용.
 */

import { useFormContext, Controller } from "react-hook-form";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { TextBox, ComboBox, CodeBox, DateBox } from "@/components/shared/inputs";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { useEnumOptions } from "@/application/enums/use-enum";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { ComboBoxOption } from "@/components/shared/inputs/_types";

/** freightSelling 또는 freightBuying 필드 prefix */
export type FieldPrefix = "freightSelling" | "freightBuying";

// ── FreightCode 셀 ────────────────────────────────────────────

interface FreightCodeCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function FreightCodeCell({ prefix, index }: FreightCodeCellProps) {
  const { register, setValue } = useFormContext<HouseBlFormValues>();
  const freight = useCodeAutocomplete(CODE_SOURCES.freight);
  return (
    <CodeBox
      kind="code-only"
      variant="cell"
      codeProps={{ ...register(`${prefix}.${index}.freightCode`) }}
      onLookup={() => {}}
      onSearch={freight.onSearch}
      suggestions={freight.suggestions}
      suggestionsLoading={freight.suggestionsLoading}
      onSelect={(it) => {
        setValue(`${prefix}.${index}.freightCode`, it.code);
        setValue(`${prefix}.${index}.freightName`, it.name);
      }}
    />
  );
}

// ── Currency 셀 ───────────────────────────────────────────────

interface CurrencyCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function CurrencyCell({ prefix, index }: CurrencyCellProps) {
  const { register, setValue } = useFormContext<HouseBlFormValues>();
  const currency = useCodeAutocomplete(CODE_SOURCES.currency);
  return (
    <CodeBox
      kind="code-only"
      variant="cell"
      codeProps={{ ...register(`${prefix}.${index}.currency`) }}
      onLookup={() => {}}
      onSearch={currency.onSearch}
      suggestions={currency.suggestions}
      suggestionsLoading={currency.suggestionsLoading}
      onSelect={(it) => { setValue(`${prefix}.${index}.currency`, it.code); }}
    />
  );
}

// ── Customer 셀 ───────────────────────────────────────────────

interface CustomerCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function CustomerCell({ prefix, index }: CustomerCellProps) {
  const { register, setValue } = useFormContext<HouseBlFormValues>();
  const customer = useCodeAutocomplete(CODE_SOURCES.customer);
  return (
    <CodeBox
      kind="code-only"
      variant="cell"
      codeProps={{ ...register(`${prefix}.${index}.customerCode`) }}
      onLookup={() => {}}
      onSearch={customer.onSearch}
      suggestions={customer.suggestions}
      suggestionsLoading={customer.suggestionsLoading}
      onSelect={(it) => {
        setValue(`${prefix}.${index}.customerCode`, it.code);
        setValue(`${prefix}.${index}.customerName`, it.name);
      }}
    />
  );
}

// ── Per 셀 ────────────────────────────────────────────────────

interface PerCellProps {
  prefix: FieldPrefix;
  index: number;
  perOptions: ComboBoxOption[];
  /** per 선택 시 qty 스냅샷 setValue 콜백 */
  onPerChange: (index: number, value: string) => void;
  /** 셀 표시용 라벨 해석 함수 */
  resolveLabel: (code: string) => string;
}

export function PerCell({ prefix, index, perOptions, onPerChange, resolveLabel }: PerCellProps) {
  const { control } = useFormContext<HouseBlFormValues>();
  return (
    <Controller
      name={`${prefix}.${index}.per`}
      control={control}
      render={({ field }) => {
        // 저장된 값이 현재 perOptions에 없을 수 있음(컨테이너 삭제 등) — 저장값 기준 표시 유지
        const currentValue = field.value ?? "";
        const hasCurrentInOptions = perOptions.some((o) => o.value === currentValue);
        const baseOptions = perOptions.map((o) => ({
          ...o,
          label: o.value === currentValue ? resolveLabel(currentValue) : o.label,
        }));
        const displayOptions =
          currentValue && !hasCurrentInOptions
            ? [{ value: currentValue, label: resolveLabel(currentValue) }, ...baseOptions]
            : baseOptions;
        return (
          <ComboBox
            variant="cell"
            options={displayOptions}
            value={currentValue}
            onChange={(e) => {
              field.onChange(e);
              onPerChange(index, e.target.value);
            }}
          />
        );
      }}
    />
  );
}

// ── TaxType 셀 ───────────────────────────────────────────────

interface TaxTypeCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function TaxTypeCell({ prefix, index }: TaxTypeCellProps) {
  const { control } = useFormContext<HouseBlFormValues>();
  // §A2: BE TaxType enum 등록 완료 — useEnumOptions로 전환 (labelKo 미설정 시 영문 label 표시)
  const { options } = useEnumOptions("TaxType");
  return (
    <Controller
      name={`${prefix}.${index}.taxType`}
      control={control}
      render={({ field }) => (
        <ComboBox
          variant="cell"
          options={options}
          value={field.value ?? ""}
          onChange={field.onChange}
        />
      )}
    />
  );
}

// ── PerformanceDt 셀 ─────────────────────────────────────────

interface PerformanceDtCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function PerformanceDtCell({ prefix, index }: PerformanceDtCellProps) {
  const { control } = useFormContext<HouseBlFormValues>();
  return (
    <Controller
      name={`${prefix}.${index}.performanceDt`}
      control={control}
      render={({ field }) => (
        <DateBox
          variant="cell"
          name={field.name}
          value={(field.value as string) ?? ""}
          onChange={field.onChange}
          onBlur={field.onBlur}
        />
      )}
    />
  );
}

// ── ReadOnly 계산필드 셀 ──────────────────────────────────────

interface ReadOnlyCellProps {
  /** §A2 BE 산정값 — 저장 전 신규 행은 undefined/빈 문자열, 저장 후 BE 계산값 표시 */
  value?: string | number | null;
}

export function ReadOnlyCell({ value }: ReadOnlyCellProps) {
  return <TextBox variant="cell" readOnly value={value != null ? String(value) : ""} />;
}
