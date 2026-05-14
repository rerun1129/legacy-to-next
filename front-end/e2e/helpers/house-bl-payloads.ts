// API 직접 POST 페이로드 타입 및 빌더
// E2E의 C(Create) 단계에서 page.request.post()에 전달할 페이로드를 반환한다.
// 백엔드 CreateHouseBlRequest 구조에서 E2E 검증에 필요한 최소 필드만 포함.

// ── 타입 정의 ─────────────────────────────────────────────────────────────

export interface SeaPayload {
  jobDiv: 'SEA';
  bound: 'EXP' | 'IMP';
  hblNo: string;
  shipmentType: 'HOUSE';
  freightTerm: 'PREPAID' | 'COLLECT';
  polCode: string;
  podCode: string;
  seaDetail?: {
    loadType: string;
    linerCode: string;
    vesselName: string;
    voyageNo: string;
    onboardDate: string;
    blType: string;
    serviceTerm: string;
    rton: number;
  };
  containers?: Array<{
    containerNo: string;
    containerType: string;
    lengthFeet: number;
    sealNo1: string;
  }>;
  licenses?: Array<{
    licenseNo: string;
    pkgQty: number;
    pkgUnit: string;
    grossWeightKg: number;
  }>;
}

export interface AirPayload {
  jobDiv: 'AIR';
  bound: 'EXP' | 'IMP';
  hblNo: string;
  shipmentType: 'HOUSE';
  freightTerm: 'PREPAID';
  polCode: string;
  podCode: string;
  scheduleLegs?: Array<{
    toCode: string;
    byCarrier: string;
    flightNo: string;
    onBoardDt: string;
  }>;
  airCharges?: Array<{
    freightCode: string;
    currencyCode: string;
    rateClass: string;
    chargeWeightKg: number;
    rate: number;
  }>;
}

// E2E 회귀 4종 전용 — airDetail nested + 자식 컬렉션 전수 포함 페이로드
export interface AirHouseFullPayload {
  jobDiv: 'AIR';
  bound: 'EXP' | 'IMP';
  hblNo: string;
  shipmentType: 'HOUSE';
  freightTerm: 'PREPAID';
  polCode: string;
  podCode: string;
  airDetail?: {
    airlineCode?: string;
    chargeWeightKg?: number;
    volumeWeightKg?: number;
    rateClass?: string;
    currencyCode?: string;
    issueDate?: string;
    issuePlace?: string;
    signature?: string;
    cargoType?: string;
    originOfGoods?: string;
  };
  scheduleLegs?: Array<{
    toCode: string;
    byCarrier: string;
    flightNo: string;
    onBoardDt: string;
  }>;
  airCharges?: Array<{
    freightCode: string;
    currencyCode: string;
    rateClass: string;
    chargeWeightKg: number;
    rate: number;
  }>;
  dims?: Array<{
    lengthCm: number;
    widthCm: number;
    heightCm: number;
    quantity: number;
    cbm: number;
    volumeWeightKg: number;
  }>;
  desc?: {
    marks?: string;
    description?: string;
    descClause1?: string;
    descClause2?: string;
  };
}

export interface TruckPayload {
  jobDiv: 'TRUCK';
  bound: 'EXP';
  hblNo: string;
  freightTerm: 'PREPAID';
  truckOrders?: Array<{
    truckOrderNo: string;
    truckNo: string;
    driver: string;
    mobileNo: string;
  }>;
}

export interface NonBlPayload {
  jobDiv: 'NON_BL';
  bound: 'EXP';
  hblNo: string;
  freightTerm: 'PREPAID';
}

// ── 페이로드 빌더 ─────────────────────────────────────────────────────────

