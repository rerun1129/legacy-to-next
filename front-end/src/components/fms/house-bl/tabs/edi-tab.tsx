import type { BLVariantConfig } from "@/lib/bl-variants";

interface Props { variant?: BLVariantConfig }

export function EdiTab({ variant }: Props) {
  const isExp = variant ? variant.direction === "EXP" : true;

  return (
    <div className="page-body layout-edi" style={{ overflow: "auto" }}>
      {/* Top: EDI identifiers */}
      <div className="zone-top">
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
      <div className="zone-left">
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
                <div style={{ overflowX: "auto" }}>
                  <table className="grid">
                    <thead>
                      <tr>
                        <th className="row-num">#</th>
                        <th>수출신고번호 / 화물관리번호</th>
                        <th className="is-num">수량</th><th>단위</th><th className="is-num">중량</th>
                        <th>동시포장: 기호</th><th className="is-num">수량</th><th>단위</th>
                        <th>분할여부</th><th>차수</th><th>진행내역</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr><td colSpan={11} className="grid__empty">면장 없음 — 추가 버튼으로 입력</td></tr>
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Right: Filing Rules */}
      <div className="zone-right">
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
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
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
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
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
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
