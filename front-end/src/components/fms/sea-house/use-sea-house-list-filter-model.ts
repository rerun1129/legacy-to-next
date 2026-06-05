"use client";

import { useMemo } from "react";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { usePathname } from "next/navigation";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useSeaHouseEnums } from "@/lib/use-sea-house-enums";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { SeaHouseFilter } from "@/domain/sea-house";
import {
  DATE_KIND_OPTIONS,
  MASTER_BL_KIND_OPTIONS,
  PARTY_KIND_OPTIONS,
  PARTNER_KIND_OPTIONS,
  PORT_KIND_OPTIONS,
} from "./sea-house-list-filter-options";
import type { LabelOption } from "@/components/shared/inputs/_types";

export function useSeaHouseListFilterModel(form: UseFormReturn<SeaHouseFilter>) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register, setValue } = form;
  const t = useTranslations("fms.seaHouse.list.filter");

  // labelKey 배열 → 해석된 LabelOption 배열 (useMemo로 t 참조 변경 시에만 재계산)
  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const masterBlKindOptions = useMemo<LabelOption[]>(
    () => MASTER_BL_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const partyKindOptions = useMemo<LabelOption[]>(
    () => PARTY_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const partnerKindOptions = useMemo<LabelOption[]>(
    () => PARTNER_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const portKindOptions = useMemo<LabelOption[]>(
    () => PORT_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );

  const { shipmentType, salesClass, incoterms, loadType } = useSeaHouseEnums();
  const allOption = useMemo(() => ({ value: "", label: t("all") }), [t]);
  const shipmentTypeOptionsWithAll = useMemo(
    () => [allOption, ...shipmentType.options],
    [allOption, shipmentType.options]
  );
  const salesClassOptionsWithAll = useMemo(
    () => [allOption, ...salesClass.options],
    [allOption, salesClass.options]
  );
  const incotermsOptionsWithAll = useMemo(
    () => [allOption, ...incoterms.options],
    [allOption, incoterms.options]
  );
  const loadTypeOptionsWithAll = useMemo(
    () => [allOption, ...loadType.options],
    [allOption, loadType.options]
  );

  // 자동완성 훅 — 소스별 1:1
  const party           = useCodeAutocomplete(CODE_SOURCES.customer);
  const actualCustomer  = useCodeAutocomplete(CODE_SOURCES.customer);
  const partner         = useCodeAutocomplete(CODE_SOURCES.partner);
  const liner           = useCodeAutocomplete(CODE_SOURCES.carrierSea);
  const port            = useCodeAutocomplete(CODE_SOURCES.portSea);
  const operator        = useCodeAutocomplete(CODE_SOURCES.user);
  const salesMan        = useCodeAutocomplete(CODE_SOURCES.user);
  const team            = useCodeAutocomplete(CODE_SOURCES.team);

  return {
    t,
    register,
    setValue,
    dateKindOptions,
    masterBlKindOptions,
    partyKindOptions,
    partnerKindOptions,
    portKindOptions,
    shipmentType,
    salesClass,
    incoterms,
    loadType,
    shipmentTypeOptionsWithAll,
    salesClassOptionsWithAll,
    incotermsOptionsWithAll,
    loadTypeOptionsWithAll,
    party,
    actualCustomer,
    partner,
    liner,
    port,
    operator,
    salesMan,
    team,
  };
}
