'use client';

import { useState, useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { RotateCcw, Search } from 'lucide-react';
import { ActionButton } from '@/components/admin/access/action-button';
import type { NonBlFilter } from '@/domain/non-bl';
import { NonBlListFilter } from './non-bl-list-filter';
import { NonBlGrid } from './non-bl-grid';
import { listFilterStore, type SavedSearchState } from '@/lib/use-list-filter-store';
import { DEFAULT_PAGE_SIZE } from '@/lib/grid-pagination';

function getDefaultMonthRange() {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const pad = (n: number) => String(n).padStart(2, '0');
  const lastDate = new Date(y, m + 1, 0).getDate();
  return {
    from: `${y}${pad(m + 1)}01`,
    to: `${y}${pad(m + 1)}${pad(lastDate)}`,
  };
}

// 모듈 로드 시 1회 계산 — client component이므로 hydration mismatch 없음
const { from, to } = getDefaultMonthRange();

const DEFAULT_VALUES: NonBlFilter = {
  bound: '',
  dateFrom: from,
  dateTo: to,
  linerCode: '', linerName: '',
  nonBlNo: '',
  partyCode: '', partyName: '',
  portCode: '', portName: '',
  vessel: '', voyage: '',
  operatorCode: '', operatorName: '',
  teamCode: '', teamName: '',
  dateKind: 'ETD',
  partyKind: 'SHIPPER',
  portKind: 'POL',
};

const SCOPE = "/fms/non-bl/list";

type NonBlSearchState = SavedSearchState & { extraFilter: NonBlFilter | null };
type NonBlListInject = { nonBlNo: string };

export function NonBlListClient() {
  const menuCode = "FMS_NON_BL_LIST";
  const form = useForm<NonBlFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  // Entry → List SPA 이동 시 검색어 자동 바인딩 — inject 슬롯 mount 시 1회 읽기 (pure read)
  const initialInject = useMemo(
    () => listFilterStore.getState().getInject(SCOPE) as NonBlListInject | undefined,
    [],
  );

  const [extraFilter, setExtraFilter] = useState<NonBlFilter | null>(() => {
    if (initialInject?.nonBlNo) return { ...DEFAULT_VALUES, nonBlNo: initialInject.nonBlNo };
    const s = listFilterStore.getState().getSearch(SCOPE) as NonBlSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    if (initialInject?.nonBlNo) return 1;
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });
  const [pageSize, setPageSize] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.pageSize ?? DEFAULT_PAGE_SIZE;
  });

  const handlePageSizeChange = (size: number) => {
    setPageSize(size);
    setCurrentPage(1);
  };

  // inject가 있었으면 form 세팅 + 슬롯 clear (form은 mount 후 존재)
  useEffect(() => {
    if (initialInject?.nonBlNo) {
      listFilterStore.getState().clearInject(SCOPE);
      form.setValue("nonBlNo", initialInject.nonBlNo);
    }
  }, [form, initialInject]);

  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, { extraFilter, currentPage, pageSize });
  }, [extraFilter, currentPage, pageSize]);

  return (
    <>
      <div className="page-head__actions">
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
          <ActionButton buttonCode={`BTN_${menuCode}_RESET`} className="btn btn--normal btn--sm" onClick={() => {
              form.reset(DEFAULT_VALUES);
              setExtraFilter(null);
              setCurrentPage(1);
            }}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />} />
          <ActionButton buttonCode={`BTN_${menuCode}_SEARCH`} className="btn btn--search btn--sm" onClick={() => form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
              qc.invalidateQueries({ queryKey: ['non-bl', 'list'] });
            })()}
            icon={<Search size={12} style={{ marginRight: 4 }} />} />
        </div>
      </div>

      <NonBlListFilter form={form} />

      <div style={{ flex: 1, overflow: 'auto', margin: '10px 14px 0', display: 'flex', flexDirection: 'column' }}>
        <NonBlGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          pageSize={pageSize}
          onPageSizeChange={handlePageSizeChange}
        />
      </div>
    </>
  );
}
