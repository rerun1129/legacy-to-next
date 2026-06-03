/**
 * Freight Per(단위당) 매핑 상수 및 헬퍼.
 *
 * BE enum API가 quantitySource/scope를 내려주지 않으므로 FE 상수로 관리.
 * BE Per.java code 값과 정합을 맞춰야 한다.
 */

import type { ComboBoxOption } from "@/components/shared/inputs/_types";
import type { Mode } from "@/lib/bl-variants";

// ── Per 도메인 타입 ───────────────────────────────────────────

export type QuantitySource =
  | "FIXED_ONE"
  | "RTON"
  | "CBM"
  | "GROSS_WEIGHT"
  | "CHARGE_WEIGHT"
  | "PKG_QTY"
  | "CONTAINER";

export type PerScope = "SEA" | "AIR" | "TRUCK" | "ALL";

interface PerMeta {
  quantitySource: QuantitySource;
  scope: PerScope;
  /** i18n 라벨 (Per.java description 기준) */
  label: string;
}

// BE Per.java code 값 기준 매핑 (18종)
const PER_META: Record<string, PerMeta> = {
  SHP:  { quantitySource: "FIXED_ONE",     scope: "SEA",   label: "Ship"  },
  BL:   { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "B/L"   },
  CNTR: { quantitySource: "CONTAINER",     scope: "SEA",   label: "CNTR"  }, // SEA에서 컨테이너타입별로 동적 치환됨
  RT:   { quantitySource: "RTON",          scope: "SEA",   label: "R/TON" },
  CB:   { quantitySource: "CBM",           scope: "ALL",   label: "CBM"   },
  OT:   { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "OTH"   },
  CW:   { quantitySource: "CHARGE_WEIGHT", scope: "AIR",   label: "C/WT"  },
  GW:   { quantitySource: "GROSS_WEIGHT",  scope: "ALL",   label: "G/WT"  },
  MIN:  { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "MIN"   },
  UNIT: { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "UNIT"  },
  SET:  { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "SET"   },
  QTY:  { quantitySource: "PKG_QTY",       scope: "ALL",   label: "QTY"   },
  TRK:  { quantitySource: "FIXED_ONE",     scope: "TRUCK", label: "Truck" },
  TRP:  { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "Trip"  },
  RM:   { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "Norm"  },
  M2:   { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "M2"    },
  LIT:  { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "Lit"   },
  TN:   { quantitySource: "FIXED_ONE",     scope: "ALL",   label: "Ton"   },
};

/** ContainerType enum name(예:"T20GP") → label(예:"20GP") 매핑. BE ContainerType.getCode() 기준. */
const CONTAINER_TYPE_LABELS: Record<string, string> = {
  T20GP: "20GP", T20FR: "20FR", T20OT: "20OT", T20RF: "20RF", T20TC: "20TC",
  T20HT: "20HT", T20GH: "20GH", T20RH: "20RH", T20HZ: "20HZ", T20HQ: "20HQ",
  T22GP: "22GP", T22RE: "22RE", T22UT: "22UT", T22PL: "22PL",
  F40GP: "40GP", F40FR: "40FR", F40OT: "40OT", F40RF: "40RF", F40TC: "40TC",
  F40HT: "40HT", F40GH: "40GH", F40RH: "40RH", F40HQ: "40HQ", F40SR: "40SR",
  F40HS: "40HS", F40NR: "40NR", F40FH: "40FH",
  F42GP: "42GP", F42RE: "42RE", F42UT: "42UT", F42PL: "42PL",
  F45GP: "45GP", F45FR: "45FR", F45RE: "45RE", F45HQ: "45HQ", F45R1: "45R1",
};

// ── 헬퍼 함수 ─────────────────────────────────────────────────

/**
 * Per 옵션 목록 생성.
 * - SEA: SEA+ALL scope, CNTR은 컨테이너타입별 집계 항목으로 동적 치환.
 * - AIR: AIR+ALL scope.
 * - TRUCK: TRUCK+ALL scope.
 * - NON_BL: 전 18종 (scope 무시).
 */
