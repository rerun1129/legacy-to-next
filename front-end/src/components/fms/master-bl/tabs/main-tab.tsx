import { Search } from "lucide-react";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { Mode, Direction } from "@/lib/bl-variants";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

// ── Schedule leg ──────────────────────────────────────────────────
interface MasterScheduleLegRow {
  to: string; flight: string; onBoard: string; arrival: string;
}

const MASTER_SCHED_LEG_COLS: GridColumn<MasterScheduleLegRow>[] = [
  { key: "_no",     label: "#",        className: "row-num", render: (_, __, i) => i + 1 },
  { key: "to",      label: "To",     render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "flight",  label: "Flight",   render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "onBoard", label: "On Board", render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "arrival", label: "Arrival",  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const MASTER_SCHED_LEG_DATA: MasterScheduleLegRow[] = [
  { to: "PVG", flight: "KE851", onBoard: "26APR", arrival: "26APR" },
  { to: "NRT", flight: "KE701", onBoard: "27APR", arrival: "27APR" },
  { to: "HKG", flight: "CX418", onBoard: "28APR", arrival: "28APR" },
  { to: "SIN", flight: "SQ607", onBoard: "29APR", arrival: "29APR" },
  { to: "LAX", flight: "OZ202", onBoard: "30APR", arrival: "01MAY" },
  { to: "CDG", flight: "AF267", onBoard: "02MAY", arrival: "02MAY" },
];

// ── Cargo / document field constants ──────────────────────────────
const CARGO_EXTRAS_SEA = [{ l: "R/Ton", v: "" }];
const CARGO_EXTRAS_AIR = [
  { l: "Volume W/T", v: "14,583" },
  { l: "Charge W/T", v: "30,600" },
  { l: "Rate Class",  v: "GCR"    },
];

const DOC_FIELDS_SEA = [
  { l: "Settle Partner", v: "" },
  { l: "Co-Load Agent",  v: "" },
  { l: "Operator",       v: "KYS"     },
  { l: "Team",           v: "SEA-EXP" },
];
const DOC_FIELDS_AIR_BASE = [
  { l: "Co-Load Type",  v: "" },
  { l: "Co-Load Agent", v: "" },
  { l: "Flight Type",   v: "Passenger" },
];
const DOC_FIELDS_AIR_EXPORT_ONLY = [{ l: "Security Status", v: "SPX" }];
const DOC_FIELDS_AIR_TAIL = [
  { l: "Settle Partner", v: "" },
  { l: "Operator",       v: "KYS"     },
  { l: "Team",           v: "AIR-EXP" },
];

// ── House B/L inline grid rows ────────────────────────────────────
const HOUSE_ROWS = [
  { no: 1, hbl: "HBLKR24041956", shipper: "한진무역(주)",     consignee: "SHANGHAI TRADING CO.", doc: "HJTR001", pkg: 500, unit: "CTN", gw: "12,400", cbm: 22.5 },
  { no: 2, hbl: "HBLKR24041901", shipper: "삼성전자(주)",     consignee: "SAMSUNG EUROPE GmbH",  doc: "SEHQ001", pkg: 800, unit: "CTN", gw: "18,200", cbm: 65.0 },
  { no: 3, hbl: "HBLKR24041877", shipper: "현대상사(주)",     consignee: "HYUNDAI TRADING USA",  doc: "HTS001",  pkg: 300, unit: "CTN", gw: "7,500",  cbm: 30.0 },
  { no: 4, hbl: "HBLKR24041823", shipper: "엘지전자(주)",     consignee: "LG ELECTRONICS INC.", doc: "LGEL001", pkg: 420, unit: "CTN", gw: "9,800",  cbm: 40.5 },
  { no: 5, hbl: "HBLKR24041800", shipper: "코오롱인더스트리", consignee: "KOLON GLOBAL CORP.",   doc: "KGC001",  pkg: 250, unit: "CTN", gw: "5,200",  cbm: 18.0 },
  { no: 6, hbl: "HBLKR24041756", shipper: "SK하이닉스(주)",  consignee: "SK HYNIX INC.",        doc: "SKH001",  pkg: 180, unit: "CTN", gw: "4,500",  cbm: 15.0 },
];

// ── Data selector functions ───────────────────────────────────────
function getCargoExtras(mode: Mode) {
  return mode === "SEA" ? CARGO_EXTRAS_SEA : CARGO_EXTRAS_AIR;
}

function getDocFields(mode: Mode, direction: Direction) {
  if (mode === "SEA") return DOC_FIELDS_SEA;
  const exportOnlyFields = direction === "EXP" ? DOC_FIELDS_AIR_EXPORT_ONLY : [];
  return [...DOC_FIELDS_AIR_BASE, ...exportOnlyFields, ...DOC_FIELDS_AIR_TAIL];
}

function getGoodsDescDefaultContent(mode: Mode): string {
  return mode === "SEA"
    ? "SAID TO CONTAIN\nELECTRONIC GOODS"
    : "CONSOLIDATION SHIPMENT\nAS PER ATTACHED MANIFEST";
}

// ── Schedule section renderers ────────────────────────────────────
function renderSeaSchedule(variant: MasterVariantConfig) {
  const isExp = variant.direction === "EXP";
  return (
    <div className="sched-list">
      <div className="li">
        <span className="li__label is-required">Liner</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue="COSCO" style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue="COSCO SHIPPING" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
      <div className="sched-pair">
        {[{ l: "Vessel", v: "COSCO EXCELLENCE" }, { l: "Voyage", v: "0412E" }].map((f) => (
          <div key={f.l} className="li"><span className="li__label is-required">{f.l}</span><div className="li__input"><input defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
        ))}
      </div>
      <div className="sched-pair">
        {[{ l: "ETD", v: "2026-04-24" }, { l: "ETA", v: "2026-05-08" }].map((f) => (
          <div key={f.l} className="li"><span className="li__label is-required">{f.l}</span><div className="li__input"><input type="date" defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
        ))}
      </div>
      {[
        { l: "POL", code: "KRBSAN", name: "Busan" },
        { l: "POD", code: "CNSHA",  name: "Shanghai" },
        { l: "Delivery", code: "", name: "" },
      ].map((p) => (
        <div key={p.l} className="lcn" style={{ marginBottom: 4 }}>
          <span className="lcn__label">{p.l}</span>
          <div className="lcn__code" style={{ position: "relative" }}>
            <input defaultValue={p.code} placeholder="UNLOC" style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
            <Search size={10} className="lcn__icon" />
          </div>
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
  );
}

function renderAirSchedule(variant: MasterVariantConfig) {
  const isExp = variant.direction === "EXP";
  const airlineOrCarrierLabel = isExp ? "Airline" : "Carrier";
  return (
    <div className="sched-list">
      <div className="li">
        <span className="li__label is-required">{airlineOrCarrierLabel}</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue="KE" style={{ width: 50, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue="Korean Air" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
      <div className="li"><span className="li__label is-required">Departure</span><div className="li__input" style={{ gap: 4 }}><input defaultValue="ICN" style={{ width: 50, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} /><input defaultValue="Incheon Int'l" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} /></div></div>
      <div style={{ marginTop: 8, fontSize: 10.5, color: "var(--ink-3)" }}>Schedule Leg 그리드 ↓</div>
      <div style={{ overflow: "auto" }}>
        <GridList columns={MASTER_SCHED_LEG_COLS} data={MASTER_SCHED_LEG_DATA} rowKey={(_, i) => i} />
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
  );
}

function renderScheduleSection(variant: MasterVariantConfig) {
  return variant.mode === "SEA"
    ? renderSeaSchedule(variant)
    : renderAirSchedule(variant);
}

// ── Container / Dimension section renderers ───────────────────────
function renderContainerOrDimensionHeaders(mode: Mode) {
  if (mode === "SEA") {
    return <><th>Container No.</th><th>Type</th><th>Seal No.</th><th className="is-num">Pkg</th><th>Unit</th><th className="is-num">G/W</th><th className="is-num">CBM</th><th>SOC</th></>;
  }
  return <><th className="is-num">Length</th><th className="is-num">Width</th><th className="is-num">Height</th><th className="is-num">Qty</th><th className="is-num">CBM</th><th className="is-num">Vol. Wt.</th></>;
}

function renderContainerOrDimensionRows(mode: Mode) {
  if (mode === "SEA") {
    return (
      <>
        {[
          { cno: "CSNU1234567", type: "20GP", seal: "SL123456", pkg: 500, unit: "CTN", gw: "12,400", cbm: "22.5" },
          { cno: "TCKU9876543", type: "40HC", seal: "SL789012", pkg: 800, unit: "CTN", gw: "18,200", cbm: "65.0" },
          { cno: "MSKU3456789", type: "40GP", seal: "SL345678", pkg: 650, unit: "CTN", gw: "15,800", cbm: "60.2" },
          { cno: "HLXU2345678", type: "20GP", seal: "SL456789", pkg: 420, unit: "CTN", gw: "10,500", cbm: "21.0" },
          { cno: "GESU5678901", type: "40HC", seal: "SL567890", pkg: 750, unit: "CTN", gw: "19,400", cbm: "67.5" },
          { cno: "TCNU8901234", type: "20GP", seal: "SL678901", pkg: 350, unit: "CTN", gw: "8,750",  cbm: "19.8" },
        ].map((r, i) => (
          <tr key={r.cno} style={{ color: "var(--ink-3)" }}>
            <td className="row-num">{i + 1}</td>
            <td className="cell-mono">{r.cno}</td><td>{r.type}</td><td className="cell-mono">{r.seal}</td>
            <td className="is-num cell-mono">{r.pkg}</td><td>{r.unit}</td>
            <td className="is-num cell-mono">{r.gw}</td><td className="is-num cell-mono">{r.cbm}</td><td>N</td>
          </tr>
        ))}
      </>
    );
  }
  return (
    <>
      {[
        { length: "120", width: "80",  height: "90",  qty: "1300", cbm: "87.5",  volWt: "14,583" },
        { length: "100", width: "70",  height: "80",  qty: "200",  cbm: "15.0",  volWt: "2,500"  },
        { length: "150", width: "100", height: "120", qty: "50",   cbm: "22.5",  volWt: "3,750"  },
        { length: "60",  width: "50",  height: "40",  qty: "500",  cbm: "6.0",   volWt: "1,000"  },
        { length: "200", width: "120", height: "150", qty: "20",   cbm: "72.0",  volWt: "12,000" },
        { length: "80",  width: "60",  height: "50",  qty: "300",  cbm: "7.2",   volWt: "1,200"  },
      ].map((r, i) => (
        <tr key={i}>
          <td className="row-num">{i + 1}</td>
          {(["length", "width", "height", "qty", "cbm", "volWt"] as const).map((k) => (
            <td key={k} className="is-num">
              <input className="grid__cell-input" defaultValue={r[k]} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} />
            </td>
          ))}
        </tr>
      ))}
    </>
  );
}

// ── Component ─────────────────────────────────────────────────────
interface Props { variant: MasterVariantConfig }

export function MasterMainTab({ variant }: Props) {
  const isSea = variant.mode === "SEA";
  const modeLabels = getModeLabels(variant.mode);
  const cargoExtras = getCargoExtras(variant.mode);
  const docFields = getDocFields(variant.mode, variant.direction);
  const goodsDescContent = getGoodsDescDefaultContent(variant.mode);

  return (
    <div style={{ flex: 1, overflow: "auto", padding: "12px 16px", display: "flex", flexDirection: "column", gap: 10, minHeight: 0 }}>

      {/* House B/L Inline Grid */}
      <div className="panel">
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{modeLabels.hblList}</span>
          <span className="panel__rowcount">{HOUSE_ROWS.length}</span>
          <div className="panel__actions">
            <button className="btn btn--sm">+ {modeLabels.newHbl}</button>
            <button className="btn btn--sm">House Consol</button>
            <button className="btn btn--sm">Export</button>
            <button className="btn btn--sm btn--ghost">▲ Collapse</button>
          </div>
        </div>
        <div style={{ overflow: "auto" }}>
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num">#</th>
                <th>{modeLabels.hblNo}</th>
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
      </div>

      {/* Main body: Party + Schedule + Cargo (3-column) */}
      <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) minmax(0,1fr) minmax(0,0.8fr)", gap: 10 }}>

        {/* PARTY */}
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
            {renderScheduleSection(variant)}
          </div>
        </div>

        {/* CARGO + DOCUMENT */}
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {[
                  { l: "Main Item", v: "ELECTRONIC GOODS" },
                  { l: "HS Code",   v: "8517.13" },
                  { l: "Package",   v: "1300 CTN" },
                  { l: "G/W",       v: "30,600 KGS" },
                  { l: "CBM",       v: "87.5" },
                  ...cargoExtras,
                ].map((f) => (
                  <div key={f.l} className="li">
                    <span className="li__label">{f.l}</span>
                    <div className="li__input"><input defaultValue={f.v} style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} /></div>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: 8 }}><button className="btn btn--sm">Apply Say</button></div>
            </div>
          </div>
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {docFields.map((f) => (
                  <div key={f.l} className="li">
                    <span className="li__label">{f.l}</span>
                    <div className="li__input"><input defaultValue={f.v} style={{ width: "100%", height: 24, padding: "0 6px", fontSize: 10 }} /></div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Container / Dimension grid */}
      <div className="panel">
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{modeLabels.containerPanel}</span>
          <span className="panel__rowcount">6</span>
          {!isSea && <div className="panel__actions"><button className="btn btn--sm">Load Dimension</button><button className="btn btn--sm">+</button></div>}
        </div>
        <div style={{ overflow: "auto" }}>
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num">#</th>
                {renderContainerOrDimensionHeaders(variant.mode)}
              </tr>
            </thead>
            <tbody>
              {renderContainerOrDimensionRows(variant.mode)}
            </tbody>
          </table>
        </div>
      </div>

      {/* Marks / Description / Remark */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
        {[
          { title: "Marks & Numbers",  content: "MADE IN KOREA" },
          { title: modeLabels.goodsDesc, content: goodsDescContent },
          { title: "Remark",           content: "" },
        ].map((p) => (
          <div key={p.title} className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{p.title}</span></div>
            <div className="panel__body" style={{ paddingBottom: 8 }}>
              {isSea && p.title === modeLabels.goodsDesc && (
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
