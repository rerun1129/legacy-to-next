import type React from "react";
import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import { Search } from "lucide-react";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

// ── 공통 헬퍼 ──────────────────────────────────────────────
function LiField({
  label,
  name,
  req,
}: {
  label: string;
  name?: FieldPath<HouseBlFormValues>;
  req?: boolean;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  const registerProps = name ? register(name) : {};
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...registerProps} />
      </div>
    </div>
  );
}

function LcnField({
  label,
  req,
  codeField,
  nameField,
}: {
  label: string;
  req?: boolean;
  codeField: FieldPath<HouseBlFormValues>;
  nameField: FieldPath<HouseBlFormValues>;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  return (
    <div className="lcn">
      <span className={`lcn__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register(codeField)} />
        <Search size={12} className="lcn__icon" />
      </div>
      <input className="lcn__name" placeholder="Name" {...register(nameField)} />
    </div>
  );
}

// ── RHF-bound fields ────────────────────────────────────────
function PaymentTypeField({ inputProps }: { inputProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label is-required">Freight Term</span>
      <div className="li__input">
        <input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...inputProps} />
      </div>
    </div>
  );
}


export function TradePanel({ variant }: { variant?: AnyVariantConfig }) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: incotermsOptions, placeholder: incotermPlaceholder } = useEnumOptions("Incoterms");
  const panelScope = variant ? `trade-panel.${variant.key}` : "trade-panel";

  const tradeTermItems: FieldItemDef[] = [
    {
      key: "incoterms",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Incoterms</span>
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
    { key: "freight-term", render: () => <PaymentTypeField inputProps={register("freightTerm")} /> },
  ];

  const perfItems: FieldItemDef[] = [
    { key: "customer", render: () => <LcnField label="Actual Customer" req codeField="actualCustomerCode" nameField="actualCustomerName" /> },
    { key: "sales",    render: () => <LcnField label="Sales Man"       req codeField="salesManCode"       nameField="salesManName"       /> },
    { key: "operator", render: () => <LcnField label="Operator"        req codeField="operatorCode"       nameField="operatorName"       /> },
    { key: "team",     render: () => <LcnField label="Team"            req codeField="teamCode"           nameField="teamName"           /> },
  ];

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: "Trade Terms",
      render: () => <FieldItemGrid itemScope={`${panelScope}.trade-terms`} items={tradeTermItems} />,
    },
    {
      key:   "performance",
      label: "Performance",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Performance</div>
          <FieldItemGrid itemScope={`${panelScope}.performance`} items={perfItems} />
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
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
