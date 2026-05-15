"use client";

export function MasterContainerGrid() {
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container</span>
        <span className="panel__rowcount">0</span>
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        <table className="grid--list">
          <colgroup>
            <col style={{ width: "40px" }} />
            <col style={{ width: "120px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "80px" }} />
            <col style={{ width: "80px" }} />
            <col style={{ width: "100px" }} />
            <col style={{ width: "80px" }} />
            <col style={{ width: "100px" }} />
          </colgroup>
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th>Container No</th>
              <th>Type</th>
              <th>Seal No. 1</th>
              <th>Seal No. 2</th>
              <th>Seal No. 3</th>
              <th className="is-num">Pkg</th>
              <th>Unit</th>
              <th className="is-num">G/W</th>
              <th className="is-num">CBM</th>
              <th className="is-num">VGM</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td colSpan={11} style={{ textAlign: "center", padding: 8, fontSize: 11, color: "var(--ink-3)" }}>
                No rows.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
