"use client";

import { useFormContext } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function NonBLPartyPanel() {
  const { register, setValue } = useFormContext<NonBlFormValues>();

  const actualCustomer = useCodeAutocomplete(CODE_SOURCES.customer);
  const shipper        = useCodeAutocomplete(CODE_SOURCES.customer);
  const consignee      = useCodeAutocomplete(CODE_SOURCES.customer);
  const notify         = useCodeAutocomplete(CODE_SOURCES.customer);
  const settlePartner  = useCodeAutocomplete(CODE_SOURCES.partner);

  const fields: FieldWidgetDef[] = [
    {
      key:    "actual-customer",
      label:  "ACTUAL CUSTOMER",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="ACTUAL CUSTOMER"
          required
          codeProps={{ ...register("actualCustomerCode") }}
          nameProps={{ ...register("actualCustomerName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={actualCustomer.onSearch}
          suggestions={actualCustomer.suggestions}
          suggestionsLoading={actualCustomer.suggestionsLoading}
          onSelect={(it) => { setValue("actualCustomerCode", it.code); setValue("actualCustomerName", it.name); }}
        />
      ),
    },
    {
      key:    "shipper",
      label:  "SHIPPER",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="SHIPPER"
          codeProps={{ ...register("shipperCode") }}
          nameProps={{ ...register("shipperName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={shipper.onSearch}
          suggestions={shipper.suggestions}
          suggestionsLoading={shipper.suggestionsLoading}
          onSelect={(it) => { setValue("shipperCode", it.code); setValue("shipperName", it.name); }}
        />
      ),
    },
    {
      key:    "consignee",
      label:  "CONSIGNEE",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="CONSIGNEE"
          codeProps={{ ...register("consigneeCode") }}
          nameProps={{ ...register("consigneeName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={consignee.onSearch}
          suggestions={consignee.suggestions}
          suggestionsLoading={consignee.suggestionsLoading}
          onSelect={(it) => { setValue("consigneeCode", it.code); setValue("consigneeName", it.name); }}
        />
      ),
    },
    {
      key:    "notify",
      label:  "NOTIFY",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="NOTIFY"
          codeProps={{ ...register("notifyCode") }}
          nameProps={{ ...register("notifyName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={notify.onSearch}
          suggestions={notify.suggestions}
          suggestionsLoading={notify.suggestionsLoading}
          onSelect={(it) => { setValue("notifyCode", it.code); setValue("notifyName", it.name); }}
        />
      ),
    },
    {
      key:    "settle-partner",
      label:  "SETTLE PARTNER",
      render: () => (
        <CodeBox
          variant="panel"
          kind="party-cn"
          label="SETTLE PARTNER"
          codeProps={{ ...register("settlePartnerCode") }}
          nameProps={{ ...register("settlePartnerName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
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
        <span className="panel__title">Party</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldWidgetList panelScope="nonbl-party-panel" fields={fields} />
      </div>
    </div>
  );
}
