"use client";

import { useFormContext } from "react-hook-form";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLCargoPanel() {
  const { register } = useFormContext<NonBlFormValues>();

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "main-item",
      render: () => (
        <div className="li">
          <span className="li__label">Main Item</span>
          <div className="li__input">
            <input {...register("mainItem")} placeholder="Main Item" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
          </div>
        </div>
      ),
    },
    {
      key: "hs-code",
      render: () => (
        <div className="li">
          <span className="li__label">HS Code</span>
          <div className="li__input">
            <input {...register("hsCode")} placeholder="HS Code" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
          </div>
        </div>
      ),
    },
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input
              {...register("cargoQty", { valueAsNumber: true })}
              type="number"
              placeholder="0"
              style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }}
            />
            <select
              {...register("cargoUnit")}
              style={{ width: 44, height: 22, padding: "0 2px", fontSize: 10, border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)", flexShrink: 0 }}
            >
              <option>KG</option>
              <option>LBS</option>
            </select>
          </div>
        </div>
      ),
    },
    {
      key: "unit",
      render: () => (
        <div className="li">
          <span className="li__label">Unit</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input placeholder="Code" style={{ flex: 1, width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          </div>
        </div>
      ),
    },
    {
      key: "gross-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Gross W/T</span>
          <div className="li__input">
            <input
              {...register("grossWt", { valueAsNumber: true })}
              type="number"
              placeholder="0"
              style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}
            />
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
            <input
              {...register("volWt", { valueAsNumber: true })}
              type="number"
              placeholder="0"
              style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}
            />
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
            <input
              {...register("totalCbm", { valueAsNumber: true })}
              type="number"
              placeholder="0"
              style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}
            />
          </div>
        </div>
      ),
    },
    {
      key: "rton",
      render: () => (
        <div className="li">
          <span className="li__label">R/Ton</span>
          <div className="li__input">
            <input
              {...register("rton", { valueAsNumber: true })}
              type="number"
              style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}
            />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="nonbl-cargo-panel" items={CARGO_ITEMS} />
      </div>
    </div>
  );
}
