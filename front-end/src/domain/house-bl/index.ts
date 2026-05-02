export interface HouseBlRow {
  id: number;
  hblNo: string;
  bound: 'EXP' | 'IMP';
  // BE 미반영 필드 — 현재 null 가능
  docStatus?: string;
  masterBlId?: number | null;
  polCode?: string;
  podCode?: string;
  shipperCode?: string;
  consigneeCode?: string;
  createdAt?: string;
  etd?: string;
  eta?: string;
}

export interface HouseBlFilter {
  jobDiv?: string;
  bound?: 'EXP' | 'IMP';
  page?: number;
  size?: number;
}
