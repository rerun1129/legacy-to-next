import { describe, it, expect } from "vitest";
import {
  buildAggregateRows,
  buildAggregateOutline,
  collapseOutline,
  type AggregateDimension,
  type AggregateMeasure,
} from "../grid-aggregate";

// ── 고정 Fixture ─────────────────────────────────────────────

interface Row {
  team: string;
  carrier: string;
  amt: number;
  qty: number;
}

const DIMS_0: AggregateDimension<Row>[] = [];
const DIMS_TEAM: AggregateDimension<Row>[] = [
  { key: "team", label: "Team", bucketKey: (r) => r.team, display: (r) => r.team },
];
const DIMS_TEAM_CARRIER: AggregateDimension<Row>[] = [
  { key: "team",    label: "Team",    bucketKey: (r) => r.team,    display: (r) => r.team },
  { key: "carrier", label: "Carrier", bucketKey: (r) => r.carrier, display: (r) => r.carrier },
];

const MEASURES: AggregateMeasure<Row>[] = [
  { key: "amt", field: "amt", decimals: 2 },
  { key: "qty", field: "qty", decimals: 0 },
];

const ROWS: Row[] = [
  { team: "A", carrier: "X", amt: 100, qty: 2 },
  { team: "A", carrier: "Y", amt: 200, qty: 3 },
  { team: "B", carrier: "X", amt: 300, qty: 4 },
  { team: "B", carrier: "Y", amt: 400, qty: 5 },
];

// ── 불변식 헬퍼: Σleaf.sums === total.sums ────────────────────

function assertLeafSumMatchTotal(rows: ReturnType<typeof buildAggregateRows>): void {
  const total = rows.find((r) => r.kind === "total");
  if (!total) return;

  const leaves = rows.filter((r) => r.kind === "leaf");
  const leafKeys = Object.keys(total.sums);

  for (const key of leafKeys) {
    const leafSum = leaves.reduce((acc, l) => acc + l.sums[key], 0);
    // 부동소수 오차는 소수점 9자리 반올림으로 흡수
    const rounded = (n: number) => Math.round(n * 1e9) / 1e9;
    expect(rounded(leafSum)).toBe(rounded(total.sums[key]));
  }

  // total.count === rows.length
  expect(total.count).toBe(leaves.reduce((s, l) => s + l.count, 0));
}

// ── 각 subtotal의 sums === 소속 leaf.sums 합 ─────────────────

function assertSubtotalMatchLeaves(result: ReturnType<typeof buildAggregateRows>): void {
  const subtotals = result.filter((r) => r.kind === "subtotal");
  for (const sub of subtotals) {
    const myLeaves = result.filter(
      (r) =>
        r.kind === "leaf" &&
        sub.path.every((p, i) => r.path[i] === p),
    );
    for (const key of Object.keys(sub.sums)) {
      const leafSum = myLeaves.reduce((acc, l) => acc + l.sums[key], 0);
      const rounded = (n: number) => Math.round(n * 1e9) / 1e9;
      expect(rounded(sub.sums[key])).toBe(rounded(leafSum));
    }
  }
}

// ── 테스트 ────────────────────────────────────────────────────