export function buildSeaExpPayload(ts: string): SeaPayload {
  return {
    jobDiv: 'SEA', bound: 'EXP',
    hblNo: `HBL${ts}`,
    shipmentType: 'HOUSE', freightTerm: 'PREPAID',
    polCode: 'KRBSA', podCode: 'USLAX',
    seaDetail: {
      loadType: 'CY/CY', linerCode: 'COSCO',
      vesselName: 'COSCO EXCELLENCE', voyageNo: '0412E',
      onboardDate: '20260601', blType: 'OBL',
      serviceTerm: 'FCL', rton: 12.5,
    },
    containers: [
      { containerNo: 'CSNU1234567', containerType: '20GP', lengthFeet: 20, sealNo1: 'SL123456' },
      { containerNo: 'TCKU9876543', containerType: '40HC', lengthFeet: 40, sealNo1: 'SL789012' },
    ],
    licenses: [
      { licenseNo: 'LIC-001', pkgQty: 100, pkgUnit: 'CTN', grossWeightKg: 500 },
      { licenseNo: 'LIC-002', pkgQty: 200, pkgUnit: 'CTN', grossWeightKg: 800 },
    ],
  };
}

export function buildSeaImpPayload(ts: string): SeaPayload {
  return {
    jobDiv: 'SEA', bound: 'IMP',
    hblNo: `HBL${ts}`,
    shipmentType: 'HOUSE', freightTerm: 'COLLECT',
    polCode: 'CNSHA', podCode: 'KRBSA',
    seaDetail: {
      loadType: 'CY/CY', linerCode: 'COSCO',
      vesselName: 'COSCO UNITY', voyageNo: '0518W',
      onboardDate: '20260515', blType: 'SWB',
      serviceTerm: 'FCL', rton: 8.0,
    },
    containers: [
      { containerNo: 'GESU5678901', containerType: '40HC', lengthFeet: 40, sealNo1: 'SL567890' },
      { containerNo: 'TCNU8901234', containerType: '20GP', lengthFeet: 20, sealNo1: 'SL678901' },
    ],
  };
}

export function buildAirExpPayload(ts: string): AirPayload {
  return {
    jobDiv: 'AIR', bound: 'EXP',
    hblNo: `HAWB${ts}`,
    shipmentType: 'HOUSE', freightTerm: 'PREPAID',
    polCode: 'KRICN', podCode: 'USLAX',
    scheduleLegs: [
      { toCode: 'PVG', byCarrier: 'KE', flightNo: 'KE851', onBoardDt: '20260601' },
      { toCode: 'LAX', byCarrier: 'KE', flightNo: 'KE017', onBoardDt: '20260602' },
    ],
    airCharges: [
      { freightCode: 'Q', currencyCode: 'USD', rateClass: 'Q', chargeWeightKg: 50, rate: 3.5 },
      { freightCode: 'FS', currencyCode: 'USD', rateClass: 'S', chargeWeightKg: 50, rate: 1.2 },
    ],
  };
}

export function buildAirImpPayload(ts: string): AirPayload {
  return {
    jobDiv: 'AIR', bound: 'IMP',
    hblNo: `HAWB${ts}`,
    shipmentType: 'HOUSE', freightTerm: 'PREPAID',
    polCode: 'USLAX', podCode: 'KRICN',
    scheduleLegs: [
      { toCode: 'ICN', byCarrier: 'OZ', flightNo: 'OZ201', onBoardDt: '20260610' },
      { toCode: 'GMP', byCarrier: 'OZ', flightNo: 'OZ202', onBoardDt: '20260611' },
    ],
    airCharges: [
      { freightCode: 'Q', currencyCode: 'KRW', rateClass: 'Q', chargeWeightKg: 80, rate: 4500 },
      { freightCode: 'SS', currencyCode: 'KRW', rateClass: 'S', chargeWeightKg: 80, rate: 1500 },
    ],
  };
}

export function buildTruckPayload(ts: string): TruckPayload {
  return {
    jobDiv: 'TRUCK', bound: 'EXP',
    hblNo: `TRK${ts}`,
    freightTerm: 'PREPAID',
    truckOrders: [
      { truckOrderNo: 'TO-001', truckNo: 'TRUCK-A1', driver: 'Kim Driver', mobileNo: '010-1234-5678' },
      { truckOrderNo: 'TO-002', truckNo: 'TRUCK-B2', driver: 'Lee Driver', mobileNo: '010-9876-5432' },
    ],
  };
}

