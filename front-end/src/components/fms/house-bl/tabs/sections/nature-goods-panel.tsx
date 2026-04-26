import { LineNumberTextarea } from "@/components/shared/line-number-textarea";

export function NatureGoodsPanel() {
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Nature &amp; Quantity of Goods</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <LineNumberTextarea
          defaultValue={"ELECTRONIC GOODS\n(MOBILE PHONE PARTS)\n1,300 CARTONS"}
          style={{ flex: 1, minHeight: 0 }}
        />
      </div>
    </div>
  );
}
