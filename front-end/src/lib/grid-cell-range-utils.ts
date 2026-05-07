export interface CellRef {
  rowKey: string;
  colKey: string;
}

export interface CellRange {
  anchor: CellRef;
  focus: CellRef;
}

export interface NormalizedRange {
  minRow: number;
  maxRow: number;
  minCol: number;
  maxCol: number;
}

export function resolveRowKeyAsString<T>(
  row: T,
  index: number,
  rowKey: ((row: T, index: number) => string | number) | undefined,
): string {
  if (rowKey) return String(rowKey(row, index));
  const id = (row as Record<string, unknown>).id as string | number | undefined;
  return String(id ?? index);
}

/** CellRange를 TSV(탭+CRLF)로 직렬화. DOM textContent 우선, 가상 스크롤 미DOM 셀은 raw value fallback. */
export function serializeRange<T>(
  range: CellRange,
  data: T[],
  visibleColumns: { key: string | number | symbol }[],
  rowKey: ((row: T, index: number) => string | number) | undefined,
  getTable: () => HTMLTableElement | null,
): string {
  const rowKeys = data.map((row, i) => resolveRowKeyAsString(row, i, rowKey));
  const colKeys = visibleColumns.map((c) => String(c.key));

  const aR = rowKeys.indexOf(range.anchor.rowKey);
  const fR = rowKeys.indexOf(range.focus.rowKey);
  const aC = colKeys.indexOf(range.anchor.colKey);
  const fC = colKeys.indexOf(range.focus.colKey);

  if ([aR, fR, aC, fC].some((x) => x < 0)) return "";

  const r0 = Math.min(aR, fR);
  const r1 = Math.max(aR, fR);
  const c0 = Math.min(aC, fC);
  const c1 = Math.max(aC, fC);

  const table = getTable();
  const lines: string[] = [];

  for (let ri = r0; ri <= r1; ri++) {
    const rk = rowKeys[ri];
    const cells: string[] = [];
    for (let ci = c0; ci <= c1; ci++) {
      const ck = colKeys[ci];
      const tdEl = table?.querySelector(
        `td[data-row-key="${CSS.escape(rk)}"][data-col-key="${CSS.escape(ck)}"]`
      ) as HTMLTableCellElement | null;
      if (tdEl) {
        cells.push((tdEl.textContent ?? "").replace(/[\t\r\n]+/g, " "));
        continue;
      }
      const row = data[ri];
      const col = visibleColumns.find((c) => String(c.key) === ck);
      const raw =
        row && col
          ? typeof col.key === "string"
            ? (row as Record<string, unknown>)[col.key]
            : row[col.key as keyof T]
          : "";
      cells.push(String(raw ?? "").replace(/[\t\r\n]+/g, " "));
    }
    lines.push(cells.join("\t"));
  }

  return lines.join("\r\n");
}
