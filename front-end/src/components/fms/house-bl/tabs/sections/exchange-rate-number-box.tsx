"use client";

import { useRef, useState, useCallback } from "react";
import { useFormContext } from "react-hook-form";
import type { Path } from "react-hook-form";
import { useTranslations } from "next-intl";
import { NumberBox } from "@/components/shared/inputs";
import { CodeBoxSuggestions } from "@/components/shared/inputs/code-box-suggestions";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { exchangeRateUseCases } from "@/application/code/exchange-rate/use-cases";
import { BASE_CURRENCY } from "@/adapter/out/api/code/exchange-rate";

interface ExchangeRateNumberBoxProps {
  rateName: Path<HouseBlFormValues>;
  dateName: Path<HouseBlFormValues>;
  /** currencyName 또는 fixedCurrency 중 하나를 반드시 전달 */
  currencyName?: Path<HouseBlFormValues>;
  fixedCurrency?: string;
  decimalPlaces?: number;
}

/** kind 문자열을 i18n 라벨로 변환. 메시지 누락 시 kind 원문 fallback. */
function kindLabel(kind: string, t: (key: string) => string): string {
  try {
    return t(`exRate.kinds.${kind}`);
  } catch {
    return kind;
  }
}

/**
 * 환율 NumberBox wrapper.
 * ArrowDown 시 (일자+통화)로 ADMIN exchange_rate를 조회해 드롭다운을 띄우고,
 * 선택 시 환율 필드를 채운다.
 */
export function ExchangeRateNumberBox({
  rateName,
  dateName,
  currencyName,
  fixedCurrency,
  decimalPlaces,
}: ExchangeRateNumberBoxProps) {
  const t = useTranslations("fms.houseBl.entry.freight");
  const { getValues, setValue } = useFormContext<HouseBlFormValues>();

  const anchorRef = useRef<HTMLDivElement | null>(null);
  const dropdownRef = useRef<HTMLDivElement | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);
  const [items, setItems] = useState<CodeBoxSuggestion[]>([]);
  const [loading, setLoading] = useState(false);

  const handleSelect = useCallback(
    (item: CodeBoxSuggestion) => {
      setValue(rateName, item.code);
      setIsOpen(false);
      // NumberBox는 focus 중 외부 setValue를 DOM에 반영하지 않으므로(caret 보존 가드),
      // 선택 직후 blur로 가드를 해제해 값이 즉시 표시되도록 한다.
      inputRef.current?.blur();
    },
    [rateName, setValue]
  );

  const handleKeyDown = useCallback(
    async (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "ArrowDown") {
        if (!isOpen) {
          // 닫힘 상태에서 ArrowDown: date·currency 둘 다 있을 때만 조회
          // watch 구독 없이 getValues로 즉시 읽어 렌더 중 폼 전체 리렌더를 방지
          const date = getValues(dateName) as string | undefined;
          const currency = currencyName ? (getValues(currencyName) as string | undefined) : fixedCurrency;
          if (!date || !currency) return;
          e.preventDefault();
          setLoading(true);
          setIsOpen(true);
          setActiveIndex(0);
          try {
            const rates = await exchangeRateUseCases.findRatesByDateCurrency(date, currency, BASE_CURRENCY);
            setItems(rates.map((r) => ({ code: String(r.rate), name: kindLabel(r.kind, t) })));
          } finally {
            setLoading(false);
          }
          return;
        }
        // 열린 상태에서 ArrowDown: 다음 항목으로 이동
        e.preventDefault();
        setActiveIndex((prev) => Math.min(prev + 1, items.length - 1));
        return;
      }

      if (e.key === "ArrowUp") {
        if (!isOpen) return;
        e.preventDefault();
        setActiveIndex((prev) => Math.max(prev - 1, 0));
        return;
      }

      if (e.key === "Enter") {
        if (isOpen && items[activeIndex]) {
          e.preventDefault();
          handleSelect(items[activeIndex]);
        }
        return;
      }

      if (e.key === "Escape") {
        setIsOpen(false);
      }
    },
    [isOpen, items, activeIndex, getValues, dateName, currencyName, fixedCurrency, handleSelect, t]
  );

  const handleBlur = useCallback(() => {
    setIsOpen(false);
  }, []);

  return (
    <div ref={anchorRef} style={{ position: "relative", width: "100%" }}>
      <NumberBox
        ref={inputRef}
        variant="panel"
        name={rateName}
        valueAsNumber={false}
        decimalPlaces={decimalPlaces ?? 4}
        onKeyDown={handleKeyDown}
        onBlur={handleBlur}
        style={{ width: "100%" }}
      />
      <CodeBoxSuggestions
        items={items}
        loading={loading}
        activeIndex={activeIndex}
        onSelect={handleSelect}
        visible={isOpen}
        expandCount={0}
        onExpand={() => {}}
        anchorRef={anchorRef}
        dropdownRef={dropdownRef}
      />
    </div>
  );
}
