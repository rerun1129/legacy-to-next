'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import { RotateCcw, Search } from 'lucide-react';
import { ActionButton } from '@/components/admin/access/action-button';
import type { SeaMasterFilter } from '@/domain/sea-master';
import { SeaMasterListFilter } from './sea-master-list-filter';
import { SeaMasterGrid } from './sea-master-grid';
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

const { from, to } = getDefaultMonthRange();

interface Props {
  bound: "EXP" | "IMP";
}

type SeaMasterSearchState = SavedSearchState & { extraFilter: SeaMasterFilter | null };

export function SeaMasterListClient({ bound }: Props) {
  const menuCode = `FMS_MASTER_BL_SEA_${bound}_LIST`;
  const pathname = usePathname();
  const form = useForm<SeaMasterFilter>({
    defaultValues: {
      bound,
      dateKind: 'ETD',
      dateFrom: from,
      dateTo: to,
      masterBlKind: 'MBL',
      masterBlValue: '',
      partyKind: 'SHIPPER',
      partyCode: '',
      partyName: '',
      linerCode: '',
      linerName: '',
      portKind: 'POL',
      portCode: '',
      portName: '',
      vesselName: '',
      voyageNo: '',
      shipmentType: '',
      loadType: '',
      teamCode: '',
      teamName: '',
    },
  });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<SeaMasterFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(pathname) as SeaMasterSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(pathname);
    return s?.currentPage ?? 1;
  });
  const [pageSize, setPageSize] = useState(() => {
    const s = listFilterStore.getState().getSearch(pathname);
    return s?.pageSize ?? DEFAULT_PAGE_SIZE;
  });

  const handlePageSizeChange = (size: number) => {
    setPageSize(size);
    setCurrentPage(1);
  };

  useEffect(() => {
    listFilterStore.getState().setSearch(pathname, { extraFilter, currentPage, pageSize });
  }, [extraFilter, currentPage, pageSize, pathname]);

  return (
    <>
      <div className="page-head__actions">
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
          <ActionButton buttonCode={`BTN_${menuCode}_RESET`} className="btn btn--normal btn--sm" onClick={() => {
              form.reset({
                bound,
                dateKind: 'ETD',
                dateFrom: from,
                dateTo: to,
                masterBlKind: 'MBL',
                masterBlValue: '',
                partyKind: 'SHIPPER',
                partyCode: '',
                partyName: '',
                linerCode: '',
                linerName: '',
                portKind: 'POL',
                portCode: '',
                portName: '',
                vesselName: '',
                voyageNo: '',
                shipmentType: '',
                loadType: '',
                teamCode: '',
                teamName: '',
              });
              setExtraFilter(null);
              setCurrentPage(1);
            }}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />} />
          <ActionButton buttonCode={`BTN_${menuCode}_SEARCH`} className="btn btn--search btn--sm" onClick={() => form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
              qc.invalidateQueries({ queryKey: ['sea-master', 'list', bound] });
            })()}
            icon={<Search size={12} style={{ marginRight: 4 }} />} />
        </div>
      </div>

      <SeaMasterListFilter form={form} />

      <div style={{ flex: 1, overflow: 'auto', margin: '10px 14px 0', display: 'flex', flexDirection: 'column' }}>
        <SeaMasterGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          pageSize={pageSize}
          onPageSizeChange={handlePageSizeChange}
          bound={bound}
        />
      </div>
    </>
  );
}
