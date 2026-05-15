// Sea Master B/L E2E 회귀 4종 전용 페이로드 타입 및 빌더
// BE CreateMasterBlRequest / UpdateMasterBlRequest / MasterBlDetailResponse.SeaDetailResponse 정합.
// 결정적(deterministic) 값만 사용 — Date.now() / Math.random() / crypto.randomUUID() 0건.

// ── 타입 정의 ──────────────────────────────────────────────────────────────

/**
 * Sea Master E2E 회귀 전용 페이로드.
 * BE CreateMasterBlRequest 필수 필드(SeaMasterGroup) 전수 포함 +
 * seaDetail nested(SeaDetailRequest) + party 3종 + desc 1:1 포함.
 */
export interface SeaMasterFullPayload {
  // root 본체
  jobDiv: 'SEA';
  bound: 'EXP' | 'IMP';
  mblNo: string;
  masterRefNo: string;
  freightTerm: string;
  polCode: string;
  podCode: string;
  etd: string;
  eta: string;
  shipmentType?: string | null;
  teamCode?: string | null;
  shipperCode?: string | null;
  consigneeCode?: string | null;
  notifyCode?: string | null;
  shipperAddress?: string | null;
  consigneeAddress?: string | null;
  notifyAddress?: string | null;
  pkgQty?: number | null;
  pkgUnit?: string | null;
  grossWeightKg?: number | null;
  cbm?: number | null;
  hsCode?: string | null;
  mainItemName?: string | null;
  settlePartnerCode?: string | null;
  operatorCode?: string | null;
  remark?: string | null;
  // SEA 확장 nested
  seaDetail?: {
    loadType?: string | null;
    linerCode?: string | null;
    vesselCode?: string | null;
    vesselName?: string | null;
    voyageNo?: string | null;
    onboardDate?: string | null;
    vesselNationality?: string | null;
    serviceTerm?: string | null;
    blType?: string | null;
    porCode?: string | null;
    finalDestCode?: string | null;
    rton?: number | null;
    lineBkgNo?: string | null;
    issueDate?: string | null;
  } | null;
  // desc 1:1 (SEA Master는 scheduleLegs/dims/airCharges 없음)
  desc?: {
    marks?: string | null;
    description?: string | null;
    descClause1?: string | null;
    descClause2?: string | null;
  } | null;
}

// ── 페이로드 빌더 ──────────────────────────────────────────────────────────

/**
 * SEA EXP Master 회귀용 고정 페이로드 빌더.
 * ts는 spec 파일 단위로 한 번 생성 후 재사용 — 동일 mblNo/masterRefNo 보장.
 */
export function buildSeaExpMasterPayload(
  ts: string,
  overrides?: Partial<SeaMasterFullPayload>,
): SeaMasterFullPayload {
  return {
    jobDiv: 'SEA',
    bound: 'EXP',
    mblNo: `MBL${ts}`,
    masterRefNo: `MREF${ts}`,
    freightTerm: 'PREPAID',
    polCode: 'KRBSA',
    podCode: 'USLAX',
    etd: '20260601',
    eta: '20260620',
    shipmentType: 'FCL',
    teamCode: 'TEAM01',
    shipperCode: 'SHIPPER01',
    consigneeCode: 'CONSIG01',
    notifyCode: 'NOTIFY01',
    shipperAddress: 'SHIPPER ADDRESS KR',
    consigneeAddress: 'CONSIGNEE ADDRESS US',
    notifyAddress: 'NOTIFY ADDRESS US',
    pkgQty: 100,
    pkgUnit: 'CTN',
    grossWeightKg: 2500,
    cbm: 14.5,
    hsCode: '8517.13',
    mainItemName: 'ELECTRONIC GOODS',
    settlePartnerCode: 'SETTLE01',
    operatorCode: 'OPR01',
    seaDetail: {
      loadType: 'FCL',
      linerCode: 'COSCO',
      vesselCode: 'VSLC01',
      vesselName: 'COSCO PACIFIC',
      voyageNo: '0412N',
      onboardDate: '20260601',
      vesselNationality: 'CN',
      serviceTerm: 'CY/CY',
      blType: 'OBL',
      porCode: 'KRBSA',
      finalDestCode: 'USLAX',
      rton: 14.5,
      lineBkgNo: `BKG${ts}`,
      issueDate: '20260601',
    },
    desc: {
      marks: 'MARK-EXP-001',
      description: 'ELECTRONIC COMPONENTS',
      descClause1: 'SAID TO CONTAIN',
      descClause2: 'SHIPPER LOAD AND COUNT',
    },
    ...overrides,
  };
}

/**
 * SEA IMP Master 회귀용 고정 페이로드 빌더.
 * IMP는 consigneeCode @NotBlank(groups=SeaImpMasterGroup) 필수 — 이미 포함됨.
 */
export function buildSeaImpMasterPayload(
  ts: string,
  overrides?: Partial<SeaMasterFullPayload>,
): SeaMasterFullPayload {
  return {
    jobDiv: 'SEA',
    bound: 'IMP',
    mblNo: `MBL${ts}`,
    masterRefNo: `MREF${ts}`,
    freightTerm: 'COLLECT',
    polCode: 'CNSHA',
    podCode: 'KRBSA',
    etd: '20260515',
    eta: '20260601',
    shipmentType: 'FCL',
    teamCode: 'TEAM02',
    shipperCode: 'SHIPPER02',
    consigneeCode: 'CONSIG02',
    notifyCode: 'NOTIFY02',
    shipperAddress: 'SHIPPER ADDRESS CN',
    consigneeAddress: 'CONSIGNEE ADDRESS KR',
    notifyAddress: 'NOTIFY ADDRESS KR',
    pkgQty: 200,
    pkgUnit: 'CTN',
    grossWeightKg: 5000,
    cbm: 28.0,
    hsCode: '6204.43',
    mainItemName: 'CLOTHING ACCESSORIES',
    settlePartnerCode: 'SETTLE02',
    operatorCode: 'OPR02',
    seaDetail: {
      loadType: 'FCL',
      linerCode: 'EVERGREEN',
      vesselCode: 'VSLC02',
      vesselName: 'EVER GIVEN',
      voyageNo: '0518W',
      onboardDate: '20260515',
      vesselNationality: 'TW',
      serviceTerm: 'CY/CY',
      blType: 'SWB',
      porCode: 'CNSHA',
      finalDestCode: 'KRBSA',
      rton: 28.0,
      lineBkgNo: `BKG${ts}`,
      issueDate: '20260515',
    },
    desc: {
      marks: 'MARK-IMP-001',
      description: 'CLOTHING ACCESSORIES',
      descClause1: 'SAID TO CONTAIN',
      descClause2: 'SHIPPER LOAD AND COUNT',
    },
    ...overrides,
  };
}
