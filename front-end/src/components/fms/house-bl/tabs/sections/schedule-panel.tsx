import { Search } from "lucide-react";
import type { BLVariantConfig } from "@/lib/bl-variants";

interface Props { variant: BLVariantConfig }

export function SchedulePanel({ variant }: Props) {
  return (
    <div className="zone-schedule">
      <div className="panel panel--full">
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Schedule</span>
        </div>
        <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
          <div className="sched-list">
            <div className="li">
              <span className="li__label is-required">Liner</span>
              <div className="li__input" style={{ gap: 4 }}>
                <input placeholder="Code" defaultValue="COSCO" style={{ width: 80, height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none" }} />
                <input defaultValue="COSCO SHIPPING" style={{ flex: 1, height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
              </div>
            </div>
            <div className="sched-pair">
              {[{ l: "Vessel *", v: "COSCO EXCELLENCE" }, { l: "Voyage *", v: "0412E" }].map((f) => (
                <div key={f.l} className="li">
                  <span className="li__label is-required">{f.l}</span>
                  <div className="li__input"><input defaultValue={f.v} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div>
                </div>
              ))}
            </div>
            <div className="sched-pair">
              {[{ l: "ETD *", t: "date", v: "2026-04-24" }, { l: "ETA", t: "date", v: "2026-05-08" }].map((f) => (
                <div key={f.l} className="li">
                  <span className="li__label is-required">{f.l}</span>
                  <div className="li__input"><input type={f.t} defaultValue={f.v} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div>
                </div>
              ))}
            </div>
            <div className="li"><span className="li__label">On Board</span><div className="li__input"><input type="date" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div></div>

            <div style={{ marginTop: 8 }}>
              <div className="subhead"><div className="subhead__bar" />Ports</div>
              {[
                { label: "POL *",      code: "KRBSAN", name: "Busan" },
                { label: "POD *",      code: "CNSHA",  name: "Shanghai" },
                { label: "Delivery",   code: "",       name: "" },
              ].map((p) => (
                <div key={p.label} className="lcn" style={{ marginBottom: 4 }}>
                  <span className="lcn__label">{p.label}</span>
                  <div className="lcn__code" style={{ position: "relative" }}>
                    <input defaultValue={p.code} placeholder="UNLOC" style={{ width: "100%", height: 26, padding: "0 24px 0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none" }} />
                    <Search size={12} className="lcn__icon" />
                  </div>
                  <input className="lcn__name" defaultValue={p.name} placeholder="Port Name" />
                </div>
              ))}
            </div>

            {/* Issue Information */}
            {variant.issueFields.length > 0 && (
              <div style={{ marginTop: 8 }}>
                <div className="subhead"><div className="subhead__bar" />Issue Information</div>
                {variant.issueFields.map((f) => (
                  <div key={f} className="li">
                    <span className="li__label">{f}</span>
                    <div className="li__input">
                      <input
                        type={f.includes("Date") ? "date" : "text"}
                        defaultValue={f === "Issue Date" ? "2026-04-20" : f === "No. of B/L" ? "3" : f === "Issue Place" ? "BUSAN" : ""}
                        style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
            {variant.hasDoDate && (
              <div style={{ marginTop: 8 }}>
                <div className="subhead"><div className="subhead__bar" />D/O Date</div>
                <div className="li">
                  <span className="li__label">D/O Date</span>
                  <div className="li__input"><input type="date" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
