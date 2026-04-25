import { Search } from "lucide-react";

interface Props { isExp: boolean }

export function PartyPanel({ isExp }: Props) {
  return (
    <div className="zone-party">
      <div className="panel panel--full">
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Party</span>
        </div>
        <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
          {([
            { role: "SHIPPER",     filled: true  },
            { role: "CONSIGNEE",   filled: true, required: !isExp },
            { role: "NOTIFY",      filled: false },
            { role: "DOC PARTNER", filled: false, required: true  },
          ] as const).map((p) => (
            <div key={p.role} className="party-block">
              <div className="party-block__head">
                <span style={{ fontSize: 11, color: "var(--ink)", minWidth: 90, flexShrink: 0 }}>
                  {p.role}
                  {(p as { required?: boolean }).required && (
                    <span style={{ color: "var(--required)", marginLeft: 3 }}>*</span>
                  )}
                </span>
                <div style={{ display: "grid", gridTemplateColumns: "120px 1fr", gap: 8, flex: "1 1 auto", alignItems: "center", minWidth: 180 }}>
                  <div style={{ position: "relative" }}>
                    <input
                      className="text-mono"
                      placeholder="Code"
                      defaultValue={p.filled ? (p.role === "SHIPPER" ? "HJTR001" : p.role === "CONSIGNEE" ? "SHTRC001" : "") : ""}
                      style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 20px 4px 2px", fontSize: 10, color: "var(--ink)", outline: "none", fontFamily: "var(--font-mono)" }}
                    />
                    <Search size={12} style={{ position: "absolute", right: 4, top: "50%", transform: "translateY(-50%)", color: "var(--ink-4)", cursor: "pointer" }} />
                  </div>
                  <input
                    placeholder="Company Name"
                    defaultValue={p.filled ? (p.role === "SHIPPER" ? "한진무역(주)" : p.role === "CONSIGNEE" ? "SHANGHAI TRADING CO., LTD." : "") : ""}
                    style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 10, color: "var(--ink)", outline: "none" }}
                  />
                </div>
                <div className="party-block__head-actions">
                  {p.role === "CONSIGNEE" && <button className="party-block__head-btn">To Order</button>}
                  {p.role === "NOTIFY"    && <button className="party-block__head-btn">Same as Cne.</button>}
                  <button className="party-block__head-btn">Clear</button>
                </div>
              </div>
              <textarea
                className="textarea textarea--party"
                placeholder="Address (free text)"
                defaultValue={
                  p.filled && p.role === "SHIPPER"
                    ? "서울특별시 중구 을지로 100\n한진무역 빌딩 12층\nTEL: +82-2-1234-5678"
                    : p.filled && p.role === "CONSIGNEE"
                    ? "1200 LUJIAZUI RING ROAD\nPUDONG NEW DISTRICT\nSHANGHAI 200120, CHINA"
                    : ""
                }
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
