import { Search } from "lucide-react";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { PartyPanel } from "./sections/party-panel";
import { SchedulePanel } from "./sections/schedule-panel";

interface Props { variant: BLVariantConfig }

export function MainTabSea({ variant }: Props) {
  const isExp = variant.direction === "EXP";

  return (
    <div className="page-body layout-main" style={{ overflow: "auto" }}>
      {/* PARTY */}
      <PartyPanel isExp={isExp} />

      {/* SCHEDULE */}
      <SchedulePanel variant={variant} />

      {/* TRADE */}
      <div className="zone-trade">
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Trade & Performance</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="sched-list">
              {[
                { l: "Incoterms",    v: "FOB",     req: true  },
                { l: "Freight Term", v: "Prepaid",  req: true  },
                { l: "Payable At",   v: "ORIGIN",   req: false },
                { l: "Co-Load",      v: "N",        req: false },
              ].map((f) => (
                <div key={f.l} className="li">
                  <span className={`li__label${f.req ? " is-required" : ""}`}>{f.l}</span>
                  <div className="li__input">
                    <input defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
              <div style={{ marginTop: 8 }}>
                <div className="subhead"><div className="subhead__bar" />Performance</div>
                {[
                  { l: "Actual Customer", code: "HJTR001", name: "한진무역(주)" },
                  { l: "Sales Man",       code: "LJY",     name: "이진영" },
                  { l: "Operator",        code: "KYS",     name: "김영선" },
                  { l: "Team",            code: "SEA-EXP", name: "해상수출팀" },
                ].map((f) => (
                  <div key={f.l} className="lcn" style={{ marginBottom: 4 }}>
                    <span className="lcn__label is-required">{f.l}</span>
                    <div className="lcn__code" style={{ position: "relative" }}>
                      <input defaultValue={f.code} style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
                      <Search size={12} className="lcn__icon" />
                    </div>
                    <input className="lcn__name" defaultValue={f.name} placeholder="Name" />
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* CONTAINER */}
      <div className="zone-container">
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Container</span>
            <span className="panel__rowcount">2</span>
            <div className="panel__actions"><button className="btn btn--sm">+ Add Row</button></div>
          </div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <table className="grid" style={{ tableLayout: 'fixed' }}>
              <thead>
                <tr>
                  <th className="row-num" style={{ width: 36 }}>#</th>
                  <th style={{ width: 160 }}>Container No *</th><th style={{ width: 70 }}>Type</th><th style={{ width: 110 }}>Seal No</th>
                  <th className="is-num" style={{ width: 70 }}>Pkg</th><th style={{ width: 60 }}>Unit</th>
                  <th className="is-num" style={{ width: 90 }}>G/W</th><th className="is-num" style={{ width: 80 }}>CBM</th><th className="is-num" style={{ width: 90 }}>VGM</th>
                </tr>
              </thead>
              <tbody>
                {[
                  { cno: "CSNU1234567", type: "20GP", seal: "SL123456", pkg: 500, pkgT: "CTN", gw: "12,400", cbm: 22.5, vgm: "12,540" },
                  { cno: "TCKU9876543", type: "40HC", seal: "SL789012", pkg: 800, pkgT: "CTN", gw: "18,200", cbm: 65.0, vgm: "18,380" },
                ].map((r, i) => (
                  <tr key={i}>
                    <td className="row-num" style={{ width: 36 }}>{i + 1}</td>
                    <td style={{ width: 160 }}><input className="grid__cell-input" defaultValue={r.cno} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /></td>
                    <td style={{ width: 70 }}><input className="grid__cell-input" defaultValue={r.type} /></td>
                    <td style={{ width: 110 }}><input className="grid__cell-input" defaultValue={r.seal} style={{ fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num" style={{ width: 70 }}><input className="grid__cell-input" defaultValue={r.pkg} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td style={{ width: 60 }}><input className="grid__cell-input" defaultValue={r.pkgT} /></td>
                    <td className="is-num" style={{ width: 90 }}><input className="grid__cell-input" defaultValue={r.gw} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num" style={{ width: 80 }}><input className="grid__cell-input" defaultValue={r.cbm} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num" style={{ width: 90 }}><input className="grid__cell-input" defaultValue={r.vgm} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="grid-foot">
            <span>2 containers</span>
            <div className="grid-foot__spacer" />
            <span>G/W: <strong className="grid-foot__total">30,600 kg</strong></span>
            <span>CBM: <strong className="grid-foot__total">87.5</strong></span>
          </div>
        </div>
      </div>

      {/* MARKS */}
      <div className="zone-marks">
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Marks & Numbers</span></div>
          <div className="panel__body" style={{ flex: 1 }}>
            <textarea className="textarea textarea--tall" defaultValue={"MADE IN KOREA\nCTN NO. 1-500\nGROSS WT: 12,400 KGS"} />
          </div>
        </div>
      </div>

      {/* DESCRIPTION */}
      <div className="zone-description">
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Description</span></div>
          <div className="panel__body" style={{ flex: 1 }}>
            <div className="li" style={{ marginBottom: 8 }}>
              <span className="li__label">Clause</span>
              <div className="li__input">
                <select style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}>
                  <option value="">-- 부지약관 --</option>
                  <option>SAID TO CONTAIN</option>
                  <option>SHIPPER&apos;S LOAD AND COUNT</option>
                </select>
              </div>
            </div>
            <textarea className="textarea textarea--tall" defaultValue={"ELECTRONIC GOODS\n(MOBILE PHONE PARTS)\n1,300 CARTONS\nSAID TO CONTAIN"} />
          </div>
        </div>
      </div>

      {/* ITEM */}
      <div className="zone-item">
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Item / HS Code</span><span className="panel__rowcount">1</span><div className="panel__actions"><button className="btn btn--sm">+ Add</button></div></div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <table className="grid" style={{ tableLayout: 'fixed' }}>
              <thead>
                <tr><th className="row-num" style={{ width: 36 }}>#</th><th style={{ width: 100 }}>HS Code</th><th style={{ width: 200 }}>Description</th><th className="is-num" style={{ width: 70 }}>Qty</th><th style={{ width: 60 }}>Unit</th><th className="is-num" style={{ width: 100 }}>Value</th><th style={{ width: 80 }}>Currency</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td className="row-num" style={{ width: 36 }}>1</td>
                  <td style={{ width: 100 }}><input className="grid__cell-input" defaultValue="8517.13" style={{ fontFamily: "var(--font-mono)" }} /></td>
                  <td style={{ width: 200 }}><input className="grid__cell-input" defaultValue="MOBILE PHONE PARTS" /></td>
                  <td className="is-num" style={{ width: 70 }}><input className="grid__cell-input" defaultValue="1300" style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td style={{ width: 60 }}><input className="grid__cell-input" defaultValue="CTN" /></td>
                  <td className="is-num" style={{ width: 100 }}><input className="grid__cell-input" defaultValue="48,500.00" style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td style={{ width: 80 }}><input className="grid__cell-input" defaultValue="USD" style={{ fontFamily: "var(--font-mono)" }} /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
