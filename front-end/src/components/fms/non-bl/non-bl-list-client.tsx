'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { RotateCcw, Search } from 'lucide-react';
import { Button } from '@/components/shared/button';
import type { NonBlFilter } from '@/domain/non-bl';
import { NonBlListFilter } from './non-bl-list-filter';
import { NonBlGrid } from './non-bl-grid';

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

export function NonBlListClient() {
  const form = useForm<NonBlFilter>({ defaultValues: DEFAULT_VALUES });
  const [extraFilter, setExtraFilter] = useState<NonBlFilter | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const qc = useQueryClient();

  return (
    <>
      <div className="page-head__actions">
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
          <Button
            size="sm"
            variant="normal"
            leftIcon={<RotateCcw size={12} />}
            onClick={() => {
              form.reset(DEFAULT_VALUES);
              setExtraFilter(null);
              setCurrentPage(1);
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
              qc.invalidateQueries({ queryKey: ['non-bl', 'list'] });
            })()}
          >
            Search
          </Button>
        </div>
      </div>

      <NonBlListFilter form={form} />

      <div style={{ flex: 1, overflow: 'auto', margin: '10px 14px 0', display: 'flex', flexDirection: 'column' }}>
        <NonBlGrid extraFilter={extraFilter} currentPage={currentPage} onPageChange={setCurrentPage} />
      </div>
    </>
  );
}
