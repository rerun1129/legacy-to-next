import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function MarksPanel() {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Marks & Numbers</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <LineNumberTextarea
          defaultValue={"MADE IN KOREA\nCTN NO. 1-500\nGROSS WT: 12,400 KGS"}
          style={{ flex: 1, minHeight: 0 }}
        />
      </div>
    </div>
  );
}
