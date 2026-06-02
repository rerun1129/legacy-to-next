"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function SeaTradePanel() {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();

  const payableAt      = useCodeAutocomplete(CODE_SOURCES.portSea);
  const actualCustomer = useCodeAutocomplete(CODE_SOURCES.customer);
  const settlePartner  = useCodeAutocomplete(CODE_SOURCES.partner);
  const { options: freightTermOptions, placeholder: freightTermPlaceholder } = useEnumOptions("FreightTerm");
  const { options: incotermsOptions, placeholder: incotermPlaceholder } = useEnumOptions("Incoterms");

  const incotermsItem: FieldItemDef = {
    key: "incoterms",
    render: () => (
      <div className="li">
        <span className="li__label">{tf("incoterms")}</span>
        <div className="li__input">
          <Controller
            name="incoterms"
            control={control}
            render={({ field }) => (
              <ComboBox
                variant="panel"
                options={incotermsOptions}
                placeholder={incotermPlaceholder}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
    ),
  };

  const freightTermItem: FieldItemDef = {
    key: "freight-term",
    render: () => (
      <div className="li">
        <span className="li__label">{tf("freightTerm")}</span>
        <div className="li__input">
          <Controller
            name="freightTerm"
            control={control}
            render={({ field }) => (
              <ComboBox
                variant="panel"
                options={freightTermOptions}
                placeholder={freightTermPlaceholder}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
    ),
  };

  const payableAtItem: FieldItemDef = {
    key: "payable-at",
    fullWidth: true,
    render: () => (
      <CodeBox
        kind="lcn"
        variant="panel"
        label={tf("payableAt")}
        codeProps={{ ...register("seaDetail.payableAt") }}
        nameProps={{ ...register("seaDetail.payableAtName") }}
        onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        onSearch={payableAt.onSearch}
        suggestions={payableAt.suggestions}
        suggestionsLoading={payableAt.suggestionsLoading}
        onSelect={(it) => { setValue("seaDetail.payableAt", it.code); setValue("seaDetail.payableAtName", it.name); }}
      />
    ),
  };

  const perfItems: FieldItemDef[] = [
    {
      key: "customer",
      fullWidth: true,
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
      // settlePartnerName: schema에 존재 — nameProps 연결
      key: "settle-partner",
      fullWidth: true,
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

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: tf("tradeTerms"),
      render: () => (
        <>
          <FieldItemGrid itemScope="sea-trade-panel.trade-terms.terms"   items={[incotermsItem, freightTermItem]} cols={2} />
          <FieldItemGrid itemScope="sea-trade-panel.trade-terms.payable" items={[payableAtItem]}                  cols={2} />
        </>
      ),
    },
    {
      key:   "performance",
      label: tf("performance"),
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />{tf("performance")}</div>
          <FieldItemGrid itemScope="sea-trade-panel.performance" items={perfItems} cols={2} />
        </>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("tradePerf")}</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="sea-trade-panel" fields={fields} />
      </div>
    </div>
  );
}
