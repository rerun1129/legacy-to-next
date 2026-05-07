import { type CellRange, resolveRowKeyAsString } from "./grid-cell-range-utils";

export interface VisibleCol {
  key: string | number | symbol;
  width?: number;
  minWidth?: number;
}

export interface OverlayPositionCtx<T> {
  data: T[];
  cols: VisibleCol[];
  rowKey: ((row: T, i: number) => string | number) | undefined;
  getTable: () => HTMLTableElement | null;
  rowHeight: number;
  getRowOffset?: (i: number) => { start: number; size: number } | null;
}

/** range → overlay div 위치/크기를 계산해 imperative하게 갱신하는 공통 헬퍼. */
export function applyOverlayPosition<T>(
  overlayEl: HTMLDivElement | null,
  range: CellRange | null,
  ctx: OverlayPositionCtx<T>,
): void {
  if (!overlayEl) return;
  if (!range) {
    overlayEl.style.display = "none";
    return;
  }
  const { data, cols, rowKey, getTable, rowHeight, getRowOffset } = ctx;

  let aR = -1;
  let fR = -1;
  for (let i = 0; i < data.length; i++) {
    const k = resolveRowKeyAsString(data[i], i, rowKey);
    if (k === range.anchor.rowKey) aR = i;
    if (k === range.focus.rowKey) fR = i;
    if (aR !== -1 && fR !== -1) break;
  }
  if (aR < 0 || fR < 0) {
    overlayEl.style.display = "none";
    return;
  }

  let aC = -1;
  let fC = -1;
  for (let i = 0; i < cols.length; i++) {
    const ck = String(cols[i].key);
    if (ck === range.anchor.colKey) aC = i;
    if (ck === range.focus.colKey) fC = i;
  }
  if (aC < 0 || fC < 0) {
    overlayEl.style.display = "none";
    return;
  }

  const minRow = Math.min(aR, fR);
  const maxRow = Math.max(aR, fR);
  const minCol = Math.min(aC, fC);
  const maxCol = Math.max(aC, fC);

  let left = 0;
  for (let i = 0; i < minCol; i++) {
    left += cols[i].width ?? cols[i].minWidth ?? 80;
  }
  let width = 0;
  for (let i = minCol; i <= maxCol; i++) {
    width += cols[i].width ?? cols[i].minWidth ?? 80;
  }

  const table = getTable();
  const thead = table?.querySelector("thead") as HTMLElement | null;
  // getBoundingClientRect으로 소수점 높이를 보존해 누적 오차를 방지한다.
  const theadH = thead?.getBoundingClientRect().height ?? 0;
  // table이 grid-wrap 기준 정확히 0이 아닐 수 있으므로 offsetTop/Left 포함
  const tableOffsetTop = table?.offsetTop ?? 0;
  const tableOffsetLeft = table?.offsetLeft ?? 0;
  const minM = getRowOffset?.(minRow) ?? null;
  const maxM = getRowOffset?.(maxRow) ?? null;
  let top: number;
  let height: number;
  if (minM && maxM) {
    // virtualizer 실측값으로 계산해 스크롤 누적 오차를 제거한다.
    top = tableOffsetTop + theadH + minM.start;
    height = maxM.start + maxM.size - minM.start;
  } else {
    // getRowOffset 미제공 시 rowHeight 고정값으로 fallback
    top = tableOffsetTop + theadH + rowHeight * minRow;
    height = rowHeight * (maxRow - minRow + 1);
  }
  left += tableOffsetLeft;

  overlayEl.style.display = "block";
  overlayEl.style.transform = `translate3d(${left}px, ${top}px, 0)`;
  overlayEl.style.width = `${width}px`;
  overlayEl.style.height = `${height}px`;
}
