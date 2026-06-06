"use client";

import { useLocale, useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import type { ComboBoxOption } from "@/components/shared/inputs/_types";
import { fetchPmsEnum } from "@/adapter/out/api/pms/enums";

export const pmsEnumKeys = {
  all: ["pms-enums"] as const,
  one: (name: string) => [...pmsEnumKeys.all, name] as const,
};

export function usePmsEnumOptions(name: string): {
  options: ComboBoxOption[];
  isLoading: boolean;
  error: unknown;
  placeholder: string | undefined;
} {
  const locale = useLocale();
  const t = useTranslations("common");
  const { data, isLoading, error } = useQuery({
    queryKey: pmsEnumKeys.one(name),
    queryFn: () => fetchPmsEnum(name),
    // PMS ENUM 값은 배포 중 변경되지 않으므로 무기한 캐싱
    staleTime: Infinity,
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
