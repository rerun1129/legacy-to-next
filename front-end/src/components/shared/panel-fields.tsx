export function PackageField({ qty = "", unit = "", height = 22 }: {
  qty?: string; unit?: string; height?: number;
}) {
  const sel: React.CSSProperties = {
    height, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
    border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
  };
  return (
    <div className="li">
      <span className="li__label">Package</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input type="number" step="1" defaultValue={qty}
          style={{ flex: 1, height, padding: "0 6px", fontSize: 10 }} />
        <select defaultValue={unit} style={sel}>
          <option value=""></option><option>CTN</option><option>PKG</option><option>BAG</option>
          <option>PLT</option><option>BOX</option><option>PCS</option><option>ROL</option>
        </select>
      </div>
    </div>
  );
}
