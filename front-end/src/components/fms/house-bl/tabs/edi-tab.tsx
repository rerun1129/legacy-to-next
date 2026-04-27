import type { BLVariantConfig } from "@/lib/bl-variants";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface LicenseRow {
  id: number;
  exportNo: string; qty: string; unit: string; weight: string;
  symCode: string; symQty: string; symUnit: string;
  splitYn: string; seq: string; progress: string;
}

const LICENSE_ROWS: LicenseRow[] = [
  { id: 1, exportNo: "22231-26-123456X", qty: "300", unit: "CTN", weight: "3,720", symCode: "",   symQty: "",   symUnit: "",    splitYn: "N", seq: "1", progress: "수리"     },
  { id: 2, exportNo: "22231-26-123457X", qty: "250", unit: "CTN", weight: "3,100", symCode: "",   symQty: "",   symUnit: "",    splitYn: "N", seq: "1", progress: "수리"     },
  { id: 3, exportNo: "22231-26-123458X", qty: "200", unit: "CTN", weight: "2,480", symCode: "A1", symQty: "50", symUnit: "CTN", splitYn: "Y", seq: "1", progress: "검사지정" },
  { id: 4, exportNo: "22231-26-123459X", qty: "300", unit: "CTN", weight: "3,720", symCode: "",   symQty: "",   symUnit: "",    splitYn: "N", seq: "2", progress: "수리"     },
  { id: 5, exportNo: "22231-26-123460X", qty: "250", unit: "CTN", weight: "3,100", symCode: "",   symQty: "",   symUnit: "",    splitYn: "N", seq: "1", progress: "신고수리"  },
  { id: 6, exportNo: "22231-26-123461X", qty: "200", unit: "CTN", weight: "2,480", symCode: "",   symQty: "",   symUnit: "",    splitYn: "N", seq: "1", progress: "수리"      },
];

const LICENSE_COLS: GridColumn<LicenseRow>[] = [
  { key: "_no",       label: "#",                    className: "row-num",   render: (_, __, i) => i + 1 },
  { key: "exportNo",  label: "수출신고번호 / 화물관리번호" },
  { key: "qty",       label: "수량",                  className: "is-num" },
  { key: "unit",      label: "단위" },
  { key: "weight",    label: "중량",                  className: "is-num" },
  { key: "symCode",   label: "동시포장: 기호" },
  { key: "symQty",    label: "수량",                  className: "is-num" },
  { key: "symUnit",   label: "단위" },
  { key: "splitYn",   label: "분할여부" },
  { key: "seq",       label: "차수" },
  { key: "progress",  label: "진행내역" },
];

interface Props { variant?: BLVariantConfig }

export function EdiTab({ variant }: Props) {
  const isExp = variant ? variant.direction === "EXP" : true;

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
                { label: "EDI B/L No",      value: variant?.mode === "AIR" ? "HAWBKR24041001" : "HBLKR24041956", req: true  },
                { label: "EDI Item",         value: "",         req: false },
                { label: "Cargo Class",      value: "GEN",      req: false },
                { label: "T/S",              value: "N",        req: false },
                { label: "Customs Entry No", value: "",         req: false },
                { label: "MRN No",           value: "",         req: false },
                { label: "Co-load HBL No",   value: "",         req: false },
                { label: "Filing Type",      value: "AMS",      req: false },
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
                <div className="subhead"><div className="subhead__bar" />License (수출신고필증) — Korea Only<div className="panel__actions" style={{ marginLeft: "auto" }}><button className="btn btn--sm">+ 추가</button></div></div>
                <div style={{ overflow: "auto" }}>
                  <GridList
                    columns={LICENSE_COLS}
                    data={LICENSE_ROWS}
                    rowKey={(row) => row.id}
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
