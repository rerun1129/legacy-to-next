'use client';

import { useState } from 'react';
import type { MasterBlFilter } from '@/domain/master-bl';
import { MasterBlListFilter } from './master-bl-list-filter';
import { MasterBlGrid } from './master-bl-grid';
import type { MasterVariantConfig } from '@/lib/bl-variants';

interface Props {
  variantKey: string;
  variant: MasterVariantConfig;
}

export function MasterBLListClient({ variantKey, variant }: Props) {
  const [extraFilter, setExtraFilter] = useState<Partial<MasterBlFilter>>({});

  return (
    <>
      <MasterBlListFilter
        onSearch={(f) => setExtraFilter(f)}
        onReset={() => setExtraFilter({})}
      />
      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <MasterBlGrid variantKey={variantKey} variant={variant} extraFilter={extraFilter} />
      </div>
    </>
  );
}
