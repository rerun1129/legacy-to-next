import { describe, it, expect } from "vitest";
import {
  distinctDisplayValues,
  applyColumnFilters,
  type ColumnFilterState,
} from "../grid-column-filter";

// ── 고정 Fixture ─────────────────────────────────────────────

interface Row {
  team: string;
  carrier: string;
  pol: string;
}

const ROWS: Row[] = [
  { team: "ALPHA", carrier: "MSC",    pol: "PUS" },
  { team: "BETA",  carrier: "MAERSK", pol: "INC" },
  { team: "ALPHA", carrier: "MAERSK", pol: "PUS" },
  { team: "BETA",  carrier: "MSC",    pol: "GMP" },
];

const TEAM_ACCESSOR = (r: Row) => r.team;
const CARRIER_ACCESSOR = (r: Row) => r.carrier;
const POL_ACCESSOR = (r: Row) => r.pol;

const ACCESSORS: Record<string, (r: Row) => string> = {
  team: TEAM_ACCESSOR,
  carrier: CARRIER_ACCESSOR,
  pol: POL_ACCESSOR,
};

// ── distinctDisplayValues ─────────────────────────────────────

describe("distinctDisplayValues", () => {
  it("중복 제거 후 오름차순 정렬", () => {
    const result = distinctDisplayValues(ROWS, TEAM_ACCESSOR);
    expect(result).toEqual(["ALPHA", "BETA"]);
  });

  it("결정적 — 입력 순서가 달라도 동일 출력", () => {
    const shuffled: Row[] = [
      { team: "BETA",  carrier: "MSC",    pol: "GMP" },
      { team: "ALPHA", carrier: "MSC",    pol: "PUS" },
      { team: "BETA",  carrier: "MAERSK", pol: "INC" },
      { team: "ALPHA", carrier: "MAERSK", pol: "PUS" },
    ];
    const r1 = distinctDisplayValues(ROWS,     TEAM_ACCESSOR);
    const r2 = distinctDisplayValues(shuffled, TEAM_ACCESSOR);
    expect(r1).toEqual(r2);
  });

  it("빈 문자열은 오름차순 정렬에서 항상 맨 끝", () => {
    const rowsWithEmpty: Row[] = [
      { team: "",      carrier: "MSC",    pol: "PUS" },
      { team: "ZEBRA", carrier: "MSC",    pol: "PUS" },
      { team: "ALPHA", carrier: "MSC",    pol: "PUS" },
    ];
    const result = distinctDisplayValues(rowsWithEmpty, TEAM_ACCESSOR);
    expect(result).toEqual(["ALPHA", "ZEBRA", ""]);
  });

  it("단일 값만 있을 때", () => {
    const single: Row[] = [{ team: "ONLY", carrier: "A", pol: "A" }];
    expect(distinctDisplayValues(single, TEAM_ACCESSOR)).toEqual(["ONLY"]);
  });

  it("빈 rows → 빈 배열", () => {
    expect(distinctDisplayValues([], TEAM_ACCESSOR)).toEqual([]);
  });

  it("모두 동일한 값 → 단일 요소 배열", () => {
    const sameTeam: Row[] = ROWS.map((r) => ({ ...r, team: "SAME" }));
    expect(distinctDisplayValues(sameTeam, TEAM_ACCESSOR)).toEqual(["SAME"]);
  });
});

// ── applyColumnFilters ────────────────────────────────────────

describe("applyColumnFilters", () => {
  it("state 비어있으면 전체 rows 반환", () => {
    const state: ColumnFilterState = {};
    expect(applyColumnFilters(ROWS, ACCESSORS, state)).toEqual(ROWS);
  });

  it("단일 컬럼 필터 — 선택값과 일치하는 행만 통과", () => {
    const state: ColumnFilterState = { team: new Set(["ALPHA"]) };
    const result = applyColumnFilters(ROWS, ACCESSORS, state);
    expect(result).toHaveLength(2);
    expect(result.every((r) => r.team === "ALPHA")).toBe(true);
  });

  it("다중 컬럼 AND — 두 조건 모두 만족해야 통과", () => {
    const state: ColumnFilterState = {
      team:    new Set(["ALPHA"]),
      carrier: new Set(["MAERSK"]),
    };
    const result = applyColumnFilters(ROWS, ACCESSORS, state);
    // ALPHA+MAERSK 행만: { team:"ALPHA", carrier:"MAERSK", pol:"PUS" }
    expect(result).toHaveLength(1);
    expect(result[0]).toEqual({ team: "ALPHA", carrier: "MAERSK", pol: "PUS" });
  });

  it("state 키가 accessors에 없으면 무시", () => {
    const state: ColumnFilterState = {
      nonExistentKey: new Set(["WHATEVER"]),
    };
    // 모든 행 통과
    expect(applyColumnFilters(ROWS, ACCESSORS, state)).toEqual(ROWS);
  });

  it("선택 Set이 비어있으면 해당 컬럼에서 아무 행도 통과 못함", () => {
    const state: ColumnFilterState = { team: new Set<string>() };
    expect(applyColumnFilters(ROWS, ACCESSORS, state)).toEqual([]);
  });

  it("빈 rows → 빈 배열", () => {
    const state: ColumnFilterState = { team: new Set(["ALPHA"]) };
    expect(applyColumnFilters([], ACCESSORS, state)).toEqual([]);
  });

  it("결정적 — 동일 입력 두 번 호출 시 동일 결과", () => {
    const state: ColumnFilterState = { carrier: new Set(["MAERSK"]) };
    const r1 = applyColumnFilters(ROWS, ACCESSORS, state);
    const r2 = applyColumnFilters(ROWS, ACCESSORS, state);
    expect(r1).toEqual(r2);
  });

  it("원본 rows 배열을 변이하지 않음", () => {
    const original = [...ROWS];
    const state: ColumnFilterState = { team: new Set(["ALPHA"]) };
    applyColumnFilters(ROWS, ACCESSORS, state);
    expect(ROWS).toEqual(original);
  });

  it("3컬럼 AND — 교집합이 0인 경우 빈 배열", () => {
    const state: ColumnFilterState = {
      team:    new Set(["ALPHA"]),
      carrier: new Set(["MSC"]),
      pol:     new Set(["GMP"]),
    };
    // ALPHA+MSC+GMP인 행은 없음
    expect(applyColumnFilters(ROWS, ACCESSORS, state)).toEqual([]);
  });

  it("다중 선택값 OR within 컬럼 — ALPHA or BETA 모두 통과", () => {
    const state: ColumnFilterState = {
      team: new Set(["ALPHA", "BETA"]),
    };
    expect(applyColumnFilters(ROWS, ACCESSORS, state)).toEqual(ROWS);
  });
});
