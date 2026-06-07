"use client";

import { useState } from "react";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { AggregateDimension } from "@/lib/grid-aggregate";
import type { PmsPerformanceRow } from "@/application/pms/performance/ports";

interface Props {
  catalog: AggregateDimension<PmsPerformanceRow>[];
  value: string[];
  onChange: (keys: string[]) => void;
}

/**
 * 순서형 chip 피커.
 * 미선택 dim만 ComboBox 옵션으로 제공 → 선택 시 끝에 append.
 * 선택 dim을 .pill chip 나열, 각 chip에 제거(×)와 ▲▼ reorder.
 */
export function PmsPerformanceAggregateDimPicker({ catalog, value, onChange }: Props) {
  const [addValue, setAddValue] = useState("");

  const selectedDims = value
    .map((key) => catalog.find((d) => d.key === key))
    .filter((d): d is AggregateDimension<PmsPerformanceRow> => d !== undefined);

  const unselectedOptions = catalog
    .filter((d) => !value.includes(d.key))
    .map((d) => ({ value: d.key, label: d.label }));

  const handleAdd = (e: React.ChangeEvent<HTMLInputElement>) => {
    const key = e.target.value;
    if (!key) return;
    if (!value.includes(key)) {
      onChange([...value, key]);
    }
    setAddValue("");
  };

  const handleRemove = (key: string) => {
    onChange(value.filter((k) => k !== key));
  };

  const handleMoveUp = (idx: number) => {
    if (idx === 0) return;
    const next = [...value];
    [next[idx - 1], next[idx]] = [next[idx], next[idx - 1]];
    onChange(next);
  };

  const handleMoveDown = (idx: number) => {
    if (idx === value.length - 1) return;
    const next = [...value];
    [next[idx], next[idx + 1]] = [next[idx + 1], next[idx]];
    onChange(next);
  };

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "6px", flexWrap: "wrap" }}>
      {selectedDims.map((dim, idx) => (
        <span key={dim.key} className="pill" style={{ gap: "2px" }}>
          <span style={{ fontSize: "10px", color: "var(--ink-4)", marginRight: "1px" }}>{idx + 1}.</span>
          {dim.label}
          <button
            type="button"
            onClick={() => handleMoveUp(idx)}
            disabled={idx === 0}
            style={reorderBtnStyle(idx === 0)}
            title="위로"
          >
            ▲
          </button>
          <button
            type="button"
            onClick={() => handleMoveDown(idx)}
            disabled={idx === value.length - 1}
            style={reorderBtnStyle(idx === value.length - 1)}
            title="아래로"
          >
            ▼
          </button>
          <button
            type="button"
            onClick={() => handleRemove(dim.key)}
            style={{ ...reorderBtnStyle(false), marginLeft: "2px", color: "var(--ink-3)" }}
            title="제거"
          >
            ×
          </button>
        </span>
      ))}
      {unselectedOptions.length > 0 && (
        <div style={{ width: "130px", flexShrink: 0 }}>
          <ComboBox
            options={unselectedOptions}
            value={addValue}
            onChange={handleAdd}
            name="pms-agg-dim-add"
            placeholder="+ 그룹 추가"
            variant="panel"
          />
        </div>
      )}
    </div>
  );
}

function reorderBtnStyle(disabled: boolean): React.CSSProperties {
  return {
    background: "transparent",
    border: "none",
    padding: "0 1px",
    cursor: disabled ? "default" : "pointer",
    fontSize: "9px",
    color: disabled ? "var(--ink-4)" : "var(--ink-2)",
    lineHeight: 1,
  };
}
