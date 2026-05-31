'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { RotateCcw, Search } from 'lucide-react';
import { ActionButton } from '@/components/admin/access/action-button';
import type { TruckBlFilter } from '@/domain/truck-bl';
import { TruckBlListFilter } from './truck-bl-list-filter';
import { TruckBlGrid } from './truck-bl-grid';
import { listFilterStore, type SavedSearchState } from '@/lib/use-list-filter-store';
import { DEFAULT_PAGE_SIZE, cyclePageSize } from '@/lib/grid-pagination';

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

const DEFAULT_VALUES: TruckBlFilter = {
  bound: '',
  dateFrom: from,
  dateTo: to,
  truckBlNo: '',
  truckerCode: '', truckerName: '',
  partyCode: '', partyName: '',
  portCode: '', portName: '',
  partnerKind: null, partnerCode: '', partnerName: '',
  operatorCode: '', operatorName: '',
  teamCode: '', teamName: '',
  dateKind: 'ETD',
  partyKind: 'SHIPPER',
  portKind: 'POL',
};

const SCOPE = "/fms/truck-bl/list";

type TruckBlSearchState = SavedSearchState & { extraFilter: TruckBlFilter | null };

export function TruckBlListClient() {
  const menuCode = "FMS_TRUCK_BL_LIST";
  const form = useForm<TruckBlFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<TruckBlFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as TruckBlSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });
  const [pageSize, setPageSize] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.pageSize ?? DEFAULT_PAGE_SIZE;
  });

  const handleCyclePageSize = () => {
    setPageSize(cyclePageSize(pageSize));
    setCurrentPage(1);
  };

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
              qc.invalidateQueries({ queryKey: ['truck-bl', 'list'] });
            })()}
            icon={<Search size={12} style={{ marginRight: 4 }} />} />
        </div>
      </div>

      <TruckBlListFilter form={form} />

      <div style={{ flex: 1, overflow: 'auto', margin: '10px 14px 0', display: 'flex', flexDirection: 'column' }}>
        <TruckBlGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          pageSize={pageSize}
          onCyclePageSize={handleCyclePageSize}
        />
      </div>
    </>
  );
}
