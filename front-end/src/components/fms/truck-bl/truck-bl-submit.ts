import type {
  CreateTruckBlRequest,
  UpdateTruckBlRequest,
  TruckOrderCreateRequest,
  TruckOrderUpdateRequest,
  TruckDescRequest,
  TruckBlDimRequest,
  TruckBlFreightLineRequest,
} from '@/domain/truck-bl';
import type { TruckBlFormValues } from './truck-bl-schema';
import type { FreightRow } from '@/components/fms/house-bl/house-bl-schema';

/** 빈 문자열·null → undefined 정규화 */
function toStr(v: string | undefined): string | undefined {
  return v?.trim() || undefined;
}

/** 문자열 형식의 수치 필드 → number 변환. 빈 문자열이면 undefined 반환. */
function toNum(v: string | number | undefined): number | undefined {
  if (v === undefined || v === null) return undefined;
  if (typeof v === 'number') return Number.isNaN(v) ? undefined : v;
  const trimmed = v.trim();
  if (trimmed === '') return undefined;
  const n = Number(trimmed);
  return Number.isNaN(n) ? undefined : n;
}

/** FreightRow 배열 → TruckBlFreightLineRequest 배열 변환 */
function buildFreightLines(rows: FreightRow[] | undefined): TruckBlFreightLineRequest[] | undefined {
  if (!rows || rows.length === 0) return undefined;
  return rows.map((r) => ({
    id:            r.id,
    freightCode:   toStr(r.freightCode),
    per:           toStr(r.per),
    qty:           toStr(r.qty),
    price:         toStr(r.price),
    currency:      toStr(r.currency),
    customerCode:  toStr(r.customerCode),
    taxType:       toStr(r.taxType),
    performanceDt: toStr(r.performanceDt),
  }));
}

function buildDescRequest(form: TruckBlFormValues): TruckDescRequest | undefined {
  const marks = toStr(form.marks);
  const description = toStr(form.description);
  const descClause1 = toStr(form.descClause1);
  const descClause2 = toStr(form.descClause2);
  // desc 필드 중 하나라도 있으면 객체 생성 (remark는 본체로 이전 — §0d8c3b4)
  if (!marks && !description && !descClause1 && !descClause2) return undefined;
  return { marks, description, descClause1, descClause2 };
}

function buildTruckOrderCreateRows(form: TruckBlFormValues): TruckOrderCreateRequest[] | undefined {
  if (!form.truckOrders || form.truckOrders.length === 0) return undefined;
  return form.truckOrders.map((row) => ({
    truckOrderNo:  toStr(row.truckOrderNo),
    pkgQty:        toNum(row.pkgQty),
    pkgUnit:       toStr(row.pkgUnit),
    grossWeightKg: toNum(row.grossWeightKg),
    cbm:           toNum(row.cbm),
    truckNo:       toStr(row.truckNo),
    truckType:     toStr(row.truckType),
    driver:        toStr(row.driver),
    mobileNo:      toStr(row.mobileNo),
    containerNo:   toStr(row.containerNo),
    containerType: toStr(row.containerType),
    sealNo1:       toStr(row.sealNo1),
    sealNo2:       toStr(row.sealNo2),
    sealNo3:       toStr(row.sealNo3),
  }));
}

function buildTruckDimCreateRows(form: TruckBlFormValues): TruckBlDimRequest[] | undefined {
  if (!form.dimensions || form.dimensions.length === 0) return undefined;
  return form.dimensions.map((d) => ({
    lengthCm:       toNum(d.length),
    widthCm:        toNum(d.width),
    heightCm:       toNum(d.height),
    quantity:       toNum(d.qty),
    cbm:            toNum(d.cbm),
    volumeWeightKg: toNum(d.volWt),
  }));
}

function buildTruckDimUpdateRows(form: TruckBlFormValues): TruckBlDimRequest[] | undefined {
  if (!form.dimensions || form.dimensions.length === 0) return undefined;
  return form.dimensions.map((d) => ({
    id:             d.id,
    lengthCm:       toNum(d.length),
    widthCm:        toNum(d.width),
    heightCm:       toNum(d.height),
    quantity:       toNum(d.qty),
    cbm:            toNum(d.cbm),
    volumeWeightKg: toNum(d.volWt),
  }));
}

