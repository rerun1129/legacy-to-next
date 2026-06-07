/**
 * 순수·결정적 제네릭 집계 엔진.
 * React·도메인 무의존. 동일 입력 → 동일 출력.
 */

export type AggregateRowKind = "leaf" | "subtotal" | "total" | "group";

export interface AggregateDimension<T> {
  key: string;
  label: string;
  bucketKey: (r: T) => string;
  display: (r: T) => string;
}

export interface AggregateMeasure<T> {
  key: string;
  field: keyof T;
  decimals: number;
}

export interface AggregateRow {
  kind: AggregateRowKind;
  /** subtotal=소계 내는 dim 깊이(0-based), leaf=dims.length-1, total=-1 */
  level: number;
  /** 버킷 키 경로 */
  path: string[];
  /** path 평행 표시 라벨 — 빌더가 미리 계산, render에서 이전 행 참조 금지 */
  labels: string[];
  count: number;
  sums: Record<string, number>;
  /** 결정적 rowKey: `kind:path.join("›")` */
  id: string;
}

// ── 내부: 중첩 Map 트리 노드 ──────────────────────────────────

interface TreeNode {
  bucketKey: string;
  displayLabel: string;
  count: number;
  sums: Record<string, number>;
  children: Map<string, TreeNode>;
}

function makeNode(bucketKey: string, displayLabel: string, measureKeys: string[]): TreeNode {
  const sums: Record<string, number> = {};
  for (const k of measureKeys) sums[k] = 0;
  return { bucketKey, displayLabel, count: 0, sums, children: new Map() };
}

/** 빈 버킷("")은 맨 끝, 나머지는 오름차순. 결정적 비교자. */
function bucketComparator(a: string, b: string): number {
  if (a === "" && b !== "") return 1;
  if (a !== "" && b === "") return -1;
  return a < b ? -1 : a > b ? 1 : 0;
}

// ── 내부: 트리 빌드 헬퍼 ─────────────────────────────────────

/**
 * 각 행을 dims 계층에 따라 중첩 Map 트리로 누적.
 * buildAggregateRows와 buildAggregateOutline이 공유하는 순수 헬퍼.
 */
function buildTree<T>(
  rows: T[],
  dims: AggregateDimension<T>[],
  measures: AggregateMeasure<T>[],
): Map<string, TreeNode> {
  const measureKeys = measures.map((m) => m.key);
  const root: Map<string, TreeNode> = new Map();

  for (const row of rows) {
    let currentMap = root;
    const nodeStack: TreeNode[] = [];

    for (let d = 0; d < dims.length; d++) {
      const bk = dims[d].bucketKey(row) ?? "";
      const dl = bk === "" ? "(미지정)" : dims[d].display(row) || "(미지정)";

      if (!currentMap.has(bk)) {
        currentMap.set(bk, makeNode(bk, dl, measureKeys));
      }
      const node = currentMap.get(bk)!;
      nodeStack.push(node);
      currentMap = node.children;
    }

    // 트리 경로 상 모든 조상 노드에 누적
    for (const node of nodeStack) {
      node.count += 1;
      for (const m of measures) {
        node.sums[m.key] += Number(row[m.field] ?? 0);
      }
    }
  }

  return root;
}

// ── DFS flat화 (기존 subtotal/leaf 방식) ──────────────────────

function dfsEmit<T>(
  nodes: Map<string, TreeNode>,
  dims: AggregateDimension<T>[],
  measures: AggregateMeasure<T>[],
  depth: number,
  parentPath: string[],
  parentLabels: string[],
  result: AggregateRow[],
): void {
  const sorted = [...nodes.entries()].sort(([a], [b]) => bucketComparator(a, b));
  const isLeafDepth = depth === dims.length - 1;

  for (const [, node] of sorted) {
    const path = [...parentPath, node.bucketKey];
    const labels = [...parentLabels, node.displayLabel];

    if (isLeafDepth) {
      result.push({
        kind: "leaf",
        level: depth,
        path,
        labels,
        count: node.count,
        sums: { ...node.sums },
        id: `leaf:${path.join("›")}`,
      });
    } else {
      // 자식 먼저 emit
      dfsEmit(node.children, dims, measures, depth + 1, path, labels, result);
      // 소계(dims.length >= 2일 때만 발생 — isLeafDepth=false이므로 보장)
      result.push({
        kind: "subtotal",
        level: depth,
        path,
        labels,
        count: node.count,
        sums: { ...node.sums },
        id: `subtotal:${path.join("›")}`,
      });
    }
  }
}

