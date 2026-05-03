"use client";

import { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface CoLoadRow {
  id: number;
  hblNo: string; shipper: string; consignee: string;
  pkg: string; gw: string; cbm: string;
}

const CO_LOAD_COLS: GridColumn<CoLoadRow>[] = [
  { key: "_no",       label: "#",         className: "row-num", render: (_, __, i) => i + 1 },
  { key: "hblNo",     label: "HBL No" },
  { key: "shipper",   label: "Shipper" },
  { key: "consignee", label: "Consignee" },
  { key: "pkg",       label: "Pkg",       className: "is-num" },
  { key: "gw",        label: "G/W",       className: "is-num" },
  { key: "cbm",       label: "CBM",       className: "is-num" },
];

const EMPTY_CO_LOAD_ROW = {
  hblNo: "", shipper: "", consignee: "", pkg: "", gw: "", cbm: "",
};

export function OtherTab() {
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "coLoadBls" });
  const [selectedKey, setSelectedKey] = useState<number | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    const nextId = fields.length > 0 ? Math.max(...fields.map(f => f.id)) + 1 : 1;
    append({ ...EMPTY_CO_LOAD_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    remove(targetIdx);
    setSelectedKey(null);
  }

  return (
    <div style={{ flex: 1, overflow: "hidden", padding: "12px 16px" }}>
      <div style={{ display: "flex", gap: 10, height: "100%" }}>
        <div style={{ flex: 1, minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Reference Numbers</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="sched-list">
              {[
                { label: "PO No",         value: "" },
                { label: "Invoice No",    value: "" },
                { label: "Contract No",   value: "" },
                { label: "L/C No",        value: "" },
                { label: "Customer Ref",  value: "" },
                { label: "Booking Ref",   value: "" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
            </div>

            <div className="subhead" style={{ marginTop: 12 }}><div className="subhead__bar" />Additional Info</div>
            <div className="sched-list">
              {[
                { label: "Inco Place",    value: "" },
                { label: "Payment Term",  value: "" },
                { label: "Country Origin",value: "" },
                { label: "Country Dest",  value: "" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

        <div style={{ flex: 1, minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <div className="panel">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Co-Load B/L</span>
            <span className="panel__rowcount">{fields.length}</span>
            <div className="panel__actions">
              <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
              <button type="button" className="btn btn--sm" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
            </div>
          </div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <GridList
              columns={CO_LOAD_COLS}
              data={fields as unknown as CoLoadRow[]}
              rowKey={(row) => row.id}
              onRowClick={(row) => setSelectedKey(row.id)}
              rowClassName={(row) => row.id === selectedKey ? "is-selected" : undefined}
            />
          </div>
        </div>
        </div>{/* /Co-Load B/L */}
      </div>{/* /row */}
    </div>
  );
}
