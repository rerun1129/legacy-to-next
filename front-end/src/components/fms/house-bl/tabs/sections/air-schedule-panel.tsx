import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { DateCell, TimeCell } from "@/components/shared/grid-cell-inputs";
import type { BLVariantConfig } from "@/lib/bl-variants";

interface Props { variant: BLVariantConfig }

interface LegRow { to: string; by: string; flight: string; onBoard: string; boardTime: string; arrival: string; arrTime: string; }

const LEG_COLS: GridColumn<LegRow>[] = [
  { key: "_no",       width: 32, align: "center", label: "#", className: "row-num", render: (_, __, i) => i + 1 },
  { key: "to",        width: 40, align: "center", label: "To",       render: v => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "by",        width: 32, align: "center", label: "By",       render: v => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "flight",    width: 50, align: "center", label: "Flight",   render: v => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "onBoard",   width: 96, align: "center", label: "On Board", render: v => <DateCell defaultValue={String(v)} /> },
  { key: "boardTime", width: 58, align: "center", label: "Time",     render: v => <TimeCell defaultValue={String(v)} /> },
  { key: "arrival",   width: 96, align: "center", label: "Arrival",  render: v => <DateCell defaultValue={String(v)} /> },
  { key: "arrTime",   width: 58, align: "center", label: "Time",     render: v => <TimeCell defaultValue={String(v)} /> },
];

const LEG_DATA: LegRow[] = [
  { to: "PVG", by: "KE", flight: "KE851", onBoard: "2026-04-26", boardTime: "09:30", arrival: "2026-04-26", arrTime: "11:45" },
  { to: "NRT", by: "KE", flight: "KE701", onBoard: "2026-04-27", boardTime: "08:00", arrival: "2026-04-27", arrTime: "09:20" },
  { to: "LAX", by: "OZ", flight: "OZ202", onBoard: "2026-04-30", boardTime: "22:00", arrival: "2026-05-01", arrTime: "19:30" },
];

export function AirSchedulePanel({ variant }: Props) {
  const isExp = variant.direction === "EXP";
  const label = isExp ? "Airline" : "Carrier";
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <div className="sched-list">
          <div className="li">
            <span className="li__label is-required">{label}</span>
            <div className="li__input" style={{ gap: 4 }}>
              <input defaultValue={isExp ? "KE" : "OZ"} style={{ width: 60, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
              <input defaultValue={isExp ? "Korean Air" : "Asiana Airlines"} style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
            </div>
          </div>
          <div className="li">
            <span className="li__label is-required">Departure</span>
            <div className="li__input" style={{ gap: 4 }}>
              <input defaultValue="ICN" style={{ width: 60, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
              <input defaultValue="Incheon Int'l" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
            </div>
          </div>
        </div>
        <div className="subhead" style={{ marginTop: 8 }}><div className="subhead__bar" />Schedule Legs</div>
        <div style={{ overflow: "auto" }}><GridList columns={LEG_COLS} data={LEG_DATA} rowKey={(_, i) => i} /></div>
        {variant.issueFields.length > 0 && (
          <div style={{ marginTop: 8 }}>
            <div className="subhead"><div className="subhead__bar" />Issue Information</div>
            {variant.issueFields.map(f => (
              <div key={f} className="li">
                <span className="li__label">{f}</span>
                <div className="li__input"><input style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