// ── DFS pre-order 방출 (outline 트리 아웃라인 방식) ───────────

/**
 * 트리를 pre-order(그룹 헤더 먼저, 그 다음 자식)로 방출.
 * leaf depth에서는 kind:"leaf", 그 위에서는 kind:"group" + 자식 재귀.
 * total 행은 만들지 않음(footer가 grand total 담당).
 */
function dfsEmitOutline<T>(
  nodes: Map<string, TreeNode>,
  dims: AggregateDimension<T>[],
  measures: AggregateMeasure<T>[],
  depth: number,
  parentPath: string[],
  parentLabels: string[],
  result: AggregateRow[],
): void {
  const sorted = [...nodes.entries()].sort(([a], [b]) => bucketComparator(a, b));
  const isLeafDepth = depth === dims.length - 1;

  for (const [, node] of sorted) {
    const path = [...parentPath, node.bucketKey];
    const labels = [...parentLabels, node.displayLabel];

    if (isLeafDepth) {
      result.push({
        kind: "leaf",
        level: depth,
        path,
        labels,
        count: node.count,
        sums: { ...node.sums },
        id: `leaf:${path.join("›")}`,
      });
    } else {
      // pre-order: 그룹 헤더 먼저
      result.push({
        kind: "group",
        level: depth,
        path,
        labels,
        count: node.count,
        sums: { ...node.sums },
        id: `group:${path.join("›")}`,
      });
      // 그 다음 자식 재귀
      dfsEmitOutline(node.children, dims, measures, depth + 1, path, labels, result);
    }
  }
}

// ── 공개 API ──────────────────────────────────────────────────

/**
 * dims 순서로 중첩 Map 트리에 누적 후 DFS flat화.
 *
 * - 단일 dim: leaf + total (소계 없음)
 * - 다중 dim: leaf + subtotal(level=소계 깊이) + total
 * - dim 0개: []
 * - rows 0개: []
 */
export function buildAggregateRows<T>(
  rows: T[],
  dims: AggregateDimension<T>[],
  measures: AggregateMeasure<T>[],
): AggregateRow[] {
  if (dims.length === 0 || rows.length === 0) return [];

  const measureKeys = measures.map((m) => m.key);
  const root = buildTree(rows, dims, measures);

  // DFS flat화
  const result: AggregateRow[] = [];
  dfsEmit(root, dims, measures, 0, [], [], result);

  // 총계 (rows > 0 보장)
  const totalSums: Record<string, number> = {};
  for (const k of measureKeys) totalSums[k] = 0;
  for (const row of rows) {
    for (const m of measures) {
      totalSums[m.key] += Number(row[m.field] ?? 0);
    }
  }

  result.push({
    kind: "total",
    level: -1,
    path: [],
    labels: [],
    count: rows.length,
    sums: totalSums,
    id: "total",
  });

  return result;
}

/**
 * dims 순서로 트리를 빌드 후 pre-order(그룹 헤더 → 자식)로 방출.
 * 트리 아웃라인 표시에 사용. total 행 미생성(footer가 담당).
 *
 * - dim 0개 또는 rows 0개: []
 * - 단일 dim: group 헤더 없이 leaf 평면 나열
 * - 다중 dim: group(level=0..N-2) 헤더가 자식보다 먼저(pre-order), leaf(level=N-1) 가장 깊게
 */
export function buildAggregateOutline<T>(
  rows: T[],
  dims: AggregateDimension<T>[],
  measures: AggregateMeasure<T>[],
): AggregateRow[] {
  if (dims.length === 0 || rows.length === 0) return [];

  const root = buildTree(rows, dims, measures);
  const result: AggregateRow[] = [];
  dfsEmitOutline(root, dims, measures, 0, [], [], result);
  return result;
}

/**
 * 트리 아웃라인(pre-order)에서 접힌 그룹의 자손을 숨겨 가시 행만 반환.
 * collapsedIds에 든 group id의 하위(level이 더 깊은 연속 행)를 제거.
 */
export function collapseOutline(rows: AggregateRow[], collapsedIds: ReadonlySet<string>): AggregateRow[] {
  const result: AggregateRow[] = [];
  let hideDeeperThan = Infinity;
  for (const row of rows) {
    if (row.level <= hideDeeperThan) {
      hideDeeperThan = Infinity;
      result.push(row);
      if (row.kind === "group" && collapsedIds.has(row.id)) {
        hideDeeperThan = row.level;
      }
    }
    // else: 접힌 그룹의 자손 → 스킵
  }
  return result;
}
