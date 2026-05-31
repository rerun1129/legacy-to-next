"use client";

import { useFormContext, useWatch } from "react-hook-form";
import { useTranslations } from "next-intl";
import type { MasterBlFormValues } from "../../master-bl-schema";

export function MasterContainerGrid() {
  const { control } = useFormContext<MasterBlFormValues>();
  const rows = useWatch({ control, name: "consoledSeaContainers" }) ?? [];
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("container")}</span>
        <span className="panel__rowcount">{rows.length}</span>
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        <table className="grid--list">
          <colgroup>
            <col style={{ width: "40px" }} />
            <col style={{ width: "120px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "80px" }} />
            <col style={{ width: "80px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "80px" }} />
            <col style={{ width: "100px" }} />
          </colgroup>
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th>{tf("containerNo")}</th>
              <th>{tf("type")}</th>
              <th>{tf("sealNo1")}</th>
              <th>{tf("sealNo2")}</th>
              <th>{tf("sealNo3")}</th>
              <th className="is-num">{tf("pkg")}</th>
              <th>{tf("unit")}</th>
              <th className="is-num">{tf("gw")}</th>
              <th className="is-num">{tf("cbm")}</th>
              <th className="is-num">{tf("vgm")}</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 && (
              <tr>
                <td colSpan={11} style={{ textAlign: "center", padding: 8, fontSize: 11, color: "var(--ink-3)" }}>
                  {tf("noRows")}
                </td>
              </tr>
            )}
            {rows.map((row, idx) => (
              <tr key={`${row.houseBlId}-${idx}`}>
                <td className="row-num">{idx + 1}</td>
                <td>{row.containerNo ?? ""}</td>
                <td>{row.containerType ?? ""}</td>
                <td>{row.sealNo1 ?? ""}</td>
                <td>{row.sealNo2 ?? ""}</td>
                <td>{row.sealNo3 ?? ""}</td>
                <td className="is-num cell-mono">{row.pkgQty ?? ""}</td>
                <td>{row.pkgUnit ?? ""}</td>
                <td className="is-num cell-mono">{row.grossWeightKg ?? ""}</td>
                <td className="is-num cell-mono">{row.cbm ?? ""}</td>
                <td className="is-num cell-mono">{row.vgmKg ?? ""}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
