"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { TextBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

interface Props { variant?: AnyVariantConfig }

function CurrencyField() {
  const { register, setValue } = useFormContext<HouseBlFormValues>();
  const currency = useCodeAutocomplete(CODE_SOURCES.currency);
  return (
    <CodeBox
      kind="code-only"
      variant="panel"
      codeProps={{ ...register("airDetail.currencyCode") }}
      onLookup={() => {}}
      onSearch={currency.onSearch}
      suggestions={currency.suggestions}
      suggestionsLoading={currency.suggestionsLoading}
      onSelect={(it) => { setValue("airDetail.currencyCode", it.code); }}
    />
  );
}

export function AirTradePanel({ variant }: Props) {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: incotermsOptions, placeholder: incotermPlaceholder } = useEnumOptions("Incoterms");
  const { options: freightTermOptions, placeholder: freightTermPlaceholder } = useEnumOptions("FreightTerm");
  const { options: fhdOptions, placeholder: fhdPlaceholder } = useEnumOptions("Fhd");

  if (!variant) return null;
  const panelScope = `air-trade-panel.${variant.key}`;
  const isImp      = variant.direction === "IMP";

  const baseItems: FieldItemDef[] = [
    {
      key: "currency",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("currency")}</span>
          <div className="li__input">
            <CurrencyField />
          </div>
        </div>
      ),
    },
    {
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
    },
    {
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
    },
    {
      key: "other-term",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("otherTerm")}</span>
          <div className="li__input">
            <Controller
              name="airDetail.otherTerm"
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
    },
    {
      key: "dv-carriage",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("dvCarriage")}</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("airDetail.declaredValueCarriage")} />
          </div>
        </div>
      ),
    },
    {
      key: "insurance",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("insurance")}</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("airDetail.insurance")} />
          </div>
        </div>
      ),
    },
    {
      key: "dv-customs",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("dvCustoms")}</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("airDetail.declaredValueCustoms")} />
          </div>
        </div>
      ),
    },
    {
      key: "account-info",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("accountInfo")}</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("airDetail.accountInformation")} />
          </div>
        </div>
      ),
    },
  ];

  const fhdItem: FieldItemDef = {
    key: "fhd",
    render: () => (
      <div className="li">
        <span className="li__label">{tf("fhd")}</span>
        <div className="li__input">
          <Controller
            name="airDetail.fhd"
            control={control}
            render={({ field }) => (
              <ComboBox
                variant="panel"
                options={fhdOptions}
                placeholder={fhdPlaceholder}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
    ),
  };

  const tradeItems: FieldItemDef[] = isImp ? [...baseItems, fhdItem] : baseItems;

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: tf("tradeTerms"),
      render: () => (
        <FieldItemGrid itemScope={`${panelScope}.trade-terms`} items={tradeItems} cols={2} />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tp("trade")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
