"use client";

import { useFormContext, Controller } from "react-hook-form";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function SeaTradePanel() {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: freightTermOptions, placeholder: freightTermPlaceholder } = useEnumOptions("FreightTerm");
  const { options: incotermsOptions, placeholder: incotermPlaceholder } = useEnumOptions("Incoterms");

  const incotermsItem: FieldItemDef = {
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
  };

  const freightTermItem: FieldItemDef = {
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
  };

  const payableAtItem: FieldItemDef = {
    key: "payable-at",
    render: () => (
      <CodeBox
        kind="lcn"
        variant="panel"
        label="Payable At"
        codeProps={{ ...register("seaDetail.payableAt") }}
        nameProps={{ ...register("seaDetail.payableAtName") }}
        onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
      />
    ),
  };

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
      render: () => (
        <>
          <FieldItemGrid itemScope="sea-trade-panel.trade-terms.terms"   items={[incotermsItem, freightTermItem]} cols={2} />
          <FieldItemGrid itemScope="sea-trade-panel.trade-terms.payable" items={[payableAtItem]}                  cols={1} />
        </>
      ),
    },
    {
      key:   "performance",
      label: "Performance",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Performance</div>
          <FieldItemGrid itemScope="sea-trade-panel.performance" items={perfItems} cols={1} />
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
