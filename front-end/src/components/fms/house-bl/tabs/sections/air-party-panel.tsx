import { Search } from "lucide-react";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";

interface Props { variant?: AnyVariantConfig }

const PARTIES = [
  { role: "SHIPPER",     req: false },
  { role: "CONSIGNEE",  req: "imp"  },
  { role: "NOTIFY",     req: false  },
  { role: "DOC PARTNER",req: false  },
] as const;

export function AirPartyPanel({ variant }: Props) {
  if (!variant) return null;
  const isImp = variant.direction === "IMP";
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        {PARTIES.map(p => {
          const required = p.req === "imp" ? isImp : p.req;
          return (
            <div key={p.role} className="party-block">
              <div className="party-block__head">
                <span className={required ? "is-required" : undefined} style={{ fontSize: 11, minWidth: 90, flexShrink: 0 }}>
                  {p.role}
                </span>
                <div className="party-cn">
                  <div className="party-cn__code">
                    <input placeholder="Code" />
                    <Search size={12} className="party-cn__icon" />
                  </div>
                  <input className="party-cn__name" placeholder="Company Name" />
                </div>
                <div className="party-block__head-actions">
                  {p.role === "CONSIGNEE" && <button className="party-block__head-btn">To Order</button>}
                  {p.role === "NOTIFY"    && <button className="party-block__head-btn">Same as Cne.</button>}
                  <button className="party-block__head-btn">Clear</button>
                </div>
              </div>
              <LineNumberTextarea placeholder="Address (free text)" style={{ height: 100 }} />
            </div>
          );
        })}
      </div>
    </div>
  );
}
