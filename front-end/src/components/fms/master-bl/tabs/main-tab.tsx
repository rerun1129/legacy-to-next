import { Search } from "lucide-react";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { GridTable, type GridTableColumn } from "@/components/shared/grid-table";

interface MasterScheduleLegRow {
  to: string; flight: string; onBoard: string; arrival: string;
}

const MASTER_SCHED_LEG_COLS: GridTableColumn<MasterScheduleLegRow>[] = [
  { key: "_no",     label: "#",          className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "to",      label: "To *",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "flight",  label: "Flight",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "onBoard", label: "On Board *",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "arrival", label: "Arrival *",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const MASTER_SCHED_LEG_DATA: MasterScheduleLegRow[] = [
  { to: "PVG", flight: "KE851", onBoard: "26APR", arrival: "26APR" },
];

interface Props { variant: MasterVariantConfig }

// House B/L inline grid mock data
const HOUSE_ROWS = [
  { no: 1, hbl: "HBLKR24041956", shipper: "한진무역(주)", consignee: "SHANGHAI TRADING", doc: "HJTR001", pkg: 500, unit: "CTN", gw: "12,400", cbm: 22.5 },
  { no: 2, hbl: "HBLKR24041901", shipper: "삼성전자(주)",  consignee: "SAMSUNG EUROPE",   doc: "SEHQ001", pkg: 800, unit: "CTN", gw: "18,200", cbm: 65.0 },
];

export function MasterMainTab({ variant }: Props) {
  const isSea = variant.mode === "SEA";
  const isExp = variant.direction === "EXP";

  return (
    <div style={{ flex: 1, overflow: "auto", padding: "12px 16px", display: "flex", flexDirection: "column", gap: 10, minHeight: 0 }}>

      {/* House B/L Inline Grid */}
      <div className="panel">
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{isSea ? "House B/L List" : "House AWB List"}</span>
          <span className="panel__rowcount">{HOUSE_ROWS.length}</span>
          <div className="panel__actions">
            <button className="btn btn--sm">+ {isSea ? "New HBL" : "New HAWB"}</button>
            <button className="btn btn--sm">House Consol</button>
            <button className="btn btn--sm">Export</button>
            <button className="btn btn--sm btn--ghost">▲ Collapse</button>
          </div>
        </div>
        <div style={{ overflowX: "auto" }}>
          <table className="grid--list" style={{ width: "max-content", minWidth: "100%" }}>
            <thead>
              <tr>
                <th className="row-num">#</th>
                <th>{isSea ? "House B/L No." : "House AWB No."}</th>
                <th>Shipper</th><th>Consignee</th><th>DOC Partner</th>
                <th className="is-num">Package</th><th>Unit</th>
                <th className="is-num">G/W</th>
                {!isSea && <th className="is-num">Charge Wt.</th>}
                <th className="is-num">CBM</th>
                {isSea && <><th>ETD</th><th>POL</th></>}
              </tr>
            </thead>
            <tbody>
              {HOUSE_ROWS.map((r) => (
                <tr key={r.no}>
                  <td className="row-num">{r.no}</td>
                  <td className="cell-hbl">{r.hbl}</td>
                  <td>{r.shipper}</td><td>{r.consignee}</td><td className="cell-mono">{r.doc}</td>
                  <td className="is-num cell-mono">{r.pkg}</td><td>{r.unit}</td>
                  <td className="is-num cell-mono">{r.gw}</td>
                  {!isSea && <td className="is-num cell-mono">{r.gw}</td>}
                  <td className="is-num cell-mono">{r.cbm}</td>
                  {isSea && <><td className="cell-mono">04/24</td><td className="cell-mono">KRBSAN</td></>}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="grid-foot">
          <div className="grid-foot__spacer" />
          <span>Pkg: <strong>1,300</strong></span>
          <span>G/W: <strong>30,600 kg</strong></span>
          <span>CBM: <strong>87.5</strong></span>
        </div>
      </div>

      {/* Main body: Party + Schedule + Cargo (3-column) */}
      <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) minmax(0,1fr) minmax(0,0.8fr)", gap: 10, minHeight: 0 }}>

        {/* PARTY — 3 slots (no DOC Partner) */}
        <div className="panel" style={{ display: "flex", flexDirection: "column" }}>
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            {([
              { role: "SHIPPER",   btn: "O/B OF" },
              { role: "CONSIGNEE", btn: "To Order" },
              { role: "NOTIFY",    btn: "Same as Cne." },
            ] as const).map((p) => (
              <div key={p.role} className="party-block">
                <div className="party-block__head">
                  <span style={{ fontSize: 11, fontWeight: 600, color: "var(--ink)", minWidth: 80, flexShrink: 0 }}>{p.role}</span>
                  <div style={{ display: "grid", gridTemplateColumns: "100px 1fr", gap: 6, flex: "1 1 auto", alignItems: "center" }}>
                    <div style={{ position: "relative" }}>
                      <input placeholder="Code" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 18px 4px 2px", fontSize: 10, color: "var(--ink)", outline: "none", fontFamily: "var(--font-mono)" }} />
                      <Search size={10} style={{ position: "absolute", right: 2, top: "50%", transform: "translateY(-50%)", color: "var(--ink-4)" }} />
                    </div>
                    <input placeholder="Company Name" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 10, color: "var(--ink)", outline: "none" }} />
                  </div>
                  <div className="party-block__head-actions">
                    <button className="party-block__head-btn">{p.btn}</button>
                    <button className="party-block__head-btn">Clear</button>
                  </div>
                </div>
                <textarea className="textarea" style={{ minHeight: 48, fontSize: 10 }} placeholder="Address" />
              </div>
            ))}
          </div>
        </div>

        {/* SCHEDULE */}
        <div className="panel" style={{ display: "flex", flexDirection: "column" }}>
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span><div className="panel__actions"><button className="btn btn--sm">Reset</button></div></div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            {isSea ? (
              <div className="sched-list">
                <div className="li"><span className="li__label is-required">Liner</span><div className="li__input" style={{ gap: 4 }}><input defaultValue="COSCO" style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} /><input defaultValue="COSCO SHIPPING" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                <div className="sched-pair">
                  {[{ l: "Vessel *", v: "COSCO EXCELLENCE" }, { l: "Voyage *", v: "0412E" }].map((f) => (
                    <div key={f.l} className="li"><span className="li__label is-required">{f.l}</span><div className="li__input"><input defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                  ))}
                </div>
                <div className="sched-pair">
                  {[{ l: "ETD *", v: "2026-04-24" }, { l: "ETA", v: "2026-05-08" }].map((f) => (
                    <div key={f.l} className="li"><span className="li__label is-required">{f.l}</span><div className="li__input"><input type="date" defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                  ))}
                </div>
                {[
                  { l: "POL *", code: "KRBSAN", name: "Busan" },
                  { l: "POD *", code: "CNSHA",  name: "Shanghai" },
                  { l: "Delivery", code: "", name: "" },
                ].map((p) => (
                  <div key={p.l} className="lcn" style={{ marginBottom: 4 }}>
                    <span className="lcn__label">{p.l}</span>
                    <div className="lcn__code" style={{ position: "relative" }}><input defaultValue={p.code} placeholder="UNLOC" style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} /><Search size={10} className="lcn__icon" /></div>
                    <input className="lcn__name" defaultValue={p.name} placeholder="Port" style={{ fontSize: 10 }} />
                  </div>
                ))}
                {isExp && (
                  <>
                    <div className="subhead" style={{ marginTop: 8 }}><div className="subhead__bar" />Issue</div>
                    <div className="li"><span className="li__label">Issue Date</span><div className="li__input"><input type="date" defaultValue="2026-04-20" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                    <div className="li"><span className="li__label">Freight Term</span><div className="li__input"><input defaultValue="Prepaid" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                  </>
                )}
              </div>
            ) : (
              /* AIR schedule */
              <div className="sched-list">
                <div className="li"><span className="li__label is-required">{isExp ? "Airline *" : "Carrier *"}</span><div className="li__input" style={{ gap: 4 }}><input defaultValue="KE" style={{ width: 50, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} /><input defaultValue="Korean Air" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                <div className="li"><span className="li__label is-required">Departure *</span><div className="li__input" style={{ gap: 4 }}><input defaultValue="ICN" style={{ width: 50, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} /><input defaultValue="Incheon Int'l" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                <div style={{ marginTop: 8, fontSize: 10.5, color: "var(--ink-3)" }}>Schedule Leg 그리드 ↓</div>
                <div style={{ overflowX: "auto" }}>
                  <GridTable
                    columns={MASTER_SCHED_LEG_COLS}
                    data={MASTER_SCHED_LEG_DATA}
                    rowKey={(_, i) => i}
                    style={{ fontSize: "var(--fs-xs)" }}
                  />
                </div>
                {isExp && (
                  <>
                    <div className="subhead" style={{ marginTop: 8 }}><div className="subhead__bar" />Issue</div>
                    {["Issue Date", "Signature", "Issue Place"].map((f) => (
                      <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
                    ))}
                  </>
                )}
              </div>
            )}
          </div>
        </div>

        {/* CARGO block (upper right, Master-specific) */}
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {[
                  { l: "Main Item",  v: "ELECTRONIC GOODS" },
                  { l: "HS Code",    v: "8517.13" },
                  { l: "Package",    v: "1300 CTN" },
                  { l: "G/W",        v: "30,600 KGS" },
                  { l: "CBM",        v: "87.5" },
                  ...(isSea ? [{ l: "R/Ton", v: "" }] : [{ l: "Volume W/T", v: "14,583" }, { l: "Charge W/T", v: "30,600" }, { l: "Rate Class", v: "GCR" }]),
                ].map((f) => (
                  <div key={f.l} className="li">
                    <span className="li__label">{f.l}</span>
                    <div className="li__input">
                      <input defaultValue={f.v} style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} />
                    </div>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: 8 }}><button className="btn btn--sm">Apply Say</button></div>
            </div>
          </div>

          {/* Document block */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {(isSea
                  ? [{ l: "Settle Partner", v: "" }, { l: "Co-Load Agent", v: "" }, { l: "Operator", v: "KYS" }, { l: "Team", v: "SEA-EXP" }]
                  : [{ l: "Co-Load Type", v: "" }, { l: "Co-Load Agent", v: "" }, { l: "Flight Type", v: "Passenger" }, ...(isExp ? [{ l: "Security Status", v: "SPX" }] : []), { l: "Settle Partner", v: "" }, { l: "Operator", v: "KYS" }, { l: "Team", v: "AIR-EXP" }]
                ).map((f) => (
                  <div key={f.l} className="li">
                    <span className="li__label">{f.l}</span>
                    <div className="li__input">
                      <input defaultValue={f.v} style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Container / Dimension grid (read-only for Master) */}
      <div className="panel">
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{isSea ? "Container (집계 뷰 — 읽기 전용)" : "Dimension"}</span>
          <span className="panel__rowcount">2</span>
          {!isSea && <div className="panel__actions"><button className="btn btn--sm">Load Dimension</button><button className="btn btn--sm">+ Add</button></div>}
        </div>
        <div style={{ overflowX: "auto" }}>
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num">#</th>
                {isSea
                  ? <><th>Container No.</th><th>Type</th><th>Seal No.</th><th className="is-num">Pkg</th><th>Unit</th><th className="is-num">G/W</th><th className="is-num">CBM</th><th>SOC</th></>
                  : <><th className="is-num">Length</th><th className="is-num">Width</th><th className="is-num">Height</th><th className="is-num">Qty</th><th className="is-num">CBM</th><th className="is-num">Vol. Wt.</th></>
                }
              </tr>
            </thead>
            <tbody>
              {isSea ? (
                <>
                  <tr style={{ color: "var(--ink-3)" }}>
                    <td className="row-num">1</td>
                    <td className="cell-mono">CSNU1234567</td><td>20GP</td><td className="cell-mono">SL123456</td>
                    <td className="is-num cell-mono">500</td><td>CTN</td><td className="is-num cell-mono">12,400</td><td className="is-num cell-mono">22.5</td><td>N</td>
                  </tr>
                  <tr style={{ color: "var(--ink-3)" }}>
                    <td className="row-num">2</td>
                    <td className="cell-mono">TCKU9876543</td><td>40HC</td><td className="cell-mono">SL789012</td>
                    <td className="is-num cell-mono">800</td><td>CTN</td><td className="is-num cell-mono">18,200</td><td className="is-num cell-mono">65.0</td><td>N</td>
                  </tr>
                </>
              ) : (
                <tr>
                  <td className="row-num">1</td>
                  <td className="is-num"><input className="grid__cell-input" defaultValue="120" style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td className="is-num"><input className="grid__cell-input" defaultValue="80"  style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td className="is-num"><input className="grid__cell-input" defaultValue="90"  style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td className="is-num"><input className="grid__cell-input" defaultValue="1300" style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td className="is-num"><input className="grid__cell-input" defaultValue="87.5" style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                  <td className="is-num"><input className="grid__cell-input" defaultValue="14,583" style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <div className="grid-foot"><div className="grid-foot__spacer" /><span>G/W: <strong>30,600 kg</strong></span><span>CBM: <strong>87.5</strong></span></div>
      </div>

      {/* Marks / Description / Remark */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
        {[
          { title: "Marks & Numbers", content: "MADE IN KOREA" },
          { title: isSea ? "Description" : "Nature & Quantity of Goods", content: isSea ? "SAID TO CONTAIN\nELECTRONIC GOODS" : "CONSOLIDATION SHIPMENT\nAS PER ATTACHED MANIFEST" },
          { title: "Remark", content: "" },
        ].map((p) => (
          <div key={p.title} className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{p.title}</span></div>
            <div className="panel__body" style={{ paddingBottom: 8 }}>
              {isSea && p.title === "Description" && (
                <div className="li" style={{ marginBottom: 8 }}>
                  <span className="li__label" style={{ fontSize: 10 }}>Clause 1</span>
                  <div className="li__input"><select style={{ width: "100%", height: 24, fontSize: 10 }}><option>-- 부지약관 --</option><option>SAID TO CONTAIN</option></select></div>
                </div>
              )}
              <textarea className="textarea" style={{ minHeight: 80, width: "100%" }} defaultValue={p.content} placeholder={p.title} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
