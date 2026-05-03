"use client";

import { Search } from "lucide-react";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

// ── Stub 렌더러 — form 없이 hardcoded 값으로 구조 표현 ─────

export function SeaScheduleStub({ panelScope, isExp }: { panelScope: string; isExp: boolean }) {
  const linerItems: FieldItemDef[] = [
    { key: "liner", render: () => (
      <div className="li">
        <span className="li__label is-required">Liner</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue="COSCO" style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue="COSCO SHIPPING" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "vessel", render: () => (
      <div className="li">
        <span className="li__label is-required">Vessel</span>
        <div className="li__input">
          <input defaultValue="COSCO EXCELLENCE" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "voyage", render: () => (
      <div className="li">
        <span className="li__label is-required">Voyage</span>
        <div className="li__input">
          <input defaultValue="0412E" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "etd", render: () => (
      <div className="li">
        <span className="li__label is-required">ETD</span>
        <div className="li__input"><PanelDateInput defaultValue="2026-04-24" required /></div>
      </div>
    )},
    { key: "eta", render: () => (
      <div className="li">
        <span className="li__label is-required">ETA</span>
        <div className="li__input"><PanelDateInput defaultValue="2026-05-08" required /></div>
      </div>
    )},
  ];

  const portItems: FieldItemDef[] = [
    { key: "pol", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">POL</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input defaultValue="KRBSAN" placeholder="UNLOC"
            style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" defaultValue="Busan" placeholder="Port" style={{ fontSize: 10 }} />
      </div>
    )},
    { key: "pod", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">POD</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input defaultValue="CNSHA" placeholder="UNLOC"
            style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" defaultValue="Shanghai" placeholder="Port" style={{ fontSize: 10 }} />
      </div>
    )},
    { key: "delivery", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label">Delivery</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input placeholder="UNLOC"
            style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" placeholder="Port" style={{ fontSize: 10 }} />
      </div>
    )},
  ];

  const issueItems: FieldItemDef[] = [
    { key: "issue-date",   render: () => (
      <div className="li">
        <span className="li__label">Issue Date</span>
        <div className="li__input"><PanelDateInput defaultValue="2026-04-20" /></div>
      </div>
    )},
    { key: "freight-term", render: () => (
      <div className="li">
        <span className="li__label">Freight Term</span>
        <div className="li__input">
          <input defaultValue="Prepaid" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
  ];

  const fields: FieldWidgetDef[] = [
    { key: "liner-vessel", label: "Liner & Vessel", render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={linerItems} /> },
    { key: "ports",        label: "Ports",          render: () => <FieldItemGrid itemScope={`${panelScope}.ports`} items={portItems} shouldShowRowControls={false} /> },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];

  return <FieldWidgetList panelScope={panelScope} fields={fields} />;
}

export function AirScheduleStub({ panelScope, isExp }: { panelScope: string; isExp: boolean }) {
  const carrierItems: FieldItemDef[] = [
    { key: "carrier", render: () => (
      <div className="li">
        <span className="li__label is-required">{isExp ? "Airline" : "Carrier"}</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue={isExp ? "KE" : "OZ"}
            style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue={isExp ? "Korean Air" : "Asiana Airlines"}
            style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "departure", render: () => (
      <div className="li">
        <span className="li__label is-required">Departure</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue="ICN" style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue="Incheon Int'l" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
  ];

  const issueItems: FieldItemDef[] = [
    { key: "issue-date",  render: () => (
      <div className="li">
        <span className="li__label">Issue Date</span>
        <div className="li__input"><PanelDateInput defaultValue="2026-04-20" /></div>
      </div>
    )},
    { key: "signature",   render: () => (
      <div className="li">
        <span className="li__label">Signature</span>
        <div className="li__input">
          <input defaultValue="" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "issue-place", render: () => (
      <div className="li">
        <span className="li__label">Issue Place</span>
        <div className="li__input">
          <input defaultValue="INCHEON" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
  ];

  // stub leg grid — GridList 의존 제거, 단순 table로 표现
  const STUB_LEGS = [
    { id: 1, to: "PVG", by: "KE", flight: "KE851", onBoard: "2026-04-26", boardTime: "09:30", arrival: "2026-04-26", arrTime: "11:45" },
    { id: 2, to: "NRT", by: "KE", flight: "KE701", onBoard: "2026-04-27", boardTime: "08:00", arrival: "2026-04-27", arrTime: "09:20" },
  ];

  const fields: FieldWidgetDef[] = [
    { key: "carrier", label: "Carrier", render: () => <FieldItemGrid itemScope={`${panelScope}.carrier`} items={carrierItems} /> },
    {
      key: "legs", label: "Schedule Legs",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Schedule Legs</div>
          <div style={{ overflow: "auto" }}>
            <table className="grid--list">
              <thead>
                <tr>
                  <th className="row-num">#</th>
                  <th>To</th><th>By</th><th>Flight</th>
                  <th>On Board</th><th>Time</th><th>Arrival</th><th>Time</th>
                </tr>
              </thead>
              <tbody>
                {STUB_LEGS.map((r, i) => (
                  <tr key={r.id}>
                    <td className="row-num">{i + 1}</td>
                    <td><input className="grid__cell-input" defaultValue={r.to} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.by} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.flight} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.onBoard} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.boardTime} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.arrival} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.arrTime} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      ),
    },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];

  return <FieldWidgetList panelScope={panelScope} fields={fields} />;
}
