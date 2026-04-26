import { Search } from "lucide-react";

export function TradePanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Trade & Performance</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <div className="sched-list">
          {[
            { l: "Incoterms",    v: "FOB",     req: true  },
            { l: "Freight Term", v: "Prepaid",  req: true  },
            { l: "Payable At",   v: "ORIGIN",   req: false },
            { l: "Co-Load",      v: "N",        req: false },
          ].map((f) => (
            <div key={f.l} className="li">
              <span className={`li__label${f.req ? " is-required" : ""}`}>{f.l}</span>
              <div className="li__input">
                <input defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
              </div>
            </div>
          ))}
          <div style={{ marginTop: 8 }}>
            <div className="subhead"><div className="subhead__bar" />Performance</div>
            {[
              { l: "Actual Customer", code: "HJTR001", name: "한진무역(주)" },
              { l: "Sales Man",       code: "LJY",     name: "이진영" },
              { l: "Operator",        code: "KYS",     name: "김영선" },
              { l: "Team",            code: "SEA-EXP", name: "해상수출팀" },
            ].map((f) => (
              <div key={f.l} className="lcn" style={{ marginBottom: 4 }}>
                <span className="lcn__label is-required">{f.l}</span>
                <div className="lcn__code" style={{ position: "relative" }}>
                  <input defaultValue={f.code} style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
                  <Search size={12} className="lcn__icon" />
                </div>
                <input className="lcn__name" defaultValue={f.name} placeholder="Name" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
