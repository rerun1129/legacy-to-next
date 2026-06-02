"use client";

import { useFormContext } from "react-hook-form";
import { useTranslations } from "next-intl";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import { CodeBox } from "@/components/shared/inputs";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function AirPerformancePanel() {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { register, setValue } = useFormContext<HouseBlFormValues>();

  const actualCustomer = useCodeAutocomplete(CODE_SOURCES.customer);
  const settlePartner  = useCodeAutocomplete(CODE_SOURCES.partner);

  const PERF_ITEMS: FieldItemDef[] = [
    {
      key: "actual-customer",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label={tf("actualCustomer")}
          required
          codeProps={{ ...register("actualCustomerCode") }}
          nameProps={{ ...register("actualCustomerName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={actualCustomer.onSearch}
          suggestions={actualCustomer.suggestions}
          suggestionsLoading={actualCustomer.suggestionsLoading}
          onSelect={(it) => { setValue("actualCustomerCode", it.code); setValue("actualCustomerName", it.name); }}
        />
      ),
    },
    {
      key: "settle-partner",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label={tf("settlePartner")}
          codeProps={{ ...register("settlePartnerCode") }}
          nameProps={{ ...register("settlePartnerName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={settlePartner.onSearch}
          suggestions={settlePartner.suggestions}
          suggestionsLoading={settlePartner.suggestionsLoading}
          onSelect={(it) => { setValue("settlePartnerCode", it.code); setValue("settlePartnerName", it.name); }}
        />
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("performance")}</span>
      </div>
      <div className="panel__body panel__body--scroll-flex2">
        <FieldItemGrid itemScope="air-performance-panel" items={PERF_ITEMS} cols={2} />
      </div>
    </div>
  );
}