describe("buildAggregateRows", () => {
  describe("0 dim", () => {
    it("dims 0개이면 [] 반환", () => {
      expect(buildAggregateRows(ROWS, DIMS_0, MEASURES)).toEqual([]);
    });
  });

  describe("빈 rows", () => {
    it("rows 0개이면 [] 반환", () => {
      expect(buildAggregateRows([], DIMS_TEAM, MEASURES)).toEqual([]);
    });
  });

  describe("1 dim", () => {
    it("leaf + total만 있고 subtotal 없음", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM, MEASURES);
      const kinds = result.map((r) => r.kind);
      expect(kinds).not.toContain("subtotal");
      expect(kinds.at(-1)).toBe("total");
      expect(kinds.filter((k) => k === "leaf").length).toBe(2); // A, B
    });

    it("leaf가 버킷키 오름차순으로 정렬됨", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM, MEASURES);
      const leaves = result.filter((r) => r.kind === "leaf");
      expect(leaves[0].path).toEqual(["A"]);
      expect(leaves[1].path).toEqual(["B"]);
    });

    it("total.count === 전체 행 수", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM, MEASURES);
      const total = result.find((r) => r.kind === "total")!;
      expect(total.count).toBe(ROWS.length);
    });

    it("불변식: Σleaf.sums === total.sums", () => {
      assertLeafSumMatchTotal(buildAggregateRows(ROWS, DIMS_TEAM, MEASURES));
    });

    it("leaf.sums는 해당 버킷 행 합계", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM, MEASURES);
      const leafA = result.find((r) => r.kind === "leaf" && r.path[0] === "A")!;
      expect(leafA.sums["amt"]).toBe(300); // 100 + 200
      expect(leafA.sums["qty"]).toBe(5);   // 2 + 3
    });

    it("결정적 rowKey — id 형식 검증", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM, MEASURES);
      const total = result.find((r) => r.kind === "total")!;
      expect(total.id).toBe("total");
      const leafA = result.find((r) => r.kind === "leaf" && r.path[0] === "A")!;
      expect(leafA.id).toBe("leaf:A");
    });
  });

  describe("2 dim 계층", () => {
    it("leaf → subtotal → total 순서", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const last = result.at(-1)!;
      expect(last.kind).toBe("total");
    });

    it("subtotal이 dim >= 2일 때 발생", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const subtotals = result.filter((r) => r.kind === "subtotal");
      expect(subtotals.length).toBeGreaterThan(0);
    });

    it("subtotal.level은 소계 내는 dim 깊이(0-based)", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const subtotals = result.filter((r) => r.kind === "subtotal");
      // team(depth=0)이 소계를 냄 → level=0
      for (const s of subtotals) {
        expect(s.level).toBe(0);
      }
    });

    it("2 dim: 각 subtotal.sums === 소속 leaf.sums 합 (불변식)", () => {
      assertSubtotalMatchLeaves(buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES));
    });

    it("불변식: Σleaf.sums === total.sums", () => {
      assertLeafSumMatchTotal(buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES));
    });

    it("DFS 순서: TeamA 하위 leaf들 → TeamA subtotal → TeamB 하위 leaf들 → TeamB subtotal → total", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const kinds = result.map((r) => r.kind);
      // leaf X, leaf Y, subtotal A, leaf X, leaf Y, subtotal B, total
      expect(kinds).toEqual([
        "leaf", "leaf", "subtotal",
        "leaf", "leaf", "subtotal",
        "total",
      ]);
    });

    it("subtotal.labels에 조상 라벨이 미리 계산됨", () => {
      const result = buildAggregateRows(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const subA = result.find((r) => r.kind === "subtotal" && r.path[0] === "A")!;
      expect(subA.labels[0]).toBe("A");
    });
  });

  describe("빈 버킷 정렬", () => {
    it("빈 버킷('')은 오름차순 정렬에서 항상 맨 끝", () => {
      const rowsWithEmpty: Row[] = [
        { team: "",  carrier: "X", amt: 50, qty: 1 },
        { team: "Z", carrier: "X", amt: 50, qty: 1 },
        { team: "A", carrier: "X", amt: 50, qty: 1 },
      ];
      const result = buildAggregateRows(rowsWithEmpty, DIMS_TEAM, MEASURES);
      const leaves = result.filter((r) => r.kind === "leaf");
      expect(leaves[0].path[0]).toBe("A");
      expect(leaves[1].path[0]).toBe("Z");
      expect(leaves[2].path[0]).toBe("");
    });

    it("빈 버킷 표시 라벨은 '(미지정)'", () => {
      const rowsWithEmpty: Row[] = [
        { team: "", carrier: "X", amt: 50, qty: 1 },
      ];
      const result = buildAggregateRows(rowsWithEmpty, DIMS_TEAM, MEASURES);
      const leaf = result.find((r) => r.kind === "leaf")!;
      expect(leaf.labels[0]).toBe("(미지정)");
    });
  });

  describe("결정성 — 입력 순서 무관", () => {
    it("행 순서가 달라도 동일 출력", () => {
      const shuffled: Row[] = [
        { team: "B", carrier: "Y", amt: 400, qty: 5 },
        { team: "A", carrier: "X", amt: 100, qty: 2 },
        { team: "B", carrier: "X", amt: 300, qty: 4 },
        { team: "A", carrier: "Y", amt: 200, qty: 3 },
      ];
      const r1 = buildAggregateRows(ROWS,     DIMS_TEAM, MEASURES);
      const r2 = buildAggregateRows(shuffled, DIMS_TEAM, MEASURES);
      expect(r1.map((r) => r.id)).toEqual(r2.map((r) => r.id));
      expect(r1.map((r) => r.sums)).toEqual(r2.map((r) => r.sums));
    });
  });
});

// ── buildAggregateOutline ──────────────────────────────────────

