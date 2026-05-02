import { Search } from "lucide-react";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface Props { variant?: AnyVariantConfig; isExp?: boolean }

interface PartyConfig {
  key:      string;
  role:     string;
  filled:   boolean;
  required: boolean;
}

function PartyBlock({ cfg, isExp }: { cfg: PartyConfig; isExp: boolean }) {
  const defaultCode =
    cfg.role === "SHIPPER"    ? "HJTR001"  :
    cfg.role === "CONSIGNEE"  ? "SHTRC001" : "";
  const defaultName =
    cfg.role === "SHIPPER"    ? "한진무역(주)"              :
    cfg.role === "CONSIGNEE"  ? "SHANGHAI TRADING CO., LTD." : "";
  const defaultAddr =
    cfg.role === "SHIPPER"    ? "서울특별시 중구 을지로 100\n한진무역 빌딩 12층\nTEL: +82-2-1234-5678" :
    cfg.role === "CONSIGNEE"  ? "1200 LUJIAZUI RING ROAD\nPUDONG NEW DISTRICT\nSHANGHAI 200120, CHINA" : "";

  const isRequired = cfg.role === "CONSIGNEE" ? !isExp : cfg.required;

  return (
    <div className="party-block">
      <div className="party-block__head">
        <span className={isRequired ? "is-required" : undefined} style={{ fontSize: 11, minWidth: 90, flexShrink: 0 }}>
          {cfg.role}
        </span>
        <div className="party-cn">
          <div className="party-cn__code">
            <input className="text-mono" placeholder="Code" defaultValue={cfg.filled ? defaultCode : ""} />
            <Search size={12} className="party-cn__icon" />
          </div>
          <input className="party-cn__name" placeholder="Company Name" defaultValue={cfg.filled ? defaultName : ""} />
        </div>
        <div className="party-block__head-actions">
          {cfg.role === "CONSIGNEE" && <button className="party-block__head-btn">To Order</button>}
          {cfg.role === "NOTIFY"    && <button className="party-block__head-btn">Same as Cne.</button>}
          <button className="party-block__head-btn">Clear</button>
        </div>
      </div>
      <LineNumberTextarea
        placeholder="Address (free text)"
        defaultValue={cfg.filled ? defaultAddr : ""}
        style={{ height: 108 }}
      />
    </div>
  );
}

export function PartyPanel({ variant, isExp = false }: Props) {
  const panelScope = variant ? `party-panel.${variant.key}` : "party-panel";
  const PARTY_CONFIGS: PartyConfig[] = [
    { key: "shipper",     role: "SHIPPER",     filled: true,  required: false },
    { key: "consignee",   role: "CONSIGNEE",   filled: true,  required: !isExp },
    { key: "notify",      role: "NOTIFY",      filled: false, required: false  },
    { key: "doc-partner", role: "DOC PARTNER", filled: false, required: true   },
  ];

  const fields: FieldWidgetDef[] = PARTY_CONFIGS.map(cfg => ({
    key:   cfg.key,
    label: cfg.role,
    render: () => <PartyBlock cfg={cfg} isExp={isExp} />,
  }));

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Party</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
