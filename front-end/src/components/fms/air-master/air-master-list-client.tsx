'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import { RotateCcw, Search } from 'lucide-react';
import { Button } from '@/components/shared/button';
import type { AirMasterFilter } from '@/domain/air-master';
import { AirMasterListFilter } from './air-master-list-filter';
import { AirMasterGrid } from './air-master-grid';
import { listFilterStore, type SavedSearchState } from '@/lib/use-list-filter-store';

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

type AirMasterSearchState = SavedSearchState & { extraFilter: AirMasterFilter | null };

export function AirMasterListClient({ bound }: Props) {
  const pathname = usePathname();
  const form = useForm<AirMasterFilter>({
    defaultValues: {
      bound,
      dateKind: 'ETD',
      dateFrom: from,
      dateTo: to,
      masterAwbKind: 'MBL',
      masterAwbValue: '',
      partyKind: 'SHIPPER',
      partyCode: '',
      partyName: '',
      airlineCode: '',
      airlineName: '',
      portKind: 'POL',
      portCode: '',
      portName: '',
      shipmentType: '',
      teamCode: '',
      teamName: '',
    },
  });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<AirMasterFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(pathname) as AirMasterSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(pathname);
    return s?.currentPage ?? 1;
  });
  const [showAll, setShowAll] = useState(() => {
    const s = listFilterStore.getState().getSearch(pathname);
    return s?.showAll ?? true;
  });

  useEffect(() => {
    listFilterStore.getState().setSearch(pathname, { extraFilter, currentPage, showAll });
  }, [extraFilter, currentPage, showAll, pathname]);

  return (
    <>
      <div className="page-head__actions">
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
          <Button
            size="sm"
            variant="normal"
            leftIcon={<RotateCcw size={12} />}
            onClick={() => {
              form.reset({
                bound,
                dateKind: 'ETD',
                dateFrom: from,
                dateTo: to,
                masterAwbKind: 'MBL',
                masterAwbValue: '',
                partyKind: 'SHIPPER',
                partyCode: '',
                partyName: '',
                airlineCode: '',
                airlineName: '',
                portKind: 'POL',
                portCode: '',
                portName: '',
                shipmentType: '',
                teamCode: '',
                teamName: '',
              });
              setExtraFilter(null);
              setCurrentPage(1);
              setShowAll(true);
            }}
          >
            Reset
          </Button>
          <Button
            size="sm"
            variant="search"
            leftIcon={<Search size={12} />}
            onClick={() => form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
              qc.invalidateQueries({ queryKey: ['air-master', 'list', bound] });
            })()}
          >
            Search
          </Button>
        </div>
      </div>

      <AirMasterListFilter form={form} />

      <div style={{ flex: 1, overflow: 'auto', margin: '10px 14px 0', display: 'flex', flexDirection: 'column' }}>
        <AirMasterGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          showAll={showAll}
          onToggleShowAll={() => setShowAll(v => !v)}
          bound={bound}
        />
      </div>
    </>
  );
}
