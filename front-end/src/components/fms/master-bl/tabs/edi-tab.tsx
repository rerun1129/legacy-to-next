import type { MasterVariantConfig } from "@/lib/bl-variants";
import { getModeLabels } from "@/lib/bl-mode-labels";

interface Props { variant: MasterVariantConfig }

export function MasterEdiTab({ variant }: Props) {
  const modeLabels = getModeLabels(variant.mode);
  return (
    <div className="page-body" style={{ overflow: "auto", display: "flex", flexDirection: "column", gap: 10 }}>
      <div className="panel">
        <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">EDI Identifiers</span></div>
        <div className="panel__body">
          <div className="form-grid form-grid--4">
            {[
              { label: modeLabels.masterBlNo, value: isSea ? "COSCO2404195" : "180-12345678", req: true },
              { label: "EDI Item",           value: "",     req: false },
              { label: "MRN",                value: "",     req: false },
              { label: "Cargo Type",         value: "GEN",  req: false },
            ].map((f) => (
              <div key={f.label} className={`field${f.req ? " is-required" : ""}`}>
                <div className={`field__label${f.req ? " is-required" : ""}`}>{f.label}</div>
                <div className="field__input"><input defaultValue={f.value} placeholder={f.label} /></div>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="panel" style={{ flex: 1 }}>
        <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">EDI Transmission History</span></div>
        <div className="panel__body"><p style={{ color: "var(--ink-4)", fontSize: "var(--fs-sm)" }}>EDI 전송 이력 없음</p></div>
      </div>
    </div>
  );
}
