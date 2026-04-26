import { Search } from "lucide-react";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";


interface Props { variant: MasterVariantConfig }

const PARTIES = ["SHIPPER", "CONSIGNEE", "NOTIFY"] as const;
const PARTY_BTNS: Record<string, string> = { CONSIGNEE: "To Order", NOTIFY: "Same as Cne." };

function PartyBlock({ role }: { role: string }) {
  return (
    <div className="party-block">
      <div className="party-block__head">
        <span style={{ fontSize: 11, minWidth: 90, flexShrink: 0 }}>{role}</span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input className="text-mono" placeholder="Code" />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" placeholder="Company Name" />
        </div>
        <div className="party-block__head-actions">
          {PARTY_BTNS[role] && <button className="party-block__head-btn">{PARTY_BTNS[role]}</button>}
          <button className="party-block__head-btn">Clear</button>
        </div>
      </div>
      <LineNumberTextarea placeholder="Address (free text)" style={{ height: 108 }} />
    </div>
  );
}

export function MasterPartyPanel() {
  const fields: FieldWidgetDef[] = PARTIES.map(role => ({
    key:    role.toLowerCase(),
    label:  role,
    render: () => <PartyBlock role={role} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="master-party-panel" fields={fields} />
      </div>
    </div>
  );
}

// ── Marks ──────────────────────────────────────────────────
export function MasterMarksPanel() {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Marks &amp; Numbers</span></div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <LineNumberTextarea defaultValue="MADE IN KOREA" style={{ flex: 1, minHeight: 0 }} />
      </div>
    </div>
  );
}

// ── Goods Description ──────────────────────────────────────
export function MasterGoodsDescPanel({ variant }: Props) {
  const isSea = variant.mode === "SEA";
  const title = isSea ? "Description of Goods" : "Nature of Goods";
  const content = isSea ? "SAID TO CONTAIN\nELECTRONIC GOODS" : "CONSOLIDATION SHIPMENT\nAS PER ATTACHED MANIFEST";
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{title}</span></div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        {isSea && (
          <div className="li" style={{ marginBottom: 8, flexShrink: 0 }}>
            <span className="li__label">Clause</span>
            <div className="li__input"><select style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}><option>-- 부지약관 --</option><option>SAID TO CONTAIN</option></select></div>
          </div>
        )}
        <LineNumberTextarea defaultValue={content} style={{ flex: 1, minHeight: 0 }} />
      </div>
    </div>
  );
}

// ── Remark ─────────────────────────────────────────────────
export function MasterRemarkPanel() {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Remark</span></div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <LineNumberTextarea style={{ flex: 1, minHeight: 0 }} />
      </div>
    </div>
  );
}
