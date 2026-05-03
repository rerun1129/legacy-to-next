'use client';

import { useState } from 'react';
import type { HouseBlFilter } from '@/domain/house-bl';
import { ListFilter } from './list-filter';
import { HouseBLListGrid } from './house-bl-list-grid';

interface Props {
  variantKey: string;
}

export function HouseBLListClient({ variantKey }: Props) {
  const [extraFilter, setExtraFilter] = useState<Partial<HouseBlFilter>>({});

  return (
    <>
      <ListFilter
        onSearch={(f) => setExtraFilter(f)}
        onReset={() => setExtraFilter({})}
      />
      <HouseBLListGrid variantKey={variantKey} extraFilter={extraFilter} />
    </>
  );
}
