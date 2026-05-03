"use client";

import { useFormContext } from "react-hook-form";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

interface Props { variant?: AnyVariantConfig }

const LI_ST: React.CSSProperties = { width: "100%", height: 24, padding: "0 6px", fontSize: 10 };
const UNIT_SEL: React.CSSProperties = {
  height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

export function AirCargoPanel({ variant }: Props) {
  const { register } = useFormContext<HouseBlFormValues>();
  const panelScope = variant ? `air-cargo-panel.${variant.key}` : "air-cargo-panel";

  const cargoItems: FieldItemDef[] = [
    {
      key: "packages",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input" style={{ display: "flex", gap: 4 }}>
            <input
              type="number"
              step="1"
              style={{ flex: 1, height: 24, padding: "0 6px", fontSize: 10 }}
              {...register("pkgQty")}
            />
            <select style={UNIT_SEL} {...register("pkgUnit")}>
              <option value=""></option>
              <option>CTN</option><option>PKG</option><option>BAG</option>
              <option>PLT</option><option>BOX</option><option>PCS</option><option>ROL</option>
            </select>
          </div>
        </div>
      ),
    },
    {
      key: "gross-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Gross W/T</span>
          <div className="li__input" style={{ display: "flex", gap: 4 }}>
            <input
              type="number"
              step="any"
              style={{ ...LI_ST, flex: 1, width: undefined }}
              {...register("grossWeightKg")}
            />
            <select defaultValue="" style={UNIT_SEL}><option value=""></option><option>KGS</option><option>LBS</option></select>
          </div>
        </div>
      ),
    },
    {
      key: "volume-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Volume W/T</span>
          <div className="li__input">
            <input type="number" step="any" style={LI_ST} {...register("volumeWeightKg")} />
          </div>
        </div>
      ),
    },
    {
      key: "charge-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Charge W/T</span>
          <div className="li__input">
            <input type="number" step="any" style={LI_ST} {...register("chargeWeightKg")} />
          </div>
        </div>
      ),
    },
    {
      key: "rate-class",
      render: () => (
        <div className="li">
          <span className="li__label">Rate Class</span>
          <div className="li__input">
            <input type="text" style={LI_ST} {...register("rateClass")} />
          </div>
        </div>
      ),
    },
    {
      key: "cbm",
      render: () => (
        <div className="li">
          <span className="li__label">CBM</span>
          <div className="li__input">
            <input type="number" step="any" style={LI_ST} {...register("cbm")} />
          </div>
        </div>
      ),
    },
  ];

  const fields: FieldWidgetDef[] = [
    {
      key:   "cargo",
      label: "Cargo",
      render: () => (
        <FieldItemGrid itemScope={`${panelScope}.cargo`} items={cargoItems} cols={1} shouldShowRowControls={false} />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
