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
  /** 이 컬럼을 합계 집계 대상으로 지정. 자동 판별이 불가해 명시적 선언 방식 채택. */
  aggregate?: "sum";
  /** 합계 표시 소수 자리. 기본 0. */
  aggregateDecimals?: number;
  /** footer 합계 시 이 행의 값 접근자. 미지정 시 Number(row[key]). */
  footerValue?: (row: T) => number;
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
  /** footer 합계에 포함할 행 필터. 미지정 시 전체 data 합산. */
  footerSumFilter?: (row: T) => boolean;
}

// --row-h 기본값 24px — tokens.css:21 정의 기준
export const ROW_HEIGHT_PX = 24;

export function GridList<T>({ gridId, scrollPositionKey, ...rest }: GridListProps<T>) {
  if (gridId) return <ManagedGridList<T> gridId={gridId} scrollPositionKey={scrollPositionKey} {...rest} />;
  return <PlainGridList<T> scrollPositionKey={scrollPositionKey} {...rest} />;
}
