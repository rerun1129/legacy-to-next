"use client";

import type { AggregateDimension } from "@/lib/grid-aggregate";
import type { PmsPerformanceRow } from "@/application/pms/performance/ports";
import { PmsPerformanceAggregateDimPicker } from "./pms-performance-aggregate-dim-picker";

export type PmsViewMode = "detail" | "aggregate";

interface ToggleProps {
  viewMode: PmsViewMode;
  onViewModeChange: (mode: PmsViewMode) => void;
}

interface SubBarProps {
  catalog: AggregateDimension<PmsPerformanceRow>[];
  dimKeys: string[];
  onDimKeysChange: (keys: string[]) => void;
}

export type PmsAggregateToolbarProps = ToggleProps & SubBarProps;

/**
 * [상세]/[집계] 토글 세그먼트.
 * panel__head 내부 우측에 배치(marginLeft:auto).
 */
export function PmsViewToggle({ viewMode, onViewModeChange }: ToggleProps) {
  return (
    <div className="seg-view" style={{ marginLeft: "auto" }}>
      <button
        type="button"
        className={viewMode === "detail" ? "is-active" : undefined}
        onClick={() => onViewModeChange("detail")}
      >
        상세
      </button>
      <button
        type="button"
        className={viewMode === "aggregate" ? "is-active" : undefined}
        onClick={() => onViewModeChange("aggregate")}
      >
        집계
      </button>
    </div>
  );
}

/**
 * 집계 모드 전용 dim 피커 서브툴바.
 * panel__head 하단 별도 행. viewMode=aggregate일 때만 렌더.
 */
export function PmsAggregateDimBar({ catalog, dimKeys, onDimKeysChange }: SubBarProps) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: "8px",
        padding: "4px 8px",
        borderBottom: "1px solid var(--divider)",
        background: "var(--surface-2)",
        flexShrink: 0,
      }}
    >
      <span style={{ fontSize: "11px", color: "var(--ink-4)", whiteSpace: "nowrap" }}>
        그룹 기준
      </span>
      <PmsPerformanceAggregateDimPicker
        catalog={catalog}
        value={dimKeys}
        onChange={onDimKeysChange}
      />
    </div>
  );
}
