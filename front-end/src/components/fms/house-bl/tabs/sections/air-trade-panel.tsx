"use client";

import { useFormContext, Controller } from "react-hook-form";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { TextBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

interface Props { variant?: AnyVariantConfig }

export function AirTradePanel({ variant }: Props) {
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
          <span className="li__label">Currency</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("airDetail.currencyCode")} />
          </div>
        </div>
      ),
    },
    {
      key: "incoterms",
      render: () => (
        <div className="li">
          <span className="li__label">Incoterms</span>
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
          <span className="li__label">Freight Term</span>
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
          <span className="li__label">Other Term</span>
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
          <span className="li__label">D.V Carriage</span>
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
          <span className="li__label">Insurance</span>
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
          <span className="li__label">D.V Customs</span>
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
          <span className="li__label">Account Info</span>
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
        <span className="li__label">F.H.D</span>
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
      label: "Trade Terms",
      render: () => (
        <FieldItemGrid itemScope={`${panelScope}.trade-terms`} items={tradeItems} cols={1} shouldShowRowControls={false} />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Trade</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
