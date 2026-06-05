// 단일 store 유지·파일만 분리 — use-field-layout.ts에서 순수 헬퍼만 추출.
// 모듈-레벨 단조 증가 카운터 — stable React key 생성용 (Math.random/crypto 미사용)
let _rowSeq = 0;
export function nextRowId() { return `r${++_rowSeq}`; }

// fullKeys에 속한 키는 단독 행(rowMode "full"), 나머지는 n개씩 split 행으로 묶음
export const buildRowsWithFull = (ordered: string[], n: number, fullSet: Set<string>) => {
  const rows: string[][] = [];
  const rowModes: Record<number, "full" | "split"> = {};
  let cur: string[] = [];
  const flush = () => { if (cur.length) { rows.push(cur); cur = []; } };
  for (const k of ordered) {
    if (fullSet.has(k)) { flush(); rows.push([k]); rowModes[rows.length - 1] = "full"; }
    else { cur.push(k); if (cur.length >= n) flush(); }
  }
  flush();
  return { rows, rowModes };
};
