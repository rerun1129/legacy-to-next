// 신규 운임 행 빌더 — 순수 함수.
// handleAdd(freight-panels.tsx)의 헤더 환율·당사자 바인딩 로직 추출.

import type { FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import type { FieldPrefix } from "./freight-cells";
import type { Mode } from "@/lib/bl-variants";

// ── 빈 행 기본값 ─────────────────────────────────────────────
// freight-panels.tsx EMPTY_FREIGHT_ROW(라인 23~44) 이동

export const EMPTY_FREIGHT_ROW: FreightRow = {
  freightCode:         "",
  freightName:         "",
  per:                 "",
  qty:                 "",
  price:               "",
  currency:            "",
  exchangeRate:        "",
  customerCode:        "",
  customerName:        "",
  taxType:             "",
  performanceDt:       "",
  settleAmount:        "",
  localAmount:         "",
  vat:                 "",
  usdExchangeRate:     "",
  usdAmount:           "",
  financialDocType:    "",
  taxNo:               "",
  slipNo:              "",
  financialDocumentNo: "",
};

// ── 신규 행 빌더 ─────────────────────────────────────────────
// handleAdd(freight-panels.tsx, 라인 163~200)의 헤더 바인딩 로직을 그대로 추출.
// append 직전까지의 행 객체를 반환. settle/local/usd/vat는 빈칸으로 시작.

export function buildNewFreightRow(
  formValues: HouseBlFormValues,
  prefix: FieldPrefix,
  mode: Mode | undefined,
): FreightRow {
  // 헤더 환율 → 신규 행에 기본 바인딩
  const currency =
    prefix === "freightSelling"
      ? (formValues.sellRateCurrencyCode ?? "")
      : (formValues.buyRateCurrencyCode ?? "");
  const exchangeRate =
    prefix === "freightSelling"
      ? (formValues.sellRate ?? "")
      : (formValues.buyRate ?? "");
  const usdExchangeRate = formValues.usdRate ?? "";

  // 헤더 당사자 → 신규 행 Customer 자동 바인딩
  let customerCode = "";
  let customerName = "";
  if (prefix === "freightSelling") {
    customerCode = formValues.actualCustomerCode ?? "";
    customerName = formValues.actualCustomerName ?? "";
  } else {
    if (mode === "AIR") {
      customerCode = formValues.airDetail?.airlineCode ?? "";
      customerName = formValues.airDetail?.airlineName ?? "";
    } else {
      customerCode = formValues.seaDetail?.linerCode ?? "";
      customerName = formValues.linerName ?? "";
    }
  }

  return {
    ...EMPTY_FREIGHT_ROW,
    currency,
    exchangeRate,
    usdExchangeRate,
    customerCode,
    customerName,
  };
}
