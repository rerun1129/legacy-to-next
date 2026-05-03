import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { DateCell, TimeCell, PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface Props { variant?: AnyVariantConfig }

interface LegRow { id: number; to: string; by: string; flight: string; onBoard: string; boardTime: string; arrival: string; arrTime: string; }

const LEG_COLS: GridColumn<LegRow>[] = [
  { key: "_no",       width: 32, align: "center", label: "#",        className: "row-num", render: (_, __, i) => i + 1 },
  { key: "to",        width: 40, align: "center", label: "To",       render: v => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "by",        width: 32, align: "center", label: "By",       render: v => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "flight",    width: 50, align: "center", label: "Flight",   render: v => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "onBoard",   width: 96, align: "center", label: "On Board", render: v => <DateCell defaultValue={String(v)} /> },
  { key: "boardTime", width: 58, align: "center", label: "Time",     render: v => <TimeCell defaultValue={String(v)} /> },
  { key: "arrival",   width: 96, align: "center", label: "Arrival",  render: v => <DateCell defaultValue={String(v)} /> },
  { key: "arrTime",   width: 58, align: "center", label: "Time",     render: v => <TimeCell defaultValue={String(v)} /> },
];

const LEG_DATA: LegRow[] = [];

const LI_ST: React.CSSProperties = { width: "100%", height: 22, padding: "0 8px", fontSize: 10 };

export function AirSchedulePanel({ variant }: Props) {
  if (!variant) return null;
  const isExp      = variant.direction === "EXP";
  const panelScope = `air-schedule-panel.${variant.key}`;

  const airlineItems: FieldItemDef[] = [
    {
      key: "airline",
      render: () => (
        <div className="li">
          <span className="li__label is-required">{isExp ? "Airline" : "Carrier"}</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input defaultValue="" style={{ width: 60, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
            <input defaultValue="" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
          </div>
        </div>
      ),
    },
    {
      key: "departure",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Departure</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input defaultValue="" style={{ width: 60, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
            <input defaultValue="" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
          </div>
        </div>
      ),
    },
  ];

  const issueItems: FieldItemDef[] = variant.issueFields.map(f => ({
    key:    f.toLowerCase().replace(/[^a-z0-9]/g, "-"),
    render: () => (
      <div className="li">
        <span className="li__label">{f}</span>
        <div className="li__input">
          {f.includes("Date")
            ? <PanelDateInput defaultValue={""} />
            : <input defaultValue={""} style={LI_ST} />}
        </div>
      </div>
    ),
  }));

  const fields: FieldWidgetDef[] = [
    {
      key:   "airline",
      label: isExp ? "Airline" : "Carrier",
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.airline`}
          items={airlineItems}
          cols={1}
          shouldShowRowControls={false}
        />
      ),
    },
    {
      key:   "legs",
      label: "Schedule Legs",
      render: () => (
        <div style={{ overflow: "auto" }}>
          <GridList columns={LEG_COLS} data={LEG_DATA} rowKey={(row) => row.id} />
        </div>
      ),
    },
    ...(variant.issueFields.length > 0
      ? [{
          key:   "issue",
          label: "Issue Information",
          render: () => (
            <>
              <div className="subhead"><div className="subhead__bar" />Issue Information</div>
              <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} cols={1} shouldShowRowControls={false} />
            </>
          ),
        }]
      : []),
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
