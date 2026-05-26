'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import { RotateCcw, Search } from 'lucide-react';
import { ActionButton } from '@/components/admin/access/action-button';
import type { AirHouseFilter } from '@/domain/air-house';
import { AirHouseListFilter } from './air-house-list-filter';
import { AirHouseGrid } from './air-house-grid';
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

type AirHouseSearchState = SavedSearchState & { extraFilter: AirHouseFilter | null };

export function AirHouseListClient({ bound }: Props) {
  const menuCode = `FMS_HOUSE_BL_AIR_${bound}_LIST`;
  const pathname = usePathname();
  const form = useForm<AirHouseFilter>({
    defaultValues: {
      bound,
      dateKind: 'ETD',
      dateFrom: from,
      dateTo: to,
      masterAwbKind: 'MBL',
      masterAwbValue: '',
      hblNo: '',
      partyKind: 'SHIPPER',
      partyCode: '',
      partyName: '',
      actualCustomerCode: '',
      actualCustomerName: '',
      settlePartnerCode: '',
      settlePartnerName: '',
      airlineCode: '',
      airlineName: '',
      portKind: 'POL',
      portCode: '',
      portName: '',
      shipmentType: '',
      teamCode: '',
      teamName: '',
      operatorCode: '',
      operatorName: '',
      salesClass: '',
      salesManCode: '',
      salesManName: '',
      incoterms: '',
    },
  });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<AirHouseFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(pathname) as AirHouseSearchState | undefined;
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
          <ActionButton buttonCode={`BTN_${menuCode}_RESET`} className="btn btn--normal btn--sm" onClick={() => {
              form.reset({
                bound,
                dateKind: 'ETD',
                dateFrom: from,
                dateTo: to,
                masterAwbKind: 'MBL',
                masterAwbValue: '',
                hblNo: '',
                partyKind: 'SHIPPER',
                partyCode: '',
                partyName: '',
                actualCustomerCode: '',
                actualCustomerName: '',
                settlePartnerCode: '',
                settlePartnerName: '',
                airlineCode: '',
                airlineName: '',
                portKind: 'POL',
                portCode: '',
                portName: '',
                shipmentType: '',
                teamCode: '',
                teamName: '',
                operatorCode: '',
                operatorName: '',
                salesClass: '',
                salesManCode: '',
                salesManName: '',
                incoterms: '',
              });
              setExtraFilter(null);
              setCurrentPage(1);
              setShowAll(true);
            }}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />} />
          <ActionButton buttonCode={`BTN_${menuCode}_SEARCH`} className="btn btn--search btn--sm" onClick={() => form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
              qc.invalidateQueries({ queryKey: ['air-house', 'list', bound] });
            })()}
            icon={<Search size={12} style={{ marginRight: 4 }} />} />
        </div>
      </div>

      <AirHouseListFilter form={form} />

      <div style={{ flex: 1, overflow: 'auto', margin: '10px 14px 0', display: 'flex', flexDirection: 'column' }}>
        <AirHouseGrid
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
