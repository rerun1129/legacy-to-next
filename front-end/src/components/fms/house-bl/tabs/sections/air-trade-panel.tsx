import type { BLVariantConfig } from "@/lib/bl-variants";

interface Props { variant: BLVariantConfig }

const BASE_FIELDS = [
  { l: "Currency",     v: "USD" },
  { l: "Incoterms",    v: "DAP" },
  { l: "Freight Term", v: "Prepaid" },
  { l: "Other Term",   v: "" },
  { l: "D.V Carriage", v: "N.V.D." },
  { l: "Insurance",    v: "NIL" },
  { l: "D.V Customs",  v: "AS PER INV." },
  { l: "Account Info", v: "FREIGHT PREPAID" },
];

export function AirTradePanel({ variant }: Props) {
  const isImp = variant.direction === "IMP";
  const fields = isImp ? [...BASE_FIELDS, { l: "F.H.D", v: "Not" }] : BASE_FIELDS;
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Trade</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <div className="sched-list">
          {fields.map(f => (
            <div key={f.l} className="li">
              <span className="li__label">{f.l}</span>
              <div className="li__input"><input defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
