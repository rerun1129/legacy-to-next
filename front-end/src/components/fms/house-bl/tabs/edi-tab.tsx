"use client";

import { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { BLVariantConfig } from "@/lib/bl-variants";
import type { HouseBlFormValues } from "../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

// Korea Only 라이선스 (licenses 배열과 별개로 koreaLicenses로 관리)
interface KoreaLicenseRow {
  id: number;
  licenseNo: string; amount: string; cur: string;
}

const KOREA_LICENSE_COLS: GridColumn<KoreaLicenseRow>[] = [
  { key: "_no",       label: "#",                    className: "row-num",   render: (_, __, i) => i + 1 },
  { key: "licenseNo", label: "수출신고번호 / 화물관리번호" },
  { key: "amount",    label: "금액",                  className: "is-num" },
  { key: "cur",       label: "통화" },
];

const EMPTY_KOREA_LICENSE_ROW = { licenseNo: "", amount: "", cur: "" };

interface Props { variant?: BLVariantConfig }

export function EdiTab({ variant }: Props) {
  const isExp = variant ? variant.direction === "EXP" : true;
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "koreaLicenses" });
  const [selectedKey, setSelectedKey] = useState<number | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleLicenseAdd() {
    const nextId = fields.length > 0 ? Math.max(...fields.map(f => f.id)) + 1 : 1;
    append({ ...EMPTY_KOREA_LICENSE_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleLicenseRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    if (window.confirm("삭제하시겠습니까?")) {
      remove(targetIdx);
      setSelectedKey(null);
    }
  }

  return (
    <div style={{ flex: 1, overflow: "auto", padding: "12px 16px", display: "flex", flexDirection: "column", gap: 10, minHeight: 0 }}>
      {/* Row 1: EDI Identifiers (전체 너비) */}
      <div style={{ flexShrink: 0 }}>
        <div className="panel">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">EDI Identifiers</span>
          </div>
          <div className="panel__body">
            <div className="form-grid form-grid--4">
              {[
                { label: "EDI B/L No",      value: "",  req: true  },
                { label: "EDI Item",         value: "",  req: false },
                { label: "Cargo Class",      value: "",  req: false },
                { label: "T/S",              value: "",  req: false },
                { label: "Customs Entry No", value: "",  req: false },
                { label: "MRN No",           value: "",  req: false },
                { label: "Co-load HBL No",   value: "",  req: false },
                { label: "Filing Type",      value: "",  req: false },
              ].map((f) => (
                <div key={f.label} className={`field${f.req ? " is-required" : ""}`}>
                  <div className={`field__label${f.req ? " is-required" : ""}`}>{f.label}</div>
                  <div className="field__input">
                    <input defaultValue={f.value} placeholder={f.label} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Left: Dangerous Goods (IMDG/UN) */}
      {/* Row 2: Dangerous Goods | Filing Rules */}
      <div style={{ display: "flex", gap: 10, flex: "1 1 0", minHeight: 0 }}>
        <div style={{ flex: 1, minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Dangerous Goods</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="subhead"><div className="subhead__bar" />IMDG Code × 5</div>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 8, marginBottom: 12 }}>
              {[1, 2, 3, 4, 5].map((n) => (
                <div key={n} className="field">
                  <div className="field__label">IMDG {n}</div>
                  <div className="field__input"><input placeholder="Code" /></div>
                </div>
              ))}
            </div>
            <div className="subhead"><div className="subhead__bar" />UN No × 5</div>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 8 }}>
              {[1, 2, 3, 4, 5].map((n) => (
                <div key={n} className="field">
                  <div className="field__label">UN No {n}</div>
                  <div className="field__input"><input placeholder="UN No" style={{ fontFamily: "var(--font-mono)" }} /></div>
                </div>
              ))}
            </div>

            {/* License section — EXP variants only */}
            {isExp && (
              <div style={{ marginTop: 16 }}>
                <div className="subhead">
                  <div className="subhead__bar" />License (수출신고필증) — Korea Only
                  <div className="panel__actions" style={{ marginLeft: "auto" }}>
                    <button type="button" className="btn btn--sm" onClick={handleLicenseAdd}><Plus size={12} /></button>
                    <button type="button" className="btn btn--sm" onClick={handleLicenseRemove} disabled={fields.length === 0}><Minus size={12} /></button>
                  </div>
                </div>
                <div style={{ overflow: "auto" }}>
                  <GridList
                    columns={KOREA_LICENSE_COLS}
                    data={fields as unknown as KoreaLicenseRow[]}
                    rowKey={(row) => row.id}
                    onRowClick={(row) => setSelectedKey(row.id === selectedKey ? null : row.id)}
                    rowClassName={(row) => row.id === selectedKey ? "is-selected" : undefined}
                  />
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Right: Filing Rules */}
        <div style={{ flex: 1, minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Filing Rules (AMS / AFR)</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="subhead"><div className="subhead__bar" />AMS (US)</div>
            <div className="sched-list">
              {[
                { label: "Bond Type",       value: "Single" },
                { label: "Bond Holder",     value: "" },
                { label: "AMS Filing Date", value: "" },
                { label: "AMS Status",      value: "N/A" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
            </div>
            <div className="subhead" style={{ marginTop: 16 }}><div className="subhead__bar" />AFR (Japan)</div>
            <div className="sched-list">
              {[
                { label: "AFR Filing Date", value: "" },
                { label: "AFR Status",      value: "N/A" },
                { label: "MIC Code",        value: "" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
            </div>
            <div className="subhead" style={{ marginTop: 16 }}><div className="subhead__bar" />China ACDD</div>
            <div className="sched-list">
              {[
                { label: "ACDD Status", value: "N/A" },
                { label: "Filing Code", value: "" },
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
        </div>{/* /Filing Rules */}
      </div>{/* /Row 2 */}
    </div>
  );
}