describe("buildAggregateOutline", () => {
  // 3 dim용 fixture
  const DIMS_TEAM_CARRIER_POL: AggregateDimension<Row & { pol: string }>[] = [
    { key: "team",    label: "Team",    bucketKey: (r) => r.team,    display: (r) => r.team },
    { key: "carrier", label: "Carrier", bucketKey: (r) => r.carrier, display: (r) => r.carrier },
    { key: "pol",     label: "POL",     bucketKey: (r) => r.pol,     display: (r) => r.pol },
  ];
  const ROWS_3D: (Row & { pol: string })[] = [
    { team: "A", carrier: "MAERSK", pol: "PUS", amt: 100, qty: 1 },
    { team: "A", carrier: "MAERSK", pol: "INC", amt: 200, qty: 2 },
    { team: "A", carrier: "MSC",    pol: "PUS", amt: 300, qty: 3 },
    { team: "B", carrier: "MAERSK", pol: "PUS", amt: 400, qty: 4 },
  ];

  describe("0 dim", () => {
    it("dims 0개이면 [] 반환", () => {
      expect(buildAggregateOutline(ROWS, DIMS_0, MEASURES)).toEqual([]);
    });
  });

  describe("빈 rows", () => {
    it("rows 0개이면 [] 반환", () => {
      expect(buildAggregateOutline([], DIMS_TEAM, MEASURES)).toEqual([]);
    });
  });

  describe("1 dim — leaf만, group 없음", () => {
    it("kind가 leaf만 있음(group/total 없음)", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM, MEASURES);
      const kinds = result.map((r) => r.kind);
      expect(kinds.every((k) => k === "leaf")).toBe(true);
      expect(kinds).not.toContain("group");
      expect(kinds).not.toContain("total");
    });

    it("버킷키 오름차순 정렬", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM, MEASURES);
      expect(result[0].path).toEqual(["A"]);
      expect(result[1].path).toEqual(["B"]);
    });

    it("leaf.sums는 해당 버킷 행 합계", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM, MEASURES);
      const leafA = result.find((r) => r.path[0] === "A")!;
      expect(leafA.sums["amt"]).toBe(300); // 100 + 200
      expect(leafA.sums["qty"]).toBe(5);   // 2 + 3
    });

    it("leaf.count는 해당 버킷 행 수", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM, MEASURES);
      const leafA = result.find((r) => r.path[0] === "A")!;
      expect(leafA.count).toBe(2);
    });
  });

  describe("2 dim — pre-order(그룹 헤더 → 자식)", () => {
    it("group → leaf 순서(pre-order): group이 자식 leaf보다 먼저", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const kinds = result.map((r) => r.kind);
      // 2 dim: group(TeamA) → leaf(X) → leaf(Y) → group(TeamB) → leaf(X) → leaf(Y)
      expect(kinds).toEqual([
        "group", "leaf", "leaf",
        "group", "leaf", "leaf",
      ]);
    });

    it("그룹 헤더 level=0, leaf level=1", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const groups = result.filter((r) => r.kind === "group");
      const leaves = result.filter((r) => r.kind === "leaf");
      expect(groups.every((r) => r.level === 0)).toBe(true);
      expect(leaves.every((r) => r.level === 1)).toBe(true);
    });

    it("그룹 헤더 rollup sums === 소속 leaf sums 합", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const groupA = result.find((r) => r.kind === "group" && r.path[0] === "A")!;
      const leafAX = result.find((r) => r.kind === "leaf" && r.path[0] === "A" && r.path[1] === "X")!;
      const leafAY = result.find((r) => r.kind === "leaf" && r.path[0] === "A" && r.path[1] === "Y")!;
      expect(groupA.sums["amt"]).toBe(leafAX.sums["amt"] + leafAY.sums["amt"]);
      expect(groupA.count).toBe(leafAX.count + leafAY.count);
    });

    it("그룹 헤더 id 형식 = group:path", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const groupA = result.find((r) => r.kind === "group" && r.path[0] === "A")!;
      expect(groupA.id).toBe("group:A");
    });

    it("leaf id 형식 = leaf:path(›구분)", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      const leafAX = result.find((r) => r.kind === "leaf" && r.path[0] === "A" && r.path[1] === "X")!;
      expect(leafAX.id).toBe("leaf:A›X");
    });

    it("total 행 없음", () => {
      const result = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);
      expect(result.find((r) => r.kind === "total")).toBeUndefined();
    });
  });

  describe("3 dim — 중첩 pre-order", () => {
    it("pre-order 순서: group0→group1→leaf 중첩", () => {
      const result = buildAggregateOutline(ROWS_3D, DIMS_TEAM_CARRIER_POL, MEASURES);
      const kinds = result.map((r) => r.kind);
      // TeamA(group0) → MAERSK(group1) → PUS(leaf) → INC(leaf) → MSC(group1) → PUS(leaf)
      // → TeamB(group0) → MAERSK(group1) → PUS(leaf)
      expect(kinds).toEqual([
        "group", "group", "leaf", "leaf",
        "group", "leaf",
        "group", "group", "leaf",
      ]);
    });

    it("level 값: depth 0=group0, depth 1=group1, depth 2=leaf", () => {
      const result = buildAggregateOutline(ROWS_3D, DIMS_TEAM_CARRIER_POL, MEASURES);
      const groupTeamA = result.find((r) => r.kind === "group" && r.path.length === 1)!;
      const groupMaersk = result.find((r) => r.kind === "group" && r.path.length === 2)!;
      const leafPus = result.find((r) => r.kind === "leaf")!;
      expect(groupTeamA.level).toBe(0);
      expect(groupMaersk.level).toBe(1);
      expect(leafPus.level).toBe(2);
    });

    it("group0 rollup sums === 소속 leaf sums 합", () => {
      const result = buildAggregateOutline(ROWS_3D, DIMS_TEAM_CARRIER_POL, MEASURES);
      const groupA = result.find((r) => r.kind === "group" && r.path[0] === "A" && r.path.length === 1)!;
      const leavesA = result.filter((r) => r.kind === "leaf" && r.path[0] === "A");
      const leafAmtSum = leavesA.reduce((acc, l) => acc + l.sums["amt"], 0);
      expect(groupA.sums["amt"]).toBe(leafAmtSum);
    });
  });

  describe("빈 버킷 정렬", () => {
    it("빈 버킷('')은 오름차순 정렬에서 항상 맨 끝", () => {
      const rowsWithEmpty: Row[] = [
        { team: "",  carrier: "X", amt: 50, qty: 1 },
        { team: "Z", carrier: "X", amt: 50, qty: 1 },
        { team: "A", carrier: "X", amt: 50, qty: 1 },
      ];
      const result = buildAggregateOutline(rowsWithEmpty, DIMS_TEAM, MEASURES);
      expect(result[0].path[0]).toBe("A");
      expect(result[1].path[0]).toBe("Z");
      expect(result[2].path[0]).toBe("");
    });

    it("빈 버킷 표시 라벨은 '(미지정)'", () => {
      const rowsWithEmpty: Row[] = [
        { team: "", carrier: "X", amt: 50, qty: 1 },
      ];
      const result = buildAggregateOutline(rowsWithEmpty, DIMS_TEAM, MEASURES);
      const leaf = result.find((r) => r.kind === "leaf")!;
      expect(leaf.labels[0]).toBe("(미지정)");
    });
  });

  describe("결정성 — 입력 순서 무관", () => {
    it("행 순서가 달라도 동일 출력(id 순서·sums)", () => {
      const shuffled: Row[] = [
        { team: "B", carrier: "Y", amt: 400, qty: 5 },
        { team: "A", carrier: "X", amt: 100, qty: 2 },
        { team: "B", carrier: "X", amt: 300, qty: 4 },
        { team: "A", carrier: "Y", amt: 200, qty: 3 },
      ];
      const r1 = buildAggregateOutline(ROWS,     DIMS_TEAM_CARRIER, MEASURES);
      const r2 = buildAggregateOutline(shuffled, DIMS_TEAM_CARRIER, MEASURES);
      expect(r1.map((r) => r.id)).toEqual(r2.map((r) => r.id));
      expect(r1.map((r) => r.sums)).toEqual(r2.map((r) => r.sums));
    });
  });
});

