"use client";

import { useFormContext } from "react-hook-form";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import { CodeBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function TruckPerformancePanel() {
  const { register, setValue } = useFormContext<TruckBlFormValues>();
  const actualCustomer = useCodeAutocomplete(CODE_SOURCES.customer);
  const settlePartner  = useCodeAutocomplete(CODE_SOURCES.partner);
  const salesMan       = useCodeAutocomplete(CODE_SOURCES.user);
  const operator       = useCodeAutocomplete(CODE_SOURCES.user);
  const team           = useCodeAutocomplete(CODE_SOURCES.team);

  const PERF_ITEMS: FieldItemDef[] = [
    {
      key: "actual-customer",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Actual Customer"
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
          label="Settle Partner"
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
    {
      key: "sales-man",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Sales Man"
          required
          codeProps={{ ...register("salesManCode") }}
          nameProps={{ ...register("salesManName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={salesMan.onSearch}
          suggestions={salesMan.suggestions}
          suggestionsLoading={salesMan.suggestionsLoading}
          onSelect={(it) => { setValue("salesManCode", it.code); setValue("salesManName", it.name); }}
        />
      ),
    },
    {
      key: "operator",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Operator"
          required
          codeProps={{ ...register("operatorCode") }}
          nameProps={{ ...register("operatorName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={operator.onSearch}
          suggestions={operator.suggestions}
          suggestionsLoading={operator.suggestionsLoading}
          onSelect={(it) => { setValue("operatorCode", it.code); setValue("operatorName", it.name); }}
        />
      ),
    },
    {
      key: "team",
      render: () => (
        <CodeBox
          kind="lcn"
          label="Team"
          required
          codeProps={{ ...register("teamCode") }}
          nameProps={{ ...register("teamName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={team.onSearch}
          suggestions={team.suggestions}
          suggestionsLoading={team.suggestionsLoading}
          onSelect={(it) => { setValue("teamCode", it.code); setValue("teamName", it.name); }}
        />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="truck-performance-panel" items={PERF_ITEMS} cols={1} />
      </div>
    </div>
  );
}
