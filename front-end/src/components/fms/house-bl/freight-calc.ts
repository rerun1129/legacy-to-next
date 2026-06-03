/**
 * Freight 그리드 단방향 계산 체인 — 순수 함수 모음.
 *
 * 계산 방향(한 셀 수정 시 하위만 재계산, 상위 역산 없음):
 *   qty × price                        = settleAmount
 *   settleAmount × exchangeRate        = localAmount
 *   localAmount ÷ usdExchangeRate      = usdAmount  (0/빈값 → 0)
 *   localAmount × taxRate              = vat        (TAXABLE 0.10, 나머지 0)
 *
 * 값은 모두 string. 계산 후 string으로 반환, 빈값/NaN → "".
 */

/** taxType enum → 세율 */
function toTaxRate(taxType: string | undefined): number {
  if (taxType === "TAXABLE") return 0.1;
  return 0;
}

function p(v: string | undefined): number {
  if (!v || v.trim() === "") return NaN;
  return parseFloat(v);
}

function fmt(n: number): string {
  if (!Number.isFinite(n)) return "";
  return String(n);
}

// ── 개별 계산 함수 (공개) ──────────────────────────────────

export function calcSettle(qty: string | undefined, price: string | undefined): string {
  const q = p(qty);
  const pr = p(price);
  if (Number.isNaN(q) || Number.isNaN(pr)) return "";
  return fmt(q * pr);
}

export function calcLocal(settle: string | undefined, exchangeRate: string | undefined): string {
  const s = p(settle);
  const er = p(exchangeRate);
  if (Number.isNaN(s) || Number.isNaN(er)) return "";
  return fmt(s * er);
}

export function calcUsd(local: string | undefined, usdExchangeRate: string | undefined): string {
  const l = p(local);
  const uer = p(usdExchangeRate);
  // 0 나눗셈 가드: uer 가 0이거나 NaN이면 usdAmount = 0
  if (Number.isNaN(l)) return "";
  if (Number.isNaN(uer) || uer === 0) return "0";
  return fmt(l / uer);
}

export function calcVat(local: string | undefined, taxType: string | undefined): string {
  const l = p(local);
  if (Number.isNaN(l)) return "";
  return fmt(l * toTaxRate(taxType));
}

// ── 체인 함수 (변경 필드 기준 하위만 재계산) ──────────────

export interface FreightCalcRow {
  qty?: string;
  price?: string;
  settleAmount?: string;
  exchangeRate?: string;
  localAmount?: string;
  usdExchangeRate?: string;
  usdAmount?: string;
  taxType?: string;
  vat?: string;
}

/** qty 또는 price 변경 시 재계산할 필드들 */
export function recalcFromQtyPrice(row: FreightCalcRow): Partial<FreightCalcRow> {
  const settle = calcSettle(row.qty, row.price);
  const local  = calcLocal(settle, row.exchangeRate);
  const usd    = calcUsd(local, row.usdExchangeRate);
  const vat    = calcVat(local, row.taxType);
  return { settleAmount: settle, localAmount: local, usdAmount: usd, vat };
}

/** settleAmount 직접 입력 시 재계산할 필드들 */
export function recalcFromSettle(row: FreightCalcRow): Partial<FreightCalcRow> {
  const local = calcLocal(row.settleAmount, row.exchangeRate);
  const usd   = calcUsd(local, row.usdExchangeRate);
  const vat   = calcVat(local, row.taxType);
  return { localAmount: local, usdAmount: usd, vat };
}

/** exchangeRate 변경 시 재계산할 필드들 */
export function recalcFromExchangeRate(row: FreightCalcRow): Partial<FreightCalcRow> {
  const local = calcLocal(row.settleAmount, row.exchangeRate);
  const usd   = calcUsd(local, row.usdExchangeRate);
  const vat   = calcVat(local, row.taxType);
  return { localAmount: local, usdAmount: usd, vat };
}

/** localAmount 직접 입력 시 재계산할 필드들 */
export function recalcFromLocal(row: FreightCalcRow): Partial<FreightCalcRow> {
  const usd = calcUsd(row.localAmount, row.usdExchangeRate);
  const vat = calcVat(row.localAmount, row.taxType);
  return { usdAmount: usd, vat };
}

/** usdExchangeRate 변경 시 재계산할 필드들 */
export function recalcFromUsdExchangeRate(row: FreightCalcRow): Partial<FreightCalcRow> {
  const usd = calcUsd(row.localAmount, row.usdExchangeRate);
  return { usdAmount: usd };
}

/** taxType 변경 시 재계산할 필드들 */
export function recalcFromTaxType(row: FreightCalcRow): Partial<FreightCalcRow> {
  const vat = calcVat(row.localAmount, row.taxType);
  return { vat };
}