// ── collapseOutline ──────────────────────────────────────────

describe("collapseOutline", () => {
  // 2 dim fixture: group:A → leaf:A›X → leaf:A›Y → group:B → leaf:B›X → leaf:B›Y
  const OUTLINE_2D = buildAggregateOutline(ROWS, DIMS_TEAM_CARRIER, MEASURES);

  describe("빈 collapsedIds → 원본 그대로", () => {
    it("Set이 비어있으면 모든 행 반환", () => {
      const result = collapseOutline(OUTLINE_2D, new Set());
      expect(result.map((r) => r.id)).toEqual(OUTLINE_2D.map((r) => r.id));
    });
  });

  describe("특정 group id 접힘 → 그 자손만 제거", () => {
    it("group:A 접으면 leaf:A›X·leaf:A›Y 제거, group:B와 하위는 유지", () => {
      const result = collapseOutline(OUTLINE_2D, new Set(["group:A"]));
      const ids = result.map((r) => r.id);
      expect(ids).toContain("group:A");
      expect(ids).not.toContain("leaf:A›X");
      expect(ids).not.toContain("leaf:A›Y");
      expect(ids).toContain("group:B");
      expect(ids).toContain("leaf:B›X");
      expect(ids).toContain("leaf:B›Y");
    });

    it("형제 그룹은 유지 — group:B만 접으면 A 하위 leaf는 그대로", () => {
      const result = collapseOutline(OUTLINE_2D, new Set(["group:B"]));
      const ids = result.map((r) => r.id);
      expect(ids).toContain("leaf:A›X");
      expect(ids).toContain("leaf:A›Y");
      expect(ids).toContain("group:B");
      expect(ids).not.toContain("leaf:B›X");
      expect(ids).not.toContain("leaf:B›Y");
    });
  });

  describe("중첩 접힘 — 3 dim", () => {
    const DIMS_TEAM_CARRIER_POL: AggregateDimension<Row & { pol: string }>[] = [
      { key: "team",    label: "Team",    bucketKey: (r) => r.team,    display: (r) => r.team },
      { key: "carrier", label: "Carrier", bucketKey: (r) => r.carrier, display: (r) => r.carrier },
      { key: "pol",     label: "POL",     bucketKey: (r) => r.pol,     display: (r) => r.pol },
    ];
    const ROWS_3D: (Row & { pol: string })[] = [
      { team: "A", carrier: "MAERSK", pol: "PUS", amt: 100, qty: 1 },
      { team: "A", carrier: "MAERSK", pol: "INC", amt: 200, qty: 2 },
      { team: "A", carrier: "MSC",    pol: "PUS", amt: 300, qty: 3 },
      { team: "B", carrier: "MAERSK", pol: "PUS", amt: 400, qty: 4 },
    ];
    const OUTLINE_3D = buildAggregateOutline(ROWS_3D, DIMS_TEAM_CARRIER_POL, MEASURES);

    it("group:A 접으면 그 모든 자손(group:A›MAERSK, leaf:A›…, group:A›MSC) 제거", () => {
      const result = collapseOutline(OUTLINE_3D, new Set(["group:A"]));
      const ids = result.map((r) => r.id);
      expect(ids).toContain("group:A");
      expect(ids).not.toContain("group:A›MAERSK");
      expect(ids).not.toContain("leaf:A›MAERSK›PUS");
      expect(ids).not.toContain("leaf:A›MAERSK›INC");
      expect(ids).not.toContain("group:A›MSC");
      expect(ids).not.toContain("leaf:A›MSC›PUS");
      // B 계열 유지
      expect(ids).toContain("group:B");
      expect(ids).toContain("leaf:B›MAERSK›PUS");
    });

    it("group:A›MAERSK 접으면 그 leaf만 제거, group:A›MSC와 그 leaf 유지", () => {
      const result = collapseOutline(OUTLINE_3D, new Set(["group:A›MAERSK"]));
      const ids = result.map((r) => r.id);
      expect(ids).toContain("group:A");
      expect(ids).toContain("group:A›MAERSK");
      expect(ids).not.toContain("leaf:A›MAERSK›PUS");
      expect(ids).not.toContain("leaf:A›MAERSK›INC");
      expect(ids).toContain("group:A›MSC");
      expect(ids).toContain("leaf:A›MSC›PUS");
    });
  });

  describe("level0 모두 접힘 → 최상위 그룹만", () => {
    it("level0 group id 전부 접으면 group 헤더만 남음", () => {
      const topGroupIds = OUTLINE_2D
        .filter((r) => r.kind === "group" && r.level === 0)
        .map((r) => r.id);
      const result = collapseOutline(OUTLINE_2D, new Set(topGroupIds));
      const ids = result.map((r) => r.id);
      expect(ids).toEqual(topGroupIds);
    });
  });

  describe("결정성", () => {
    it("동일 입력이면 항상 동일 출력", () => {
      const collapsed = new Set(["group:A"]);
      const r1 = collapseOutline(OUTLINE_2D, collapsed);
      const r2 = collapseOutline(OUTLINE_2D, collapsed);
      expect(r1.map((r) => r.id)).toEqual(r2.map((r) => r.id));
    });

    it("입력 rows를 변이하지 않음 — 원본 배열 길이 불변", () => {
      const before = OUTLINE_2D.length;
      collapseOutline(OUTLINE_2D, new Set(["group:A"]));
      expect(OUTLINE_2D.length).toBe(before);
    });
  });
});