export function buildNonBlPayload(ts: string): NonBlPayload {
  return {
    jobDiv: 'NON_BL', bound: 'EXP',
    hblNo: `NONBL${ts}`,
    freightTerm: 'PREPAID',
  };
}

// ── AIR House E2E 회귀 4종 전용 헬퍼 ───────────────────────────────────────
// airDetail nested + 자식 컬렉션 전수 포함. INSERT/enum round-trip/자식 row 수정 검증용.

export function buildAirExpHousePayload(ts: string): AirHouseFullPayload {
  return {
    jobDiv: 'AIR', bound: 'EXP',
    hblNo: `HAWB${ts}`,
    shipmentType: 'HOUSE', freightTerm: 'PREPAID',
    polCode: 'KRICN', podCode: 'USLAX',
    airDetail: {
      airlineCode: 'KE',
      chargeWeightKg: 50,
      volumeWeightKg: 45,
      rateClass: 'Q',
      currencyCode: 'USD',
      issueDate: '20260601',
      issuePlace: 'SEL',
      signature: 'AGENT',
      cargoType: 'GENERAL',
      originOfGoods: 'KR',
    },
    scheduleLegs: [
      { toCode: 'PVG', byCarrier: 'KE', flightNo: 'KE851', onBoardDt: '20260601' },
      { toCode: 'LAX', byCarrier: 'KE', flightNo: 'KE017', onBoardDt: '20260602' },
    ],
    airCharges: [
      { freightCode: 'Q', currencyCode: 'USD', rateClass: 'Q', chargeWeightKg: 50, rate: 3.5 },
      { freightCode: 'FS', currencyCode: 'USD', rateClass: 'S', chargeWeightKg: 50, rate: 1.2 },
    ],
    dims: [
      { lengthCm: 60, widthCm: 40, heightCm: 30, quantity: 2, cbm: 0.144, volumeWeightKg: 24 },
    ],
    desc: {
      marks: 'MARK-EXP-001',
      description: 'ELECTRONIC COMPONENTS',
      descClause1: 'CLAUSE1',
      descClause2: 'CLAUSE2',
    },
  };
}

export function buildAirImpHousePayload(ts: string): AirHouseFullPayload {
  return {
    jobDiv: 'AIR', bound: 'IMP',
    hblNo: `HAWB${ts}`,
    shipmentType: 'HOUSE', freightTerm: 'PREPAID',
    polCode: 'USLAX', podCode: 'KRICN',
    airDetail: {
      airlineCode: 'OZ',
      chargeWeightKg: 80,
      volumeWeightKg: 72,
      rateClass: 'M',
      currencyCode: 'KRW',
      issueDate: '20260610',
      issuePlace: 'LAX',
      signature: 'CARRIER',
      cargoType: 'GENERAL',
      originOfGoods: 'US',
    },
    scheduleLegs: [
      { toCode: 'ICN', byCarrier: 'OZ', flightNo: 'OZ201', onBoardDt: '20260610' },
      { toCode: 'GMP', byCarrier: 'OZ', flightNo: 'OZ202', onBoardDt: '20260611' },
    ],
    airCharges: [
      { freightCode: 'Q', currencyCode: 'KRW', rateClass: 'Q', chargeWeightKg: 80, rate: 4500 },
      { freightCode: 'SS', currencyCode: 'KRW', rateClass: 'S', chargeWeightKg: 80, rate: 1500 },
    ],
    dims: [
      { lengthCm: 80, widthCm: 50, heightCm: 40, quantity: 3, cbm: 0.48, volumeWeightKg: 80 },
    ],
    desc: {
      marks: 'MARK-IMP-001',
      description: 'CLOTHING ACCESSORIES',
      descClause1: 'IMP-CLAUSE1',
      descClause2: 'IMP-CLAUSE2',
    },
  };
}
