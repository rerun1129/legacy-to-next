export interface HouseBlRow {
  no: number;
  hbl: string;
  expImp: 'EXP' | 'IMP';
  docStatus: 'ok' | 'inprog' | 'draft';
  mbl: string;
  sType: string;
  lType: string;
  etd: string;
  eta: string;
  regDate: string;
  pol: string;
  pod: string;
  vessel: string;
  voyage: string;
  shipper: string;
  consignee: string;
}

export interface HouseBlFilter {
  jobDiv?: string;
  bound?: 'EXP' | 'IMP';
  page?: number;
  size?: number;
}
