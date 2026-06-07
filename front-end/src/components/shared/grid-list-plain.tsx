"use client";

import { useCallback, useLayoutEffect, useRef, useState, type RefObject } from "react";
import { useScrollRestore } from "@/lib/use-scroll-restore";
import { useVirtualizer } from "@tanstack/react-virtual";
import { GridListProps, ROW_HEIGHT_PX } from "./grid-list";
import { GridRow } from "./grid-list-row";
import { GridFooter } from "./grid-list-footer";
import { useGridCellSelection } from "@/lib/use-grid-cell-selection";
import { useStableOptionalCallback } from "@/lib/use-stable-callback";

function useColResize() {
  const [colWidths, setColWidths] = useState<Record<string, number>>({});

  const handleResizePointerDown = useCallback(
    (
      e: React.PointerEvent<HTMLSpanElement>,
      colKey: string,
      currentWidth: number,
    ) => {
      e.stopPropagation();
      e.preventDefault();

      const startX = e.clientX;
      const startWidth = currentWidth;
      const target = e.currentTarget;
      target.setPointerCapture(e.pointerId);

      function onPointerMove(ev: PointerEvent) {
        const delta = ev.clientX - startX;
        const next = Math.max(40, startWidth + delta);
        setColWidths((prev) => ({ ...prev, [colKey]: next }));
      }

      function onPointerUp() {
        target.removeEventListener("pointermove", onPointerMove);
        target.removeEventListener("pointerup", onPointerUp);
        target.removeEventListener("pointercancel", onPointerUp);
      }

      target.addEventListener("pointermove", onPointerMove);
      target.addEventListener("pointerup", onPointerUp);
      target.addEventListener("pointercancel", onPointerUp);
    },
    [],
  );

  return { colWidths, handleResizePointerDown };
}

