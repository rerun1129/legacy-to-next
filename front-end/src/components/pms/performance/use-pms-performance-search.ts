import { useQuery } from "@tanstack/react-query";
import { pmsPerformanceKeys } from "@/application/pms/performance/use-cases";
import { pmsPerformancePort } from "@/lib/ports";
import type { PmsPerformancePage, PmsPerformanceRow, SearchPmsPerformanceInput } from "@/application/pms/performance/ports";

interface UsePmsPerformanceSearchParams {
  searchFilter: SearchPmsPerformanceInput | null;
  currentPage: number;
  pageSize: number;
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
}

/**
 * 근사 총건수(sub-second) → 배경 정확 count 비동기 보정.
 * - 근사 쿼리: page·size 포함(표시용 행 조회), exactCount=false
 * - 정확 쿼리: page=0·size=1(키 고정, 필터당 1회), exactCount=true
 *   근사 쿼리 성공 후 활성화 → 도착 시 totalElements 교체
 */
export function usePmsPerformanceSearch({
  searchFilter,
  currentPage,
  pageSize,
}: UsePmsPerformanceSearchParams): UsePmsPerformanceSearchResult {
  const PLACEHOLDER: SearchPmsPerformanceInput = { basis: "FREIGHT_INPUT", page: 0, size: pageSize, exactCount: false };

  // 근사(주) 쿼리 — 페이지/페이지크기 포함, 실제 행 데이터 조회
  const approxInput: SearchPmsPerformanceInput | null = searchFilter
    ? { ...searchFilter, page: currentPage - 1, size: pageSize, exactCount: false }
    : null;

  const {
    data: approxData,
    isFetching: isApproxFetching,
    isSuccess: isApproxSuccess,
  } = useQuery<PmsPerformancePage>({
    queryKey: pmsPerformanceKeys.search(approxInput ?? PLACEHOLDER),
    queryFn: () => pmsPerformancePort.search(approxInput!),
    enabled: searchFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  // 정확(배경) 쿼리 — page=0·size=1로 키 고정(필터당 1회만 조회), content 미사용
  const exactInput: SearchPmsPerformanceInput | null = searchFilter
    ? { ...searchFilter, page: 0, size: 1, exactCount: true }
    : null;

  const EXACT_PLACEHOLDER: SearchPmsPerformanceInput = { basis: "FREIGHT_INPUT", page: 0, size: 1, exactCount: true };

  const {
    data: exactData,
    isFetching: isExactFetching,
  } = useQuery<PmsPerformancePage>({
    queryKey: pmsPerformanceKeys.search(exactInput ?? EXACT_PLACEHOLDER),
    queryFn: () => pmsPerformancePort.search(exactInput!),
    // 근사 조회 성공 후에만 활성화 — 근사 결과가 먼저 화면에 표시된 뒤 배경 보정
    enabled: searchFilter !== null && isApproxSuccess,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows: PmsPerformanceRow[] = approxData?.content ?? [];
  const approxTotal               = approxData?.totalElements;
  const exactTotal                = exactData?.totalElements;

  // 정확치 도착 시 totalElements 교체; 미도착이면 근사치 사용
  const totalElements = exactTotal ?? approxTotal ?? 0;
  const totalPages    = Math.ceil(totalElements / pageSize) || 0;

  // 근사 데이터는 있으나 정확 데이터 미도착 상태
  const isApprox = approxData !== undefined && exactTotal === undefined;

  return {
    rows,
    isFetching: isApproxFetching,
    totalElements,
    totalPages,
    isApprox,
    isExactLoading: isExactFetching,
  };
}
