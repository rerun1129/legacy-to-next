"use client";

/**
 * Freight Selling/Buying 그리드 개별 셀 컴포넌트.
 * 각 셀은 useFormContext 의존 — freight-panels.tsx에서만 사용.
 * 계산 체인 NumberBox 셀은 freight-calc-cells.tsx에 분리.
 */

import { useFormContext, Controller, type UseFormSetValue } from "react-hook-form";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { TextBox, ComboBox, CodeBox, DateBox } from "@/components/shared/inputs";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { useEnumOptions } from "@/application/enums/use-enum";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { ComboBoxOption } from "@/components/shared/inputs/_types";
import { recalcFromTaxType, type FreightCalcRow } from "@/components/fms/house-bl/freight-calc";

/** freightSelling 또는 freightBuying 필드 prefix */
export type FieldPrefix = "freightSelling" | "freightBuying";

// ── FinancialDocType FE 상수 ──────────────────────────────────
// BE에 FinancialDocType enum이 미등록이므로 FE 상수로 정의 (다국어 범위 외 — 영문 라벨)
const FINANCIAL_DOC_OPTIONS_SELLING: ComboBoxOption[] = [
  { value: "INVOICE", label: "Invoice" },
  { value: "DEBIT",   label: "Debit" },
];
const FINANCIAL_DOC_OPTIONS_BUYING: ComboBoxOption[] = [
  { value: "PAYMENT", label: "Payment" },
  { value: "CREDIT",  label: "Credit" },
];

export function getFinancialDocOptions(prefix: FieldPrefix): ComboBoxOption[] {
  return prefix === "freightSelling"
    ? FINANCIAL_DOC_OPTIONS_SELLING
    : FINANCIAL_DOC_OPTIONS_BUYING;
}

// ── TaxType 셀 내부 계산 헬퍼 ────────────────────────────────

function readCalcRow(
  getValues: () => HouseBlFormValues,
  prefix: FieldPrefix,
  index: number,
): FreightCalcRow {
  const rows = getValues()[prefix];
  const row = rows?.[index];
  return {
    qty:             row?.qty,
    price:           row?.price,
    settleAmount:    row?.settleAmount,
    exchangeRate:    row?.exchangeRate,
    localAmount:     row?.localAmount,
    usdExchangeRate: row?.usdExchangeRate,
    usdAmount:       row?.usdAmount,
    taxType:         row?.taxType,
    vat:             row?.vat,
  };
}

// taxType 변경 시 재계산되는 출력 필드들 (vat만)
const TAX_CALC_OUTPUT_KEYS: (keyof FreightCalcRow)[] = ["vat"];

function applyCalcResult(
  setValue: UseFormSetValue<HouseBlFormValues>,
  prefix: FieldPrefix,
  index: number,
  result: Partial<FreightCalcRow>,
) {
  for (const key of TAX_CALC_OUTPUT_KEYS) {
    const val = result[key];
    if (val !== undefined) {
      setValue(
        `${prefix}.${index}.${key}` as Parameters<UseFormSetValue<HouseBlFormValues>>[0],
        val,
        { shouldDirty: true },
      );
    }
  }
}

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
  /** currency 선택 후 헤더 환율 자동 세팅 콜백 */
  onCurrencySelect?: (index: number, currencyCode: string) => void;
}

export function CurrencyCell({ prefix, index, onCurrencySelect }: CurrencyCellProps) {
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
      onSelect={(it) => {
        setValue(`${prefix}.${index}.currency`, it.code);
        onCurrencySelect?.(index, it.code);
      }}
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
  const { control, getValues, setValue } = useFormContext<HouseBlFormValues>();
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
          onChange={(e) => {
            field.onChange(e);
            // taxType 변경 → vat 재계산
            const row = readCalcRow(getValues, prefix, index);
            const updated = recalcFromTaxType({ ...row, taxType: e.target.value });
            applyCalcResult(setValue, prefix, index, updated);
          }}
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

// ── FinancialDocType ComboBox 셀 ──────────────────────────────

interface FinancialDocTypeCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function FinancialDocTypeCell({ prefix, index }: FinancialDocTypeCellProps) {
  const { control } = useFormContext<HouseBlFormValues>();
  const options = getFinancialDocOptions(prefix);
  return (
    <Controller
      name={`${prefix}.${index}.financialDocType`}
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

// ── ReadOnly 계산필드 셀 ──────────────────────────────────────

interface ReadOnlyCellProps {
  /** §A2 BE 산정값 — 저장 전 신규 행은 undefined/빈 문자열, 저장 후 BE 계산값 표시 */
  value?: string | number | null;
}

export function ReadOnlyCell({ value }: ReadOnlyCellProps) {
  return <TextBox variant="cell" readOnly value={value != null ? String(value) : ""} />;
}
