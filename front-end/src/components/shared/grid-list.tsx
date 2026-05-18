"use client";

import React from "react";
import { ManagedGridList } from "./grid-list-managed";
import { PlainGridList } from "./grid-list-plain";

export interface GridColumn<T> {
  key: keyof T | string;
  label: string;
  minWidth?: number;
  width?: number;
  align?: "left" | "center" | "right";
  isRequired?: boolean;
  className?: string;
  render?: (value: unknown, row: T, index: number) => React.ReactNode;
}

export interface GridListProps<T> {
  columns: GridColumn<T>[];
  data: T[];
  onRowClick?: (row: T, index: number) => void;
  rowKey?: (row: T, index: number) => string | number;
  rowClassName?: (row: T, index: number) => string | undefined;
  className?: string;
  style?: React.CSSProperties;
  emptyMessage?: string;
  gridId?: string;
  selectedRowKey?: string | number | null;
  onSelectRow?: (row: T | null, index: number | null) => void;
  onClearRow?: () => void;
  isLoading?: boolean;
  skeletonRowCount?: number;
  scrollPositionKey?: string;
  /** true이면 각 행 앞에 체크박스 컬럼을 렌더한다. 미지정 시 기존 동작 유지. */
  selectable?: boolean;
  /** 현재 선택된 row key Set. selectable=true일 때 유효. */
  selectedKeys?: ReadonlySet<string | number>;
  /** 선택 변경 콜백. 새 Set을 인자로 전달한다. selectable=true일 때 유효. */
  onSelectionChange?: (next: Set<string | number>) => void;
}

// --row-h 기본값 24px — tokens.css:21 정의 기준
export const ROW_HEIGHT_PX = 24;

export function GridList<T>({ gridId, scrollPositionKey, ...rest }: GridListProps<T>) {
  if (gridId) return <ManagedGridList<T> gridId={gridId} scrollPositionKey={scrollPositionKey} {...rest} />;
  return <PlainGridList<T> scrollPositionKey={scrollPositionKey} {...rest} />;
}
