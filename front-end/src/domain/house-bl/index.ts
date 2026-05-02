export interface HouseBlRow {
  id?: number;
  no?: number;
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

/** Entry 폼에서 신규/수정 저장 시 사용하는 요청 타입 */
export interface HouseBlSaveRequest {
  hbl: string;
  expImp: 'EXP' | 'IMP';
  mbl: string;
  sType: string;
  lType: string;
  etd: string;
  eta: string;
  pol: string;
  pod: string;
  settle: string;
}