export function getPerOptions(
  mode: Mode,
  containers: Array<{ containerType?: string }>,
): ComboBoxOption[] {
  const result: ComboBoxOption[] = [];

  for (const [code, meta] of Object.entries(PER_META)) {
    // scope 필터
    if (mode !== "NON_BL") {
      const allowed =
        meta.scope === "ALL" ||
        meta.scope === mode;
      if (!allowed) continue;
    }

    if (code === "CNTR" && mode === "SEA") {
      // CNTR을 컨테이너타입별 집계 항목으로 치환 — generic "CNTR" 라벨 노출 금지
      const typeCount = aggregateContainerTypes(containers);
      for (const [typeEnumName, count] of Object.entries(typeCount)) {
        const typeLabel = CONTAINER_TYPE_LABELS[typeEnumName] ?? typeEnumName;
        result.push({ value: typeEnumName, label: `${typeLabel} × ${count}` });
      }
    } else {
      result.push({ value: code, label: meta.label });
    }
  }

  return result;
}

/**
 * Per 선택 시점 qty 스냅샷 1회 산출.
 * formValues는 현재 폼 전체 getValues() 결과.
 */
export function computeQtySnapshot(
  perCode: string,
  formValues: {
    seaDetail?: { rton?: string };
    airDetail?: { chargeWeightKg?: string };
    cbm?: string;
    grossWeightKg?: string;
    chargeWeightKg?: string;
    pkgQty?: string;
    containers?: Array<{ containerType?: string }>;
  },
): string {
  const meta = PER_META[perCode];

  if (!meta) {
    // perCode가 컨테이너타입 enum name인 경우 — 해당 타입 집계 수량
    const containers = formValues.containers ?? [];
    const typeCount = aggregateContainerTypes(containers);
    const count = typeCount[perCode];
    return count !== undefined ? String(count) : "";
  }

  switch (meta.quantitySource) {
    case "FIXED_ONE":
      return "1";
    case "RTON":
      return formValues.seaDetail?.rton ?? "";
    case "CBM":
      return formValues.cbm ?? "";
    case "GROSS_WEIGHT":
      return formValues.grossWeightKg ?? "";
    case "CHARGE_WEIGHT":
      // AIR detail chargeWeightKg 우선, 없으면 root
      return formValues.airDetail?.chargeWeightKg ?? formValues.chargeWeightKg ?? "";
    case "PKG_QTY":
      return formValues.pkgQty ?? "";
    case "CONTAINER": {
      // CNTR 자체가 선택된 경우 (NON_BL 등) — 전체 컨테이너 수
      const containers = formValues.containers ?? [];
      const total = containers.filter((c) => c.containerType).length;
      return total > 0 ? String(total) : "";
    }
    default:
      return "";
  }
}

/**
 * Per 셀 디스플레이용 라벨 해석.
 * - perCode가 컨테이너타입 enum name이면 컨테이너타입 라벨.
 * - 아니면 Per 라벨.
 */
export function resolvePerLabel(
  perCode: string,
): string {
  if (CONTAINER_TYPE_LABELS[perCode] !== undefined) {
    // 컨테이너타입 코드 — 저장값 기준 표시 (컨테이너가 이후 삭제돼도 유지)
    return CONTAINER_TYPE_LABELS[perCode];
  }
  return PER_META[perCode]?.label ?? perCode;
}

// ── 내부 유틸 ─────────────────────────────────────────────────

/** containers 배열에서 containerType별 개수를 집계한다. */
function aggregateContainerTypes(
  containers: Array<{ containerType?: string }>,
): Record<string, number> {
  const result: Record<string, number> = {};
  for (const c of containers) {
    const t = c.containerType;
    if (!t) continue;
    result[t] = (result[t] ?? 0) + 1;
  }
  return result;
}
