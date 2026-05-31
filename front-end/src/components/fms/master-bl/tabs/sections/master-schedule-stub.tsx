"use client";

import { Search } from "lucide-react";
import { useTranslations } from "next-intl";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

// ── Stub 렌더러 — form 없이 hardcoded 값으로 구조 표현 ─────

export function SeaScheduleStub({ panelScope, isExp }: { panelScope: string; isExp: boolean }) {
  const tf = useTranslations("fms.masterBl.entry.fields");
  const linerItems: FieldItemDef[] = [
    { key: "liner", render: () => (
      <div className="li">
        <span className="li__label is-required">{tf("liner")}</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue="" style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue="" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "vessel", render: () => (
      <div className="li">
        <span className="li__label is-required">{tf("vessel")}</span>
        <div className="li__input">
          <input defaultValue="" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "voyage", render: () => (
      <div className="li">
        <span className="li__label is-required">{tf("voyage")}</span>
        <div className="li__input">
          <input defaultValue="" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "etd", render: () => (
      <div className="li">
        <span className="li__label is-required">{tf("etd")}</span>
        <div className="li__input"><PanelDateInput defaultValue="" required /></div>
      </div>
    )},
    { key: "eta", render: () => (
      <div className="li">
        <span className="li__label is-required">{tf("eta")}</span>
        <div className="li__input"><PanelDateInput defaultValue="" required /></div>
      </div>
    )},
  ];

  const portItems: FieldItemDef[] = [
    { key: "pol", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">{tf("pol")}</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input defaultValue="" placeholder="UNLOC"
            style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" defaultValue="" placeholder="Port" style={{ fontSize: 10 }} />
      </div>
    )},
    { key: "pod", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">{tf("pod")}</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input defaultValue="" placeholder="UNLOC"
            style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" defaultValue="" placeholder="Port" style={{ fontSize: 10 }} />
      </div>
    )},
    { key: "delivery", render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label">Delivery</span>{/* Delivery: no catalog key needed — stub-only, delivery is not in master SEA schedule */}
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
        <span className="li__label">{tf("issueDate")}</span>
        <div className="li__input"><PanelDateInput defaultValue="" /></div>
      </div>
    )},
    { key: "freight-term", render: () => (
      <div className="li">
        <span className="li__label">{tf("freightTerm")}</span>
        <div className="li__input">
          <input defaultValue="" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
  ];

  const fields: FieldWidgetDef[] = [
    { key: "liner-vessel", label: tf("linerVessel"), render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={linerItems} /> },
    { key: "ports",        label: tf("ports"),       render: () => <FieldItemGrid itemScope={`${panelScope}.ports`} items={portItems} shouldShowRowControls={false} /> },
    ...(isExp ? [{ key: "issue", label: tf("issue"), render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];

  return <FieldWidgetList panelScope={panelScope} fields={fields} />;
}

export function AirScheduleStub({ panelScope, isExp }: { panelScope: string; isExp: boolean }) {
  const tf = useTranslations("fms.masterBl.entry.fields");
  const carrierItems: FieldItemDef[] = [
    { key: "carrier", render: () => (
      <div className="li">
        <span className="li__label is-required">{isExp ? tf("airline") : tf("carrier")}</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue=""
            style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue=""
            style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "departure", render: () => (
      <div className="li">
        <span className="li__label is-required">{tf("departure")}</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input defaultValue="" style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input defaultValue="" style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
  ];

  const issueItems: FieldItemDef[] = [
    { key: "issue-date",  render: () => (
      <div className="li">
        <span className="li__label">{tf("issueDate")}</span>
        <div className="li__input"><PanelDateInput defaultValue="" /></div>
      </div>
    )},
    { key: "signature",   render: () => (
      <div className="li">
        <span className="li__label">{tf("signature")}</span>
        <div className="li__input">
          <input defaultValue="" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
    { key: "issue-place", render: () => (
      <div className="li">
        <span className="li__label">{tf("issuePlace")}</span>
        <div className="li__input">
          <input defaultValue="" style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} />
        </div>
      </div>
    )},
  ];

  // stub leg grid — GridList 의존 제거, 단순 table로 표现
  const STUB_LEGS: Array<{ id: number; to: string; by: string; flight: string; onBoard: string; boardTime: string; arrival: string; arrTime: string }> = [];

  const fields: FieldWidgetDef[] = [
    { key: "carrier", label: tf("carrier"), render: () => <FieldItemGrid itemScope={`${panelScope}.carrier`} items={carrierItems} /> },
    {
      key: "legs", label: tf("scheduleLegs"),
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />{tf("scheduleLegs")}</div>
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
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.to} /></td>
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.by} /></td>
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.flight} /></td>
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.onBoard} /></td>
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.boardTime} /></td>
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.arrival} /></td>
                    <td><input autoComplete="off" className="grid__cell-input" defaultValue={r.arrTime} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      ),
    },
    ...(isExp ? [{ key: "issue", label: tf("issue"), render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];

  return <FieldWidgetList panelScope={panelScope} fields={fields} />;
}
