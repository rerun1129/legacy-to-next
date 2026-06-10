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
  /** true = 근사치 표시 중(BE 플래그 기반, 정확치 미도착) */
  isApprox: boolean;
  /** true = opt-in 정확 count 조회 진행 중 */
  isExactLoading: boolean;
  /** true = 근사 표시 중 + 아직 정확 요청 안 함 → 클릭으로 정확 조회 가능 */
  canRequestExact: boolean;
}

/**
 * 점진적 count 훅 — BE 응답 플래그(approximateTotal) 기반.
 *
 * - primary 쿼리: 항상 exactCount=false (디폴트). BE가 "Redis count-index 정확 우선 → 불가 시 근사"를 자동 판단하며
 *   approximateTotal 플래그로 결과를 알려준다.
 * - FE 휴리스틱(92일 / 서류조건 분기)은 count-index 도입 전 유물이므로 제거.
 * - opt-in 정확 쿼리: approximateTotal === true이고 사용자가 클릭(exactRequested=true)할 때만 실행.
 * - 배경 자동 정확 보정 쿼리는 제거 (count-index가 가용하면 이미 정확치이므로 불필요).
 */
export function usePmsPerformanceSearch({
  searchFilter,
  currentPage,
  pageSize,
  exactRequested = false,
}: UsePmsPerformanceSearchParams): UsePmsPerformanceSearchResult {
  const PLACEHOLDER: SearchPmsPerformanceInput = { basis: "FREIGHT_INPUT", page: 0, size: pageSize, exactCount: false };

  // 주 쿼리 — 실제 행 데이터 + 근사/정확 count (BE 자동 판단)
  const primaryInput: SearchPmsPerformanceInput | null = searchFilter
    ? { ...searchFilter, page: currentPage - 1, size: pageSize, exactCount: false }
    : null;

  const {
    data: primaryData,
    isFetching: isPrimaryFetching,
  } = useQuery<PmsPerformancePage>({
    queryKey: pmsPerformanceKeys.search(primaryInput ?? PLACEHOLDER),
    queryFn: () => pmsPerformancePort.search(primaryInput!),
    enabled: searchFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  // opt-in 정확 쿼리 — BE 플래그가 근사이고 사용자가 클릭한 경우에만 실행 (page=0·size=1, 필터당 1회 키 고정)
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
    enabled: searchFilter !== null && primaryData?.approximateTotal === true && exactRequested === true,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows: PmsPerformanceRow[] = primaryData?.content ?? [];
  const primaryTotal               = primaryData?.totalElements;
  const exactTotal                 = exactData?.totalElements;

  // opt-in 정확 count가 도착하면 교체, 그 전까지는 primary count 사용
  const totalElements = exactTotal ?? primaryTotal ?? 0;
  const totalPages    = Math.ceil(totalElements / pageSize) || 0;

  // 틸드(~) 표시: BE가 근사 플래그를 세웠고 정확치가 아직 미도착인 상태
  const isApprox       = primaryData?.approximateTotal === true && exactTotal === undefined;
  const isExactLoading = isExactFetching;

  // 근사 표시 중이고 아직 정확 요청 안 함 → 클릭으로 정확 조회 가능
  const canRequestExact = isApprox && exactRequested !== true;

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
