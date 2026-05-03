"use client";

import { Search } from "lucide-react";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";

const LINER_VESSEL_ITEMS: FieldItemDef[] = [
  {
    key: "liner",
    render: () => (
      <div className="li">
        <span className="li__label">Liner</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <input placeholder="Liner Name" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "vessel",
    render: () => (
      <div className="li">
        <span className="li__label">Vessel</span>
        <div className="li__input">
          <input placeholder="Vessel Name" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "voy",
    render: () => (
      <div className="li">
        <span className="li__label">Voy</span>
        <div className="li__input">
          <input placeholder="Voyage No" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "etd",
    render: () => (
      <div className="li">
        <span className="li__label is-required">ETD</span>
        <div className="li__input"><PanelDateInput defaultValue="" required /></div>
      </div>
    ),
  },
  {
    key: "eta",
    render: () => (
      <div className="li">
        <span className="li__label is-required">ETA</span>
        <div className="li__input"><PanelDateInput defaultValue="" required /></div>
      </div>
    ),
  },
];

const PORT_ITEMS: FieldItemDef[] = [
  {
    key: "pol",
    render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">POL</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} className="lcn__icon" />
        </div>
        <input className="lcn__name" placeholder="Port Name" />
      </div>
    ),
  },
  {
    key: "pod",
    render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label is-required">POD</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} className="lcn__icon" />
        </div>
        <input className="lcn__name" placeholder="Port Name" />
      </div>
    ),
  },
  {
    key: "final-dest",
    render: () => (
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label">Final Dest.</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} className="lcn__icon" />
        </div>
        <input className="lcn__name" placeholder="Port Name" />
      </div>
    ),
  },
  {
    key: "final-eta",
    render: () => (
      <div className="li">
        <span className="li__label">Final ETA</span>
        <div className="li__input"><PanelDateInput defaultValue="" /></div>
      </div>
    ),
  },
];

export function NonBLSchedulePanel() {
  const fields: FieldWidgetDef[] = [
    {
      key:    "liner-vessel",
      label:  "Liner & Vessel",
      render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.liner" items={LINER_VESSEL_ITEMS} />,
    },
    {
      key:    "ports",
      label:  "Ports",
      render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.ports" items={PORT_ITEMS} shouldShowRowControls={false} />,
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Schedule</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="nonbl-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}
