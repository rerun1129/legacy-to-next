import { useQuery } from "@tanstack/react-query";
import { pmsPerformanceKeys } from "@/application/pms/performance/use-cases";
import { pmsPerformancePort } from "@/lib/ports";
import type { PmsPerformancePage, PmsPerformanceRow, SearchPmsPerformanceInput } from "@/application/pms/performance/ports";

interface UsePmsPerformanceSearchParams {
  searchFilter: SearchPmsPerformanceInput | null;
  currentPage: number;
  pageSize: number;
  /** true = 사용자가 정확 count를 명시 요청(opt-in). 기본 false. */
  exactRequested?: boolean;
}

interface UsePmsPerformanceSearchResult {
  rows: PmsPerformanceRow[];
  isFetching: boolean;
  totalElements: number;
  totalPages: number;
  /** true = 근사치 표시 중(정확 count 미도착) */
  isApprox: boolean;
  /** true = 배경 정확 count 조회 진행 중 */
  isExactLoading: boolean;
  /** true = 서류조건 근사 표시 중 + 아직 정확 요청 안 함 → 클릭으로 정확 조회 가능 */
  canRequestExact: boolean;
}

/** 모든 dateKind(ETD/ETA/PERF/DOC)에서 이 일수를 초과할 때만 근사 count를 사용한다 (분기 기준). */
const APPROX_MIN_SPAN_DAYS = 92;

/** yyyyMMdd 문자열 두 개의 일수 차이. 하나라도 없으면 0. */
function spanDays(from: string | null | undefined, to: string | null | undefined): number {
  if (!from || !to) return 0;
  const toDate = (s: string) =>
    new Date(Number(s.slice(0, 4)), Number(s.slice(4, 6)) - 1, Number(s.slice(6, 8)));
  return Math.round((toDate(to).getTime() - toDate(from).getTime()) / 86400000);
}

/**
 * 정형 서류조건(issued/grouped/documentStatus/documentTypes) 존재 여부.
 * 저카디라 정확 count가 범위와 무관하게 느리므로 이 조건이 있으면 전 범위 근사 기본 대상.
 */
export function hasDocLineFilter(f: SearchPmsPerformanceInput | null): boolean {
  if (!f) return false;
  return Boolean(f.issued || f.grouped || f.documentStatus || (f.documentTypes && f.documentTypes.length > 0));
}

/**
 * 조회 조건에 따라 근사 count 사용 여부를 판단한다.
 *
 * - 서류조건(issued/grouped/documentStatus/documentTypes)이 있으면 범위와 무관하게 근사 → opt-in 정확
 * - 서류조건 없고 날짜 범위 > 92일: 근사(sub-second) → 배경 자동 정확 보정
 * - 그 외(범위 ≤ 92일): 정확치 직접(1회 왕복)
 */
function calcNeedsApprox(filter: SearchPmsPerformanceInput | null): boolean {
  if (!filter) return false;
  return hasDocLineFilter(filter) || spanDays(filter.dateFrom, filter.dateTo) > APPROX_MIN_SPAN_DAYS;
}

/**
 * 점진적 count 훅:
 * - needsApprox=false (모든 dateKind·범위 ≤ 92일): primary를 exactCount=true로 호출 → 1회 왕복으로 정확치 바로 표시
 * - needsApprox=true (모든 dateKind·범위 > 92일): primary=근사(fast), 배경=정확 보정
 */
export function usePmsPerformanceSearch({
  searchFilter,
  currentPage,
  pageSize,
  exactRequested = false,
}: UsePmsPerformanceSearchParams): UsePmsPerformanceSearchResult {
  const needsApprox = calcNeedsApprox(searchFilter);
  // 서류조건 여부: true이면 배경 정확은 자동이 아닌 opt-in으로만 실행
  const structured  = hasDocLineFilter(searchFilter);

  const PLACEHOLDER: SearchPmsPerformanceInput = { basis: "FREIGHT_INPUT", page: 0, size: pageSize, exactCount: false };

  // 주 쿼리 — 실제 행 데이터 + (조건에 따라) 정확 or 근사 count
  const primaryInput: SearchPmsPerformanceInput | null = searchFilter
    ? { ...searchFilter, page: currentPage - 1, size: pageSize, exactCount: !needsApprox }
    : null;

  const {
    data: primaryData,
    isFetching: isPrimaryFetching,
    isSuccess: isPrimarySuccess,
  } = useQuery<PmsPerformancePage>({
    queryKey: pmsPerformanceKeys.search(primaryInput ?? PLACEHOLDER),
    queryFn: () => pmsPerformancePort.search(primaryInput!),
    enabled: searchFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  // 배경 정확 쿼리 — needsApprox일 때만 실행 (page=0·size=1로 필터당 1회 키 고정)
  const EXACT_PLACEHOLDER: SearchPmsPerformanceInput = { basis: "FREIGHT_INPUT", page: 0, size: 1, exactCount: true };

  const exactInput: SearchPmsPerformanceInput | null = searchFilter
    ? { ...searchFilter, page: 0, size: 1, exactCount: true }
    : null;

  const {
    data: exactData,
    isFetching: isExactFetching,
  } = useQuery<PmsPerformancePage>({
    queryKey: pmsPerformanceKeys.search(exactInput ?? EXACT_PLACEHOLDER),
    queryFn: () => pmsPerformancePort.search(exactInput!),
    // 서류조건: 클릭 opt-in(exactRequested) 시에만. 그 외 근사(>92일): 기존대로 자동 배경 정확.
    enabled: searchFilter !== null && needsApprox && isPrimarySuccess && (!structured || exactRequested === true),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows: PmsPerformanceRow[] = primaryData?.content ?? [];
  const primaryTotal               = primaryData?.totalElements;
  const exactTotal                 = exactData?.totalElements;

  // needsApprox=false면 primaryTotal이 이미 정확치; needsApprox=true면 exactTotal 도착 시 교체
  const totalElements = exactTotal ?? primaryTotal ?? 0;
  const totalPages    = Math.ceil(totalElements / pageSize) || 0;

  // 틸드(~) 표시: 근사 모드이고 행 데이터는 있으나 정확치 미도착 상태
  const isApprox      = needsApprox && primaryData !== undefined && exactTotal === undefined;
  const isExactLoading = needsApprox && isExactFetching;

  // 서류조건 근사 표시 중 + 아직 정확 요청 안 함 → 클릭으로 정확 조회 가능
  const canRequestExact = isApprox && structured && exactRequested !== true;

  return {
    rows,
    isFetching: isPrimaryFetching,
    totalElements,
    totalPages,
    isApprox,
    isExactLoading,
    canRequestExact,
  };
}
