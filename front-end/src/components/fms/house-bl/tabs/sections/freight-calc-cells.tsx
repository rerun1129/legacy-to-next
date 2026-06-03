"use client";

/**
 * Freight 그리드 계산 체인 NumberBox 셀 컴포넌트.
 * qty/price/exchangeRate/settleAmount/localAmount/vat/usdExchangeRate 입력 시
 * 단방향 계산 체인을 실행해 하위 필드만 갱신한다.
 *
 * watch 미사용 — getValues()로 현재 행 읽기, setValue()로 하위만 갱신(형제 focus 보호).
 * register 미사용 — NumberBox 내부 useController가 controlled 등록 담당,
 * onChange prop으로 재계산만 트리거(handleChange가 field.onChange 후 호출).
 */

import { useFormContext, type UseFormSetValue } from "react-hook-form";
import { NumberBox } from "@/components/shared/inputs";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import type { FieldPrefix } from "./freight-cells";
import {
  recalcFromQtyPrice,
  recalcFromSettle,
  recalcFromExchangeRate,
  recalcFromLocal,
  recalcFromUsdExchangeRate,
  type FreightCalcRow,
} from "@/components/fms/house-bl/freight-calc";

// ── 내부 헬퍼 ─────────────────────────────────────────────────

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

// 계산 결과로 나올 수 있는 출력 필드들 (입력 필드인 exchangeRate/usdExchangeRate 제외)
const CALC_OUTPUT_KEYS: (keyof FreightCalcRow)[] = [
  "settleAmount", "localAmount", "usdAmount", "vat",
];

function applyCalcResult(
  setValue: UseFormSetValue<HouseBlFormValues>,
  prefix: FieldPrefix,
  index: number,
  result: Partial<FreightCalcRow>,
) {
  for (const key of CALC_OUTPUT_KEYS) {
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

// ── 셀 컴포넌트 ───────────────────────────────────────────────

interface CalcCellProps {
  prefix: FieldPrefix;
  index: number;
}

export function QtyCell({ prefix, index }: CalcCellProps) {
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.qty`}
      valueAsNumber={false}
      onChange={() => {
        const row = readCalcRow(getValues, prefix, index);
        applyCalcResult(setValue, prefix, index, recalcFromQtyPrice(row));
      }}
    />
  );
}

export function PriceCell({ prefix, index }: CalcCellProps) {
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.price`}
      valueAsNumber={false}
      onChange={() => {
        const row = readCalcRow(getValues, prefix, index);
        applyCalcResult(setValue, prefix, index, recalcFromQtyPrice(row));
      }}
    />
  );
}

export function ExchangeRateCell({ prefix, index }: CalcCellProps) {
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.exchangeRate`}
      valueAsNumber={false}
      onChange={() => {
        const row = readCalcRow(getValues, prefix, index);
        applyCalcResult(setValue, prefix, index, recalcFromExchangeRate(row));
      }}
    />
  );
}

export function SettleAmountCell({ prefix, index }: CalcCellProps) {
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.settleAmount`}
      valueAsNumber={false}
      onChange={() => {
        const row = readCalcRow(getValues, prefix, index);
        applyCalcResult(setValue, prefix, index, recalcFromSettle(row));
      }}
    />
  );
}

export function LocalAmountCell({ prefix, index }: CalcCellProps) {
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.localAmount`}
      valueAsNumber={false}
      onChange={() => {
        const row = readCalcRow(getValues, prefix, index);
        applyCalcResult(setValue, prefix, index, recalcFromLocal(row));
      }}
    />
  );
}

export function VatCell({ prefix, index }: CalcCellProps) {
  // vat는 체인 하단 — 변경해도 재계산 트리거 없음, useController가 값 등록
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.vat`}
      valueAsNumber={false}
    />
  );
}

export function UsdExchangeRateCell({ prefix, index }: CalcCellProps) {
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();
  return (
    <NumberBox
      variant="cell"
      name={`${prefix}.${index}.usdExchangeRate`}
      valueAsNumber={false}
      onChange={() => {
        const row = readCalcRow(getValues, prefix, index);
        applyCalcResult(setValue, prefix, index, recalcFromUsdExchangeRate(row));
      }}
    />
  );
}
