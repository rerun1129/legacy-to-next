"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { RotateCcw, Search } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import { ActionButton } from "@/components/admin/access/action-button";
import { useTranslations } from "next-intl";
import { listFilterStore } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE, cyclePageSize } from "@/lib/grid-pagination";
import { toast } from "@/lib/toast-store";
import { FinancialDocumentListFilter } from "./financial-document-list-filter";
import { FinancialDocumentMasterGrid } from "./financial-document-master-grid";
import { FinancialDocumentDetailGrid } from "./financial-document-detail-grid";
import { FinancialDocumentGroupModal } from "./group/financial-document-group-modal";
import type { FinancialDocumentListConfig } from "./financial-document-list-config";
import type { FinancialDocumentFilter } from "./use-financial-document-list-filter-model";
import { DEFAULT_FILTER } from "./use-financial-document-list-filter-model";
import type { SearchFinancialDocumentInput, FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";
import { financialDocumentKeys } from "@/application/bms/financial-document/use-cases";

interface Props {
  config: FinancialDocumentListConfig;
}

/**
 * BS-01 Invoice / BS-02 Payment / BS-03 D/C Note 공용 Shell.
 * config 주입으로 3화면 공유, 검색은 Search 명시 트리거만(자동조회 없음).
 */
type DocumentListInject = { documentNoLike?: string };

function buildDocFilter(documentTypes: string[], docNo: string): SearchFinancialDocumentInput {
  return {
    documentTypes,
    documentStatus: null,
    customerCode: null,
    documentNoLike: docNo,
    teamCode: null,
    operator: null,
    documentDtFrom: null,
    documentDtTo: null,
    performanceDtFrom: null,
    performanceDtTo: null,
    etdFrom: null,
    etdTo: null,
    etaFrom: null,
    etaTo: null,
    jobDiv: null,
    bound: null,
  };
}

export function FinancialDocumentListClient({ config }: Props) {
  const scope = config.routeKey;
  const tg = useTranslations("bms.list.group");
  const queryClient = useQueryClient();

  const form = useForm<FinancialDocumentFilter>({ defaultValues: DEFAULT_FILTER });

  // 마운트 시 inject 1회 캡처 (cross-route 진입)
  const [initialInject] = useState<DocumentListInject | undefined>(
    () => listFilterStore.getState().getInject(scope) as DocumentListInject | undefined,
  );

  const [submittedFilter, setSubmittedFilter] = useState<SearchFinancialDocumentInput | null>(() => {
    if (initialInject?.documentNoLike) {
      return buildDocFilter(config.documentTypes, initialInject.documentNoLike);
    }
    // SPA 재진입 시 직전 submittedFilter 복원 — 최초 진입/새로고침은 store가 비어 null 유지
    const saved = listFilterStore.getState().getSearch(scope);
    return (saved?.submittedFilter as SearchFinancialDocumentInput | null | undefined) ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    if (initialInject?.documentNoLike) return 1; // inject 진입 시 page 1
    return listFilterStore.getState().getSearch(scope)?.currentPage ?? 1;
  });
  const [pageSize, setPageSize] = useState(() => listFilterStore.getState().getSearch(scope)?.pageSize ?? DEFAULT_PAGE_SIZE);
  const [selectedRow, setSelectedRow] = useState<FinancialDocumentSearchRow | null>(null);
  const [groupSelectedKeys, setGroupSelectedKeys] = useState<Set<number>>(new Set());
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [groupSnapshot, setGroupSnapshot] = useState<FinancialDocumentSearchRow[]>([]);

  // inject 처리: cross-route는 init에서 완료, same-route는 subscribe 콜백으로 반영
  useEffect(() => {
    // (a) 마운트 시 캡처한 초기 inject 정리 — submittedFilter는 init에 이미 반영됨.
    //     form 동기화 + 슬롯 clear만 수행 (둘 다 React setState 아님)
    if (initialInject?.documentNoLike) {
      form.setValue("documentNoLike", initialInject.documentNoLike);
      form.setValue("dateFrom", "");
      form.setValue("dateTo", "");
      listFilterStore.getState().clearInject(scope);
    }
    // (b) same-route 재진입: 머무는 동안 도착하는 새 inject 반영
    //     콜백 내 setState는 deferred 이벤트라 react-hooks/set-state-in-effect 위반 아님
    const unsub = listFilterStore.subscribe((state, prevState) => {
      // injects[scope]가 바뀐 경우에만 반응 — form.setValue→setFilter / setState→setSearch 등
      // injects 무관 store 변경으로 인한 재진입(무한루프) 차단
      if (state.injects[scope] === prevState.injects[scope]) return;
      const inj = state.injects[scope] as DocumentListInject | undefined;
      if (!inj?.documentNoLike) return;
      const docNo = inj.documentNoLike;
      setSubmittedFilter(buildDocFilter(config.documentTypes, docNo));
      setCurrentPage(1);
      setSelectedRow(null);
      setGroupSelectedKeys(new Set());
      form.setValue("documentNoLike", docNo);
      form.setValue("dateFrom", "");
      form.setValue("dateTo", "");
      // clear 후 재호출돼도 위 guard로 루프 없음
      listFilterStore.getState().clearInject(scope);
    });
    return unsub;
  }, [initialInject, scope, config.documentTypes, form]);

  // 영속화 저장
  useEffect(() => {
    listFilterStore.getState().setSearch(scope, {
      submittedFilter,
      currentPage,
      pageSize,
      selectedId: selectedRow?.financialDocumentId ?? null,
    });
  }, [submittedFilter, currentPage, pageSize, selectedRow, scope]);

  const handleCyclePageSize = () => {
    setPageSize(cyclePageSize(pageSize));
    setCurrentPage(1);
  };

  function handleReset() {
    form.reset(DEFAULT_FILTER);
    setSubmittedFilter(null);
    setSelectedRow(null);
    setCurrentPage(1);
    setGroupSelectedKeys(new Set());
  }

  function handleSearch() {
    form.handleSubmit((values) => {
      // dateKind → 대응 날짜 범위 필드에 매핑
      const dateFrom = values.dateFrom || null;
      const dateTo   = values.dateTo || null;

      const filter: SearchFinancialDocumentInput = {
        documentTypes: config.documentTypes,
        documentStatus: values.documentStatus || null,
        customerCode: values.customerCode || null,
        documentNoLike: values.documentNoLike || null,
        teamCode: values.teamCode || null,
        operator: values.operator || null,
        documentDtFrom:     values.dateKind === "DOCUMENT_DT"   ? dateFrom : null,
        documentDtTo:       values.dateKind === "DOCUMENT_DT"   ? dateTo   : null,
        performanceDtFrom:  values.dateKind === "PERFORMANCE_DT" ? dateFrom : null,
        performanceDtTo:    values.dateKind === "PERFORMANCE_DT" ? dateTo   : null,
        etdFrom:            values.dateKind === "ETD"            ? dateFrom : null,
        etdTo:              values.dateKind === "ETD"            ? dateTo   : null,
        etaFrom:            values.dateKind === "ETA"            ? dateFrom : null,
        etaTo:              values.dateKind === "ETA"            ? dateTo   : null,
        jobDiv: values.jobDiv || null,
        bound:  values.bound  || null,
      };

      setSubmittedFilter(filter);
      setSelectedRow(null);
      setCurrentPage(1);
      setGroupSelectedKeys(new Set());
    })();
  }

  function handleGroupButtonClick() {
    if (groupSelectedKeys.size === 0) {
      toast.error(tg("selectRequired"));
      return;
    }
    // submittedFilter가 null이면 캐시 조회 불가 — 방어 return
    if (submittedFilter === null) return;

    // 클릭 시점에 캐시를 직접 읽어 최신 행 확보 (useMemo deps 불일치로 빈 배열이 되는 결함 방지)
    const cached = queryClient.getQueryData<{ content: FinancialDocumentSearchRow[] }>(
      financialDocumentKeys.search(submittedFilter, currentPage - 1, pageSize),
    );
    const allRows = cached?.content ?? [];
    const snapshot = allRows.filter((r) => groupSelectedKeys.has(r.financialDocumentId));

    if (snapshot.length === 0) return;

    // 동일 고객 정책: customerCode가 2종 이상이면 차단
    const customerCodes = new Set(snapshot.map((r) => r.customerCode));
    if (customerCodes.size > 1) {
      toast.error(tg("customerMixed"));
      return;
    }

    setGroupSnapshot(snapshot);
    setIsGroupModalOpen(true);
  }

  function handleGroupSuccess() {
    setGroupSelectedKeys(new Set());
    setIsGroupModalOpen(false);
  }

  return (
    <>
      {/* 상단 툴바: Reset / Group / Search */}
      <div className="page-head__actions">
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
          <ActionButton
            buttonCode={config.resetButtonCode}
            className="btn btn--normal btn--sm"
            onClick={handleReset}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode={config.groupButtonCode}
            className="btn btn--normal btn--sm"
            onClick={handleGroupButtonClick}
          >
            {tg("button")}
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
      <FinancialDocumentListFilter form={form} scope={scope} />

      {/* 마스터-디테일 2그리드 */}
      <div
        style={{
          display: "grid",
          gridTemplateRows: "1fr 1fr",
          gap: 8,
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
          margin: "10px 14px 0",
        }}
      >
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <FinancialDocumentMasterGrid
            searchFilter={submittedFilter}
            currentPage={currentPage}
            onPageChange={setCurrentPage}
            pageSize={pageSize}
            onCyclePageSize={handleCyclePageSize}
            selectedId={selectedRow?.financialDocumentId ?? null}
            onSelectRow={setSelectedRow}
            selectable
            selectedKeys={groupSelectedKeys}
            onSelectionChange={setGroupSelectedKeys}
          />
        </div>
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <FinancialDocumentDetailGrid
            selectedDocumentId={selectedRow?.financialDocumentId ?? null}
          />
        </div>
      </div>

      {/* 그룹화 모달 */}
      <FinancialDocumentGroupModal
        isOpen={isGroupModalOpen}
        onClose={() => setIsGroupModalOpen(false)}
        rows={groupSnapshot}
        searchFilter={submittedFilter}
        page={currentPage}
        pageSize={pageSize}
        onGroupSuccess={handleGroupSuccess}
      />
    </>
  );
}
