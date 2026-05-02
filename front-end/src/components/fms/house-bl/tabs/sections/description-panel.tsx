import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function DescriptionPanel() {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Description</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="li" style={{ marginBottom: 8, flexShrink: 0 }}>
          <span className="li__label">Clause</span>
          <div className="li__input">
            <select style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }}>
              <option value="">-- 부지약관 --</option>
              <option>SAID TO CONTAIN</option>
              <option>SHIPPER&apos;S LOAD AND COUNT</option>
            </select>
          </div>
        </div>
        <LineNumberTextarea
          defaultValue={"ELECTRONIC GOODS\n(MOBILE PHONE PARTS)\n1,300 CARTONS\nSAID TO CONTAIN"}
          style={{ flex: 1, minHeight: 0 }}
        />
      </div>
    </div>
  );
}