function buildTruckOrderUpdateRows(form: TruckBlFormValues): TruckOrderUpdateRequest[] | undefined {
  if (!form.truckOrders || form.truckOrders.length === 0) return undefined;
  return form.truckOrders.map((row) => ({
    // DB id 포함 — BE merge-by-id (§6.28)
    id:            row.id,
    truckOrderNo:  toStr(row.truckOrderNo),
    pkgQty:        toNum(row.pkgQty),
    pkgUnit:       toStr(row.pkgUnit),
    grossWeightKg: toNum(row.grossWeightKg),
    cbm:           toNum(row.cbm),
    truckNo:       toStr(row.truckNo),
    truckType:     toStr(row.truckType),
    driver:        toStr(row.driver),
    mobileNo:      toStr(row.mobileNo),
    containerNo:   toStr(row.containerNo),
    containerType: toStr(row.containerType),
    sealNo1:       toStr(row.sealNo1),
    sealNo2:       toStr(row.sealNo2),
    sealNo3:       toStr(row.sealNo3),
  }));
}

/** TruckBlFormValues → CreateTruckBlRequest 변환 */
export function buildTruckBlCreateRequest(form: TruckBlFormValues): CreateTruckBlRequest {
  return {
    hblNo:              toStr(form.truckBlNo),
    bound:              toStr(form.bound),
    shipperCode:        toStr(form.shipperCode),
    shipperAddress:     toStr(form.shipperAddr),
    consigneeCode:      toStr(form.consigneeCode),
    consigneeAddress:   toStr(form.consigneeAddr),
    notifyCode:         toStr(form.notifyCode),
    notifyAddress:      toStr(form.notifyAddr),
    settlePartnerCode:  toStr(form.settlePartnerCode),
    docPartnerAddress:  toStr(form.docPartnerAddress),
    polCode:            toStr(form.polCode),
    podCode:            toStr(form.podCode),
    etd:                toStr(form.etd),
    eta:                toStr(form.eta),
    pkgQty:             typeof form.pkgQty === 'number' ? form.pkgQty : toNum(form.pkgQty as string | undefined),
    pkgUnit:            toStr(form.pkgUnit),
    weightUnit:         toStr(form.weightUnit),
    grossWeightKg:      typeof form.grossWeightKg === 'number' ? form.grossWeightKg : toNum(form.grossWeightKg as string | undefined),
    cbm:                typeof form.cbm === 'number' ? form.cbm : toNum(form.cbm as string | undefined),
    actualCustomerCode: toStr(form.actualCustomerCode),
    operatorCode:       toStr(form.operatorCode),
    teamCode:           toStr(form.teamCode),
    salesManCode:       toStr(form.salesManCode),
    truckerCode:        toStr(form.truckerCode),
    truckerPic:         toStr(form.truckerPic),
    chargeWeightKg:     typeof form.chargeWeightKg === 'number' ? form.chargeWeightKg : toNum(form.chargeWeightKg as string | undefined),
    pickupDate:         toStr(form.pickupDate),
    loadType:           toStr(form.loadType),
    serviceTerm:        toStr(form.serviceTerm),
    voyageNo:           toStr(form.voyNo),
    hsCode:             toStr(form.hsCode),
    remark:             toStr(form.remark),
    volumeDivisor:      toStr(form.dimensionDivisor),
    desc:               buildDescRequest(form),
    truckOrders:        buildTruckOrderCreateRows(form),
    dims:               buildTruckDimCreateRows(form),
    // §Freight 탭 — 환율 헤더 + 매출/매입 라인
    sellRateDt:          toStr(form.sellRateDt),
    sellRateCurrencyCode: toStr(form.sellRateCurrencyCode),
    sellRate:            toStr(form.sellRate),
    buyRateDt:           toStr(form.buyRateDt),
    buyRateCurrencyCode: toStr(form.buyRateCurrencyCode),
    buyRate:             toStr(form.buyRate),
    usdRateDt:           toStr(form.usdRateDt),
    usdRate:             toStr(form.usdRate),
    freightSelling: buildFreightLines(form.freightSelling as FreightRow[] | undefined),
    freightBuying:  buildFreightLines(form.freightBuying as FreightRow[] | undefined),
  };
}

/**
 * Update 빌더 — hblNo 필드 destructure 제외.
 * B/L No 변경은 PUT /{id}/hbl-no 전용 endpoint 사용 (§10 House B/L 계열 hblNo Update 차단).
 */
export function buildTruckBlUpdateRequest(form: TruckBlFormValues): UpdateTruckBlRequest {
  const { hblNo: _hblNo, ...base } = buildTruckBlCreateRequest(form);
  return {
    ...base,
    truckOrders: buildTruckOrderUpdateRows(form),
    dims:        buildTruckDimUpdateRows(form),
  };
}
