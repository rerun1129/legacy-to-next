import type React from "react";
import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface Props { variant?: AnyVariantConfig }

const LI_ST: React.CSSProperties = { width: "100%", height: 22, padding: "0 8px", fontSize: 10 };

function LiField({
  label,
  name,
}: {
  label: string;
  name?: FieldPath<HouseBlFormValues>;
}) {
  const { register } = useFormContext<HouseBlFormValues>();
  const registerProps = name ? register(name) : {};
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input"><input style={LI_ST} {...registerProps} /></div>
    </div>
  );
}

function FreightTermField({ inputProps }: { inputProps: React.InputHTMLAttributes<HTMLInputElement> }) {
  return (
    <div className="li">
      <span className="li__label">Freight Term</span>
      <div className="li__input"><input style={LI_ST} {...inputProps} /></div>
    </div>
  );
}

export function AirTradePanel({ variant }: Props) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { options: incotermsOptions, placeholder: incotermPlaceholder } = useEnumOptions("Incoterms");

  if (!variant) return null;
  const panelScope = `air-trade-panel.${variant.key}`;
  const isImp      = variant.direction === "IMP";

  const baseItems: FieldItemDef[] = [
    { key: "currency",     render: () => <LiField label="Currency"     name="currency"   /> },
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
    { key: "freight-term", render: () => <FreightTermField inputProps={register("freightTerm")} /> },
    { key: "other-term",   render: () => <LiField label="Other Term"   name="otherTerm"  /> },
    { key: "dv-carriage",  render: () => <LiField label="D.V Carriage" name="dvCarriage" /> },
    { key: "insurance",    render: () => <LiField label="Insurance"    name="insurance"  /> },
    { key: "dv-customs",   render: () => <LiField label="D.V Customs"  name="dvCustoms"  /> },
    { key: "account-info", render: () => <LiField label="Account Info" name="accountInfo" /> },
  ];

  const fhdItem: FieldItemDef = { key: "fhd", render: () => <LiField label="F.H.D" name="fhd" /> };

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
