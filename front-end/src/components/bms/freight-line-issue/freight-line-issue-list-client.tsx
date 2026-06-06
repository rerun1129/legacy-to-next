"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { RotateCcw, Search, Stamp } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import { ActionButton } from "@/components/admin/access/action-button";
import { useTranslations } from "next-intl";
import { listFilterStore } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE, cyclePageSize } from "@/lib/grid-pagination";
import { toast } from "@/lib/toast-store";
import { FreightLineIssueListFilter } from "./freight-line-issue-list-filter";
import { FreightLineIssueGrid } from "./freight-line-issue-grid";
import { FreightLineIssueModal } from "./freight-line-issue-modal";
import type { FreightLineIssueListConfig } from "./freight-line-issue-list-config";
import type { FreightLineIssueFilter } from "./use-freight-line-issue-filter-model";
import { DEFAULT_ISSUE_FILTER } from "./use-freight-line-issue-filter-model";
import { evaluateIssueGate } from "./freight-line-issue-gate";
import { freightLineIssueKeys } from "@/application/bms/freight-line-issue/use-cases";
import type { SearchFreightLineInput, FreightLineIssueRow } from "@/application/bms/freight-line-issue/ports";

interface Props {
  config: FreightLineIssueListConfig;
}

/**
 * BS-E1 세금계산서 발급 / BS-E2 전표 발급 공용 Shell.
 * config 주입으로 2화면 공유. 진입 시 자동조회 억제(submittedFilter=null).
 */
export function FreightLineIssueListClient({ config }: Props) {
  const scope = config.routeKey;
  const t = useTranslations("bms.issue");
  const queryClient = useQueryClient();

  const form = useForm<FreightLineIssueFilter>({ defaultValues: DEFAULT_ISSUE_FILTER });

  const [submittedFilter, setSubmittedFilter] = useState<SearchFreightLineInput | null>(() => {
    // SPA 재진입 시 직전 filter 복원 — 최초 진입/새로고침은 null 유지(자동조회 억제)
    const saved = listFilterStore.getState().getSearch(scope);
    return (saved?.submittedFilter as SearchFreightLineInput | null | undefined) ?? null;
  });
  const [currentPage, setCurrentPage] = useState(
    () => listFilterStore.getState().getSearch(scope)?.currentPage ?? 1,
  );
  const [pageSize, setPageSize] = useState(
    () => listFilterStore.getState().getSearch(scope)?.pageSize ?? DEFAULT_PAGE_SIZE,
  );
  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);
  const [issueSnapshot, setIssueSnapshot] = useState<FreightLineIssueRow[]>([]);

  // 영속화 저장 — submittedFilter/page/pageSize 변경 시마다 동기화
  useEffect(() => {
    listFilterStore.getState().setSearch(scope, {
      submittedFilter,
      currentPage,
      pageSize,
      selectedId: null,
    });
  }, [submittedFilter, currentPage, pageSize, scope]);

  const handleCyclePageSize = () => {
    setPageSize(cyclePageSize(pageSize));
    setCurrentPage(1);
  };

  function handleReset() {
    form.reset(DEFAULT_ISSUE_FILTER);
    setSubmittedFilter(null);
    setCurrentPage(1);
    setSelectedKeys(new Set());
  }

  function handleSearch() {
    form.handleSubmit((values) => {
      const filter: SearchFreightLineInput = {
        customerCode:      values.customerCode || null,
        financialDocType:  values.financialDocType || null,
        jobDiv:            values.jobDiv || null,
        bound:             values.bound || null,
        performanceDtFrom: values.performanceDtFrom || null,
        performanceDtTo:   values.performanceDtTo || null,
        issuedStatus:      values.issuedStatus || null,
      };
      setSubmittedFilter(filter);
      setSelectedKeys(new Set());
      setCurrentPage(1);
    })();
  }

  function handleIssueButtonClick() {
    if (submittedFilter === null) return;

    // 클릭 시점에 캐시를 직접 읽어 최신 행 확보 (stale useMemo 방지)
    const cached = queryClient.getQueryData<{ content: FreightLineIssueRow[] }>(
      freightLineIssueKeys.search(submittedFilter, currentPage - 1, pageSize),
    );
    const allRows = cached?.content ?? [];
    const snapshot = allRows.filter((r) => selectedKeys.has(r.freightLineId));

    const gate = evaluateIssueGate(snapshot, config.issueType);
    if (gate.kind === "error") {
      toast.error(t(`gate.${gate.messageKey}`));
      return;
    }

    setIssueSnapshot(snapshot);
    setIsIssueModalOpen(true);
  }

  function handleIssueSuccess() {
    setSelectedKeys(new Set());
    setIsIssueModalOpen(false);
  }

  return (
    <>
      {/* 상단 툴바: Reset / Search / Issue */}
      <div className="page-head__actions">
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
          <ActionButton
            buttonCode={config.resetButtonCode}
            className="btn btn--normal btn--sm"
            onClick={handleReset}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode={config.issueButtonCode}
            className="btn btn--normal btn--sm"
            onClick={handleIssueButtonClick}
            icon={<Stamp size={12} style={{ marginRight: 4 }} />}
          >
            {t("button.issue")}
          </ActionButton>
          <ActionButton
            buttonCode={config.searchButtonCode}
            className="btn btn--search btn--sm"
            onClick={handleSearch}
            icon={<Search size={12} style={{ marginRight: 4 }} />}
          />
        </div>
      </div>

      {/* 필터 카드 */}
      <FreightLineIssueListFilter form={form} scope={scope} />

      {/* 단일 운임행 그리드 */}
      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
          margin: "10px 14px 0",
          display: "flex",
          flexDirection: "column",
        }}
      >
        <FreightLineIssueGrid
          searchFilter={submittedFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          pageSize={pageSize}
          onCyclePageSize={handleCyclePageSize}
          selectedKeys={selectedKeys}
          onSelectionChange={setSelectedKeys}
          issueType={config.issueType}
        />
      </div>

      {/* 발급 확정 모달 */}
      <FreightLineIssueModal
        isOpen={isIssueModalOpen}
        onClose={() => setIsIssueModalOpen(false)}
        issueType={config.issueType}
        rows={issueSnapshot}
        onIssueSuccess={handleIssueSuccess}
      />
    </>
  );
}
