import { Search } from "lucide-react";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface ScheduleLegRow {
  to: string; by: string; flight: string;
  onBoard: string; boardTime: string; arrival: string; arrTime: string;
}

interface DimensionRow {
  length: string; width: string; height: string;
  qty: string; cbm: string; volWt: string;
}

interface ItemRow {
  hs: string; desc: string; qty: string; unit: string; value: string; cur: string;
}

const SCHEDULE_LEG_COLS: GridColumn<ScheduleLegRow>[] = [
  { key: "_no",      label: "#",           className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "to",       label: "To *",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "by",       label: "By",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "flight",   label: "Flight",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "onBoard",  label: "On Board *",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "boardTime",label: "Time",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "arrival",  label: "Arrival *",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "arrTime",  label: "Time",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const DIMENSION_COLS: GridColumn<DimensionRow>[] = [
  { key: "_no",   label: "#",          className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "length",label: "Length",     className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "width", label: "Width",      className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "height",label: "Height",     className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "qty",   label: "Qty",        className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "cbm",   label: "CBM",        className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "volWt", label: "Volume Wt.", className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
];

const AIR_ITEM_COLS: GridColumn<ItemRow>[] = [
  { key: "_no",   label: "#",          className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "hs",    label: "HS Code",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "desc",  label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",   label: "Qty",        className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "unit",  label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "value", label: "Value",      className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "cur",   label: "Cur.",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const SCHEDULE_LEG_DATA: ScheduleLegRow[] = [
  { to: "PVG", by: "KE", flight: "KE851", onBoard: "26APR", boardTime: "09:30", arrival: "26APR", arrTime: "11:45" },
];

const DIMENSION_DATA: DimensionRow[] = [
  { length: "120", width: "80", height: "90", qty: "1300", cbm: "87.5", volWt: "14,583" },
];

const AIR_ITEM_DATA: ItemRow[] = [
  { hs: "8517.13", desc: "MOBILE PHONE PARTS", qty: "1300", unit: "CTN", value: "48,500.00", cur: "USD" },
];

interface Props { variant: BLVariantConfig }

export function MainTabAir({ variant }: Props) {
  const isExp = variant.direction === "EXP";
  const isImp = variant.direction === "IMP";

  return (
    <div className="page-body" style={{ overflow: "auto", display: "grid", gridTemplateColumns: "minmax(0,0.82fr) minmax(0,0.58fr) minmax(0,0.58fr)", gridTemplateRows: "minmax(0,5fr) auto minmax(0,4fr) minmax(0,3fr)", gap: 10, gridTemplateAreas: `"party schedule trade" "party cargo cargo" "dim dim dim" "marks desc item"` }}>

      {/* PARTY */}
      <div style={{ gridArea: "party", minHeight: 0, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            {([
              { role: "SHIPPER",     req: false },
              { role: "CONSIGNEE",  req: isImp  },
              { role: "NOTIFY",     req: false },
              { role: "DOC PARTNER", req: false },
            ] as const).map((p) => (
              <div key={p.role} className="party-block">
                <div className="party-block__head">
                  <span style={{ fontSize: 12, color: "var(--ink)", minWidth: 90, flexShrink: 0 }}>
                    {p.role}{p.req && <span style={{ color: "var(--required)", marginLeft: 3 }}>*</span>}
                  </span>
                  <div style={{ display: "grid", gridTemplateColumns: "110px 1fr", gap: 6, flex: "1 1 auto", alignItems: "center" }}>
                    <div style={{ position: "relative" }}>
                      <input placeholder="Code" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 20px 4px 2px", fontSize: 11, color: "var(--ink)", outline: "none", fontFamily: "var(--font-mono)" }} />
                      <Search size={10} style={{ position: "absolute", right: 4, top: "50%", transform: "translateY(-50%)", color: "var(--ink-4)" }} />
                    </div>
                    <input placeholder="Company Name" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 11, color: "var(--ink)", outline: "none" }} />
                  </div>
                  <div className="party-block__head-actions">
                    {p.role === "CONSIGNEE" && <button className="party-block__head-btn">To Order</button>}
                    {p.role === "NOTIFY"    && <button className="party-block__head-btn">Same as Cne.</button>}
                    <button className="party-block__head-btn">Clear</button>
                  </div>
                </div>
                <textarea className="textarea" placeholder="Address" style={{ minHeight: 60 }} />
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* SCHEDULE (AIR leg grid) */}
      <div style={{ gridArea: "schedule", minHeight: 0, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Schedule</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="sched-list">
              <div className="li">
                <span className="li__label is-required">{isExp ? "Airline *" : "Carrier *"}</span>
                <div className="li__input" style={{ gap: 4 }}>
                  <input placeholder="Code" defaultValue={isExp ? "KE" : "OZ"} style={{ width: 60, height: 26, padding: "0 6px", fontSize: 12, fontFamily: "var(--font-mono)" }} />
                  <input defaultValue={isExp ? "Korean Air" : "Asiana Airlines"} style={{ flex: 1, height: 26, padding: "0 8px", fontSize: 12 }} />
                </div>
              </div>
              <div className="li">
                <span className="li__label is-required">Departure *</span>
                <div className="li__input" style={{ gap: 4 }}>
                  <input placeholder="IATA" defaultValue="ICN" style={{ width: 60, height: 26, padding: "0 6px", fontSize: 12, fontFamily: "var(--font-mono)" }} />
                  <input defaultValue="Incheon Int'l" style={{ flex: 1, height: 26, padding: "0 8px", fontSize: 12 }} />
                </div>
              </div>
            </div>

            {/* Schedule Leg Grid */}
            <div style={{ marginTop: 8 }}>
              <div className="subhead"><div className="subhead__bar" />Schedule Legs<div className="panel__actions" style={{ marginLeft: "auto" }}><button className="btn btn--sm">+ Row</button></div></div>
              <div style={{ overflowX: "auto" }}>
                <GridList
                  columns={SCHEDULE_LEG_COLS}
                  data={SCHEDULE_LEG_DATA}
                  rowKey={(_, i) => i}
                />
              </div>
              {/* Derived values (read-only) */}
              <div style={{ background: "var(--bg-sunken)", borderRadius: 4, padding: "6px 10px", marginTop: 6, fontSize: 11 }}>
                <div style={{ display: "grid", gridTemplateColumns: "auto 1fr auto 1fr auto 1fr", gap: "4px 8px", alignItems: "center" }}>
                  <span style={{ color: "var(--ink-3)" }}>Destination</span>
                  <input readOnly value="PVG" style={{ background: "transparent", border: "none", color: "var(--accent-ink)", fontFamily: "var(--font-mono)", fontWeight: 600, outline: "none", fontSize: 12 }} />
                  <span style={{ color: "var(--ink-3)" }}>On board</span>
                  <input readOnly value="26APR" style={{ background: "transparent", border: "none", color: "var(--ink-2)", fontFamily: "var(--font-mono)", outline: "none", fontSize: 12 }} />
                  <span style={{ color: "var(--ink-3)" }}>Arrival</span>
                  <input readOnly value="26APR" style={{ background: "transparent", border: "none", color: "var(--ink-2)", fontFamily: "var(--font-mono)", outline: "none", fontSize: 12 }} />
                </div>
              </div>
            </div>

            {/* Issue info */}
            {variant.issueFields.length > 0 && (
              <div style={{ marginTop: 8 }}>
                <div className="subhead"><div className="subhead__bar" />Issue Information</div>
                {variant.issueFields.map((f) => (
                  <div key={f} className="li">
                    <span className="li__label">{f}</span>
                    <div className="li__input">
                      <input defaultValue={f === "Issue Date" ? "2026-04-20" : f === "Issue Place" ? "ICN" : ""} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12 }} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* TRADE (AIR) */}
      <div style={{ gridArea: "trade", minHeight: 0, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Trade</span></div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="sched-list">
              {[
                { l: "Currency",       v: "USD" },
                { l: "Incoterms",      v: "DAP" },
                { l: "Freight Term",   v: "Prepaid" },
                { l: "Other Term",     v: "" },
                { l: "D.V Carriage",   v: "N.V.D." },
                { l: "Insurance",      v: "NIL" },
                { l: "D.V Customs",    v: "AS PER INV." },
                { l: "Account Info",   v: "FREIGHT PREPAID" },
                ...(isImp ? [{ l: "F.H.D", v: "Not" }] : []),
              ].map((f) => (
                <div key={f.l} className="li">
                  <span className="li__label">{f.l}</span>
                  <div className="li__input">
                    <input defaultValue={f.v} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12 }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* CARGO (AIR) */}
      <div style={{ gridArea: "cargo", minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
          <div className="panel__body">
            <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "8px 16px" }}>
              {[
                { l: "Package",    v: "1300 CTN" },
                { l: "Gross W/T", v: "30,600 KGS" },
                { l: "Volume W/T",v: "14,583" },
                { l: "Charge W/T",v: "30,600" },
                { l: "Rate Class", v: "GCR" },
                { l: "CBM",       v: "87.5" },
              ].map((f) => (
                <div key={f.l} className="field">
                  <div className="field__label">{f.l}</div>
                  <div className="field__input"><input defaultValue={f.v} /></div>
                </div>
              ))}
            </div>
            <div style={{ marginTop: 8, display: "flex", gap: 8 }}>
              <button className="btn btn--sm">Apply Weight</button>
              <span style={{ fontSize: 11, color: "var(--ink-4)", alignSelf: "center" }}>CM / 6000</span>
            </div>
          </div>
        </div>
      </div>

      {/* DIMENSION Grid */}
      <div style={{ gridArea: "dim", minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Dimension</span><span className="panel__rowcount">1</span><div className="panel__actions"><button className="btn btn--sm">+ Add</button></div></div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <GridList columns={DIMENSION_COLS} data={DIMENSION_DATA} rowKey={(_, i) => i} />
          </div>
          <div className="grid-foot"><div className="grid-foot__spacer" /><span>Qty: <strong>1,300</strong></span><span>CBM: <strong>87.5</strong></span><span>Vol. Wt.: <strong>14,583</strong></span></div>
        </div>
      </div>

      {/* MARKS */}
      <div style={{ gridArea: "marks", minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Marks & Numbers</span></div>
          <div className="panel__body" style={{ flex: 1 }}><textarea className="textarea textarea--tall" defaultValue={"MADE IN KOREA\nCTN NO. 1-1300"} /></div>
        </div>
      </div>

      {/* DESCRIPTION (AIR: Nature & Quantity) */}
      <div style={{ gridArea: "desc", minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Nature & Quantity of Goods</span></div>
          <div className="panel__body" style={{ flex: 1 }}><textarea className="textarea textarea--tall" defaultValue={"ELECTRONIC GOODS\n(MOBILE PHONE PARTS)\n1,300 CARTONS"} /></div>
        </div>
      </div>

      {/* ITEM */}
      <div style={{ gridArea: "item", minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Item / HS Code</span><span className="panel__rowcount">1</span><div className="panel__actions"><button className="btn btn--sm">+ Add</button></div></div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <GridList columns={AIR_ITEM_COLS} data={AIR_ITEM_DATA} rowKey={(_, i) => i} />
          </div>
        </div>
      </div>
    </div>
  );
}
