export interface MasterBlRow {
  id?: number;
  mblNo: string;
  bound: 'EXP' | 'IMP';
  pol: string;
  pod: string;
  etd: string;
  eta: string;
  shipperCode: string;
  consigneeCode: string;
}

export interface MasterBlFilter {
  bound?: 'EXP' | 'IMP';
  page?: number;
  size?: number;
}
