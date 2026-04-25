import { Search } from "lucide-react";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { PartyPanel } from "./sections/party-panel";
import { SchedulePanel } from "./sections/schedule-panel";
import { GridTable, type GridTableColumn } from "@/components/shared/grid-table";

interface ContainerRow {
  cno: string; type: string; seal: string; pkg: number; pkgT: string;
  gw: string; cbm: number; vgm: string;
}

interface ItemRow {
  hs: string; desc: string; qty: string; unit: string; value: string; cur: string;
}

const CONTAINER_COLS: GridTableColumn<ContainerRow>[] = [
  { key: "_no", label: "#", width: 36, className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "cno",  label: "Container No *", width: 160,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "type", label: "Type", width: 70,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "seal", label: "Seal No", width: 110,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "pkg",  label: "Pkg", width: 70, className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "pkgT", label: "Unit", width: 60,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "gw",   label: "G/W", width: 90, className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "cbm",  label: "CBM", width: 80, className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "vgm",  label: "VGM", width: 90, className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
];

const ITEM_COLS: GridTableColumn<ItemRow>[] = [
  { key: "_no",   label: "#", width: 36, className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "hs",    label: "HS Code", width: 100,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "desc",  label: "Description", width: 200,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",   label: "Qty", width: 70, className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "unit",  label: "Unit", width: 60,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "value", label: "Value", width: 100, className: "is-num",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "cur",   label: "Currency", width: 80,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const CONTAINER_DATA: ContainerRow[] = [
  { cno: "CSNU1234567", type: "20GP", seal: "SL123456", pkg: 500, pkgT: "CTN", gw: "12,400", cbm: 22.5, vgm: "12,540" },
  { cno: "TCKU9876543", type: "40HC", seal: "SL789012", pkg: 800, pkgT: "CTN", gw: "18,200", cbm: 65.0, vgm: "18,380" },
];

const ITEM_DATA: ItemRow[] = [
  { hs: "8517.13", desc: "MOBILE PHONE PARTS", qty: "1300", unit: "CTN", value: "48,500.00", cur: "USD" },
];

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
            <GridTable columns={CONTAINER_COLS} data={CONTAINER_DATA} rowKey={(_, i) => i} />
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
            <GridTable columns={ITEM_COLS} data={ITEM_DATA} rowKey={(_, i) => i} />
          </div>
        </div>
      </div>
    </div>
  );
}
