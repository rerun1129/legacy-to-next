"use client";

import { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { RotateCcw, Search } from "lucide-react";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { ActionButton } from "@/components/admin/access/action-button";
import { listFilterStore } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE, cyclePageSize } from "@/lib/grid-pagination";
import { PmsPerformanceFilterFms } from "./pms-performance-filter-fms";
import { PmsPerformanceFilterBms } from "./pms-performance-filter-bms";
import { PmsPerformanceGrid } from "./pms-performance-grid";
import { usePmsPerformanceFilterOptions } from "./use-pms-performance-filter-options";
import type { PmsPerformanceFilter } from "./pms-performance-filter-model";
import { DEFAULT_PMS_FILTER } from "./pms-performance-filter-model";
import type { SearchPmsPerformanceInput } from "@/application/pms/performance/ports";

const ROUTE_SCOPE = "/pms/performance";

function buildSearchInput(values: PmsPerformanceFilter): SearchPmsPerformanceInput {
  return {
    basis: values.basis,
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    jobDiv: values.jobDiv || null,
    bound: values.bound || null,
    // dateKind/dateFrom/dateTo: ETD·ETA는 그대로, PERF·DOC는 adapter 계층에서 분기
    dateKind: values.dateKind || null,
    dateFrom: values.dateFrom || null,
    dateTo: values.dateTo || null,
    hblNo: values.hblNo || null,
    mblNo: values.mblNo || null,
    actualCustomerCode: values.actualCustomerCode || null,
    settlePartnerCode: values.settlePartnerCode || null,
    carrierCode: values.carrierCode || null,
    portKind: values.portKind || null,
    portCode: values.portCode || null,
    salesManCode: values.salesManCode || null,
    salesClass: values.salesClass || null,
    incoterms: values.incoterms || null,
    loadType: values.loadType || null,
    teamCode: values.teamCode || null,
    operator: values.operator || null,
    documentTypes: values.documentTypes.length > 0 ? values.documentTypes : null,
    documentStatus: values.documentStatus || null,
    documentNoLike: values.documentNoLike || null,
    groupFinancialNo: values.groupFinancialNo || null,
    grouped: values.grouped || null,
    issued: values.issued || null,
    financialDocType: values.financialDocType || null,
    taxType: values.taxType || null,
  };
}

/**
 * PS-01 실적 조회 클라이언트 Shell.
 * 집계 기준 토글 + 조회 조건 바 + 실적 그리드.
 * Search 명시 트리거만 조회, Reset은 조건·그리드 비우기(invalidate 금지).
 */
export function PmsPerformanceListClient() {
  const t = useTranslations("pms.performance.filter");
  const opts = usePmsPerformanceFilterOptions(t);

  const form = useForm<PmsPerformanceFilter>({
    defaultValues: DEFAULT_PMS_FILTER,
  });

  const [submittedFilter, setSubmittedFilter] = useState<SearchPmsPerformanceInput | null>(() => {
    const saved = listFilterStore.getState().getSearch(ROUTE_SCOPE);
    return (saved?.submittedFilter as SearchPmsPerformanceInput | null | undefined) ?? null;
  });

  const [currentPage, setCurrentPage] = useState(
    () => listFilterStore.getState().getSearch(ROUTE_SCOPE)?.currentPage ?? 1
  );
  const [pageSize, setPageSize] = useState(
    () => listFilterStore.getState().getSearch(ROUTE_SCOPE)?.pageSize ?? DEFAULT_PAGE_SIZE
  );

  // 영속화 저장
  useEffect(() => {
    listFilterStore.getState().setSearch(ROUTE_SCOPE, {
      submittedFilter,
      currentPage,
      pageSize,
    });
  }, [submittedFilter, currentPage, pageSize]);

  const handleCyclePageSize = () => {
    setPageSize(cyclePageSize(pageSize));
    setCurrentPage(1);
  };

  function handleReset() {
    form.reset(DEFAULT_PMS_FILTER);
    setSubmittedFilter(null);
    setCurrentPage(1);
  }

  function handleSearch() {
    form.handleSubmit((values) => {
      setSubmittedFilter(buildSearchInput(values));
      setCurrentPage(1);
    })();
  }

  return (
    <>
      {/* 상단 툴바: Reset / Search */}
      <div className="page-head__actions">
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
          <ActionButton
            buttonCode="BTN_PMS_PERFORMANCE_RESET"
            className="btn btn--normal btn--sm"
            onClick={handleReset}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode="BTN_PMS_PERFORMANCE_SEARCH"
            className="btn btn--search btn--sm"
            onClick={handleSearch}
            icon={<Search size={12} style={{ marginRight: 4 }} />}
          />
        </div>
      </div>

      {/* 집계 기준 ComboBox — PMS 고유, basis 변경은 Search 트리거만으로 반영(자동조회 금지) */}
      <div style={{ padding: "8px 14px 0", display: "flex", alignItems: "center", gap: 8 }}>
        <span style={{ fontSize: "var(--fs-sm)", color: "var(--ink-2)", whiteSpace: "nowrap" }}>
          {t("basisLabel")}
        </span>
        <Controller
          control={form.control}
          name="basis"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={opts.basisOptions}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              style={{ width: 180 }}
            />
          )}
        />
      </div>

      {/* 조회 조건 바 */}
      <form
        noValidate
        onKeyDown={(e) => {
          // Enter 오제출 방지 (textarea 제외)
          if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
            e.preventDefault();
          }
        }}
      >
        <div className="search-card">
          <div className="search-card__body">
            <div className="filter-grid">
              <PmsPerformanceFilterFms
                control={form.control}
                register={form.register}
                setValue={form.setValue}
                watch={form.watch}
                t={t}
                jobDivOptionsWithAll={opts.jobDivOptionsWithAll}
                jobDivLoading={opts.jobDivLoading}
                jobDivPlaceholder={opts.jobDivPlaceholder}
                boundOptionsWithAll={opts.boundOptionsWithAll}
                boundLoading={opts.boundLoading}
                boundPlaceholder={opts.boundPlaceholder}
                dateKindOptions={opts.dateKindOptions}
                portKindOptions={opts.portKindOptions}
                actualCustomer={opts.actualCustomer}
                settlePartner={opts.settlePartner}
                carrier={opts.carrier}
                port={opts.port}
                salesMan={opts.salesMan}
                team={opts.team}
                operator={opts.operator}
              />
              <PmsPerformanceFilterBms
                control={form.control}
                register={form.register}
                t={t}
                groupedOptions={opts.groupedOptions}
                issuedOptions={opts.issuedOptions}
                documentTypeOptions={opts.documentTypeOptions}
                documentStatusOptions={opts.documentStatusOptions}
              />
            </div>
          </div>
        </div>
      </form>

      {/* 실적 그리드 */}
      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
          margin: "10px 14px 0",
        }}
      >
        <PmsPerformanceGrid
          searchFilter={submittedFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          pageSize={pageSize}
          onCyclePageSize={handleCyclePageSize}
        />
      </div>
    </>
  );
}