export function PlainGridList<T>({
  columns,
  data,
  onRowClick,
  rowKey,
  rowClassName,
  className,
  style,
  emptyMessage = "No rows.",
  selectedRowKey,
  onSelectRow,
  onClearRow,
  isLoading = false,
  skeletonRowCount = 12,
  scrollPositionKey,
  selectable = false,
  selectedKeys,
  onSelectionChange,
}: Omit<GridListProps<T>, "gridId">) {
  const { colWidths, handleResizePointerDown } = useColResize();
  const scrollRef = useRef<HTMLDivElement>(null);
  const tableRef = useRef<HTMLTableElement>(null);
  const selectionOverlayRef = useRef<HTMLDivElement>(null);
  const copiedOverlayRef = useRef<HTMLDivElement>(null);

  useScrollRestore(scrollRef, scrollPositionKey, !isLoading);

  // list-wrap(부모)의 높이를 매 렌더마다 직접 읽는다.
  // ResizeObserver 콜백은 비동기라 초기 렌더에서 높이가 0으로 잡히는 문제가 있어 교체.
  const [containerH, setContainerH] = useState(0);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useLayoutEffect(() => {
    const h = scrollRef.current?.parentElement?.clientHeight ?? 0;
    if (h > 0 && h !== containerH) setContainerH(h);
  });

  const rowVirtualizer = useVirtualizer({
    count: data.length,
    getScrollElement: () => scrollRef.current,
    estimateSize: () => ROW_HEIGHT_PX,
    overscan: 30,
    // 실제 DOM 높이로 측정해 추정값과의 누적 오차를 제거한다.
    measureElement: (el) => el?.getBoundingClientRect().height ?? ROW_HEIGHT_PX,
    // index 기반 default key는 행 삭제 시 같은 위치에 다른 row가 들어와
    // uncontrolled input의 DOM value가 stale로 남는 버그를 유발한다.
    getItemKey: (index) => {
      if (rowKey) {
        try { return String(rowKey(data[index], index)); } catch { return index; }
      }
      const idVal = (data[index] as Record<string, unknown> | undefined)?.id;
      return idVal != null ? String(idVal) : index;
    },
  });

  const getRowOffset = useCallback(
    (i: number) => {
      const m = rowVirtualizer.measurementsCache[i];
      return m ? { start: m.start, size: m.size } : null;
    },
    [rowVirtualizer],
  );

  // 호출처 인라인 콜백을 안정적인 참조로 감싸 GridRow의 React.memo 비교를 통과시킨다.
  // rowClassName은 extraClassName으로 직접 계산해 전달하므로 stable 래핑 불필요.
  const stableRowKey = useStableOptionalCallback(rowKey);
  const stableOnRowClick = useStableOptionalCallback(onRowClick);
  const stableOnSelectRow = useStableOptionalCallback(onSelectRow);
  const stableOnClearRow = useStableOptionalCallback(onClearRow);

  // 드래그 중 focus 행이 변경될 때 onRowClick을 통해 외부 selected 상태를 갱신한다.
  const onActiveRowChange = useCallback((rk: string) => {
    if (!stableOnRowClick) return;
    for (let i = 0; i < data.length; i++) {
      const k = String(rowKey ? rowKey(data[i], i) : (data[i] as Record<string, unknown>).id ?? i);
      if (k === rk) { stableOnRowClick(data[i], i); return; }
    }
  }, [data, rowKey, stableOnRowClick]);

  // colWidths가 반영된 컬럼 정의. overlay 계산 시 실제 렌더 너비와 일치시킨다.
  const resolvedColumns = columns.map((col) => {
    const key = String(col.key);
    const w = colWidths[key] ?? col.width ?? col.minWidth;
    return { ...col, width: w };
  });
  const hasFooter = resolvedColumns.some((c) => c.aggregate === "sum");

  // 셀 선택/복사 범위는 체크박스 컬럼을 제외한 데이터 컬럼만 대상으로 한다.
  const { handleTableMouseDown } = useGridCellSelection({
    data,
    rowKey,
    visibleColumns: resolvedColumns,
    getTable: () => tableRef.current,
    overlayRef: selectionOverlayRef as RefObject<HTMLDivElement | null>,
    copiedOverlayRef: copiedOverlayRef as RefObject<HTMLDivElement | null>,
    rowHeight: ROW_HEIGHT_PX,
    getRowOffset,
    onActiveRowChange,
    onClearActiveRow: () => { stableOnClearRow?.(); },
  });

  const virtualRows = rowVirtualizer.getVirtualItems();
  const totalSize = rowVirtualizer.getTotalSize();
  const paddingTop = virtualRows.length > 0 ? virtualRows[0].start : 0;
  const paddingBottom =
    virtualRows.length > 0 ? totalSize - virtualRows[virtualRows.length - 1].end : 0;

  // 전체 선택 체크박스 상태 계산
  const allChecked = selectable && data.length > 0 && selectedKeys != null && selectedKeys.size === data.length;
  const indeterminate = selectable && selectedKeys != null && selectedKeys.size > 0 && selectedKeys.size < data.length;

  function handleHeaderCheckboxChange() {
    if (!onSelectionChange) return;
    if (allChecked) {
      onSelectionChange(new Set());
    } else {
      const next = new Set<string | number>();
      for (let i = 0; i < data.length; i++) {
        const k = rowKey ? rowKey(data[i], i) : ((data[i] as Record<string, unknown>).id as string | number | undefined) ?? i;
        next.add(k);
      }
      onSelectionChange(next);
    }
  }

  // 체크박스 컬럼 포함 시 colSpan 계산
  const totalColSpan = resolvedColumns.length + (selectable ? 1 : 0);

  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} ref={scrollRef} style={{ ...style, overflowY: isLoading ? "hidden" : undefined }}>
      <table ref={tableRef} className={`grid--list${hasFooter ? " grid--list--has-foot" : ""}`} onMouseDown={handleTableMouseDown}>
        <colgroup>
          {selectable && <col style={{ width: 28 }} />}
          {resolvedColumns.map((col) => (
            <col key={String(col.key)} style={{ width: col.width }} />
          ))}
        </colgroup>
        <thead>
          <tr>
            {selectable && (
              <th className="grid__select-cell" style={{ width: 28 }}>
                <input
                  type="checkbox"
                  className="chk"
                  checked={allChecked}
                  ref={(el) => { if (el) el.indeterminate = indeterminate; }}
                  onChange={handleHeaderCheckboxChange}
                />
              </th>
            )}
            {resolvedColumns.map((col) => {
              const key = String(col.key);
              return (
                <th
                  key={key}
                  style={{ width: col.width, textAlign: "center" }}
                  className={col.isRequired ? "is-required" : undefined}
                >
                  {col.label}
                  <span
                    className="grid__resize-handle"
                    onPointerDown={(e) =>
                      handleResizePointerDown(e, key, col.width ?? 80)
                    }
                  />
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {isLoading ? (
            Array.from(
              {
                length: containerH > 0
                  ? Math.ceil(containerH / ROW_HEIGHT_PX) + 10
                  : (skeletonRowCount ?? 20),
              },
              (_, i) => (
                <tr key={`skel-${i}`}>
                  {selectable && <td className="grid__select-cell" />}
                  {resolvedColumns.map((col) => (
                    <td key={String(col.key)}>
                      <div className="h-3 w-full rounded animate-pulse" style={{ backgroundColor: "var(--surface-3)" }} />
                    </td>
                  ))}
                </tr>
              )
            )
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={totalColSpan} className="grid__empty">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            <>
              {paddingTop > 0 && (
                <tr>
                  <td colSpan={totalColSpan} style={{ height: paddingTop, padding: 0 }} />
                </tr>
              )}
              {virtualRows.map((virtualRow) => {
                const ri = virtualRow.index;
                const rk = rowKey ? rowKey(data[ri], ri) : ((data[ri] as Record<string, unknown>).id as string | number | undefined) ?? ri;
                return (
                  <GridRow<T>
                    key={String(virtualRow.key)}
                    row={data[ri]}
                    rowIndex={ri}
                    columns={resolvedColumns}
                    rowKey={stableRowKey}
                    extraClassName={rowClassName?.(data[ri], ri) ?? undefined}
                    onRowClick={stableOnRowClick}
                    selectedRowKey={selectedRowKey}
                    onSelectRow={stableOnSelectRow}
                    measureRef={rowVirtualizer.measureElement}
                    dataIndex={ri}
                    selectable={selectable}
                    selected={selectable && selectedKeys != null ? selectedKeys.has(rk) : undefined}
                    onToggleSelect={selectable && onSelectionChange ? () => {
                      const next = new Set(selectedKeys);
                      if (next.has(rk)) { next.delete(rk); } else { next.add(rk); }
                      onSelectionChange(next);
                    } : undefined}
                  />
                );
              })}
              {paddingBottom > 0 && (
                <tr>
                  <td colSpan={totalColSpan} style={{ height: paddingBottom, padding: 0 }} />
                </tr>
              )}
              {hasFooter && (
                <tr className="grid__filler" aria-hidden="true">
                  <td colSpan={totalColSpan} />
                </tr>
              )}
            </>
          )}
        </tbody>
        {!isLoading && (
          <GridFooter<T>
            columns={resolvedColumns}
            data={data}
            selectable={selectable}
          />
        )}
      </table>
      <div ref={selectionOverlayRef} className="grid-selection-overlay" aria-hidden="true" />
      <div ref={copiedOverlayRef} className="grid-copied-overlay" aria-hidden="true" />
    </div>
  );
}
