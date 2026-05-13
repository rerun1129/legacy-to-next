"use client";

import { useFormContext, Controller } from "react-hook-form";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { TextBox, CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function SeaTradePanel() {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: freightTermOptions, placeholder: freightTermPlaceholder } = useEnumOptions("FreightTerm");

  const tradeTermItems: FieldItemDef[] = [
    {
      key: "incoterms",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Incoterms</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("incoterms")} />
          </div>
        </div>
      ),
    },
    {
      key: "freight-term",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Freight Term</span>
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
      key: "payable-at",
      render: () => (
        <div className="li">
          <span className="li__label">Payable At</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("paymentPlace")} />
          </div>
        </div>
      ),
    },
    {
      key: "co-load",
      render: () => (
        <div className="li">
          <span className="li__label">Co-Load</span>
          <div className="li__input">
            <TextBox variant="panel" {...register("coLoad")} />
          </div>
        </div>
      ),
    },
  ];

  const perfItems: FieldItemDef[] = [
    {
      key: "customer",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="Actual Customer"
          required
          codeProps={{ ...register("actualCustomerCode") }}
          nameProps={{ ...register("actualCustomerName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
    {
      // settlePartnerName: schema에 존재 — nameProps 연결
      key: "settle-partner",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="Settle Partner"
          codeProps={{ ...register("settlePartnerCode") }}
          nameProps={{ ...register("settlePartnerName") }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
  ];

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: "Trade Terms",
      render: () => <FieldItemGrid itemScope="sea-trade-panel.trade-terms" items={tradeTermItems} />,
    },
    {
      key:   "performance",
      label: "Performance",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Performance</div>
          <FieldItemGrid itemScope="sea-trade-panel.performance" items={perfItems} />
        </>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Trade & Performance</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="sea-trade-panel" fields={fields} />
      </div>
    </div>
  );
}
