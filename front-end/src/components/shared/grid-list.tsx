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
  /**
   * 전체 조회 행 합계(컬럼 key → 합계). 미전달 시 현재 data로 계산(FE-only).
   * 향후 BE 집계 API 연동 시 주입하여 페이지 범위를 넘는 정확한 전체 합계를 표시한다.
   */
  totalAggregates?: Record<string, number>;
}

// --row-h 기본값 24px — tokens.css:21 정의 기준
export const ROW_HEIGHT_PX = 24;

export function GridList<T>({ gridId, scrollPositionKey, ...rest }: GridListProps<T>) {
  if (gridId) return <ManagedGridList<T> gridId={gridId} scrollPositionKey={scrollPositionKey} {...rest} />;
  return <PlainGridList<T> scrollPositionKey={scrollPositionKey} {...rest} />;
}
