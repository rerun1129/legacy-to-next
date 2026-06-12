"use client";

import { useLocale, useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import type { ComboBoxOption } from "@/components/shared/inputs/_types";
import { fetchBmsEnum } from "@/adapter/out/api/bms/enums";

export const bmsEnumKeys = {
  all: ["bms-enums"] as const,
  one: (name: string) => [...bmsEnumKeys.all, name] as const,
};

export function useBmsEnumOptions(name: string): {
  options: ComboBoxOption[];
  isLoading: boolean;
  error: unknown;
  placeholder: string | undefined;
} {
  const locale = useLocale();
  const t = useTranslations("common");
  const { data, isLoading, error } = useQuery({
    queryKey: bmsEnumKeys.one(name),
    queryFn: () => fetchBmsEnum(name),
    // PMS(Infinity)와 달리 BMS enum은 admin DB 공통코드로 동적 관리되므로 5분 캐싱
    // FMS use-enum.ts 와 동일 staleTime 정책
    staleTime: 300_000,
    retry: false,
  });
  return {
    options:
      data?.map((o) => ({
        value: o.code,
        label: locale === "ko" && o.labelKo ? o.labelKo : o.label,
      })) ?? [],
    isLoading,
    error,
    placeholder: isLoading ? t("loading") : undefined,
  };
}
