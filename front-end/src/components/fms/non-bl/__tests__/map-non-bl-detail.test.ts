import { describe, it, expect } from "vitest";
import type { NonBlDetail } from "@/domain/non-bl";
import { mapNonBlDetailToFormValues } from "../map-non-bl-detail";

const BASE_DETAIL: NonBlDetail = {
  id: 1,
  hblNo: "HBLTEST001",
  jobDiv: "SEA",
  bound: "EXP",
  workDivision: "Sea",
  originalBlRef: "REF001",
  shipperCode: "SHP001",
  consigneeCode: "CNS001",
  notifyCode: "NTF001",
  settlePartnerCode: "STL001",
  actualCustomerCode: "ACT001",
  linerCode: "LNR001",
  linerName: "Test Liner",
  vesselName: "TEST VESSEL",
  voyageNo: "001E",
  etd: "2025-01-01",
  eta: "2025-01-15",
  polCode: "KRPUS",
  podCode: "CNSHA",
  finalDestCode: "CNBJS",
  finalDestName: "Beijing",
  finalEta: "2025-01-20",
  mainItemName: "Electronics",
  hsCode: "8542",
  pkgQty: 100,
  pkgUnit: "CTN",
  weightUnit: "KG",
  grossWeightKg: 5000,
  cbm: 20,
  rton: 20,
  volumeWtKg: 3000,
  operatorCode: "OPR001",
  salesManCode: "SLS001",
  teamCode: "TM01",
  salesClass: "A",
  volumeDivisor: "CM6000",
  remark: "Test remark",
  containers: [
    {
      id: 10,
      containerNo: "ABCD1234567",
      containerType: "20GP",
      sealNo1: "SEAL001",
      sealNo2: "SEAL002",
      sealNo3: "",
      pkgQty: 50,
      pkgUnit: "CTN",
      grossWeightKg: 2500,
      cbm: 10,
    },
  ],
  dims: [
    {
      id: 20,
      lengthCm: 100,
      widthCm: 80,
      heightCm: 60,
      quantity: 10,
      cbm: 4.8,
      volumeWeightKg: 800,
    },
  ],
};

describe("mapNonBlDetailToFormValues", () => {
  it("정상 detail → form 값 정확히 매핑", () => {
    const result = mapNonBlDetailToFormValues(BASE_DETAIL);

    expect(result.nonBlNo).toBe("HBLTEST001");
    expect(result.workDiv).toBe("Sea");
    expect(result.bound).toBe("EXP");
    expect(result.refNo).toBe("REF001");
    expect(result.shipperCode).toBe("SHP001");
    expect(result.linerCode).toBe("LNR001");
    expect(result.linerName).toBe("Test Liner");
    expect(result.vesselName).toBe("TEST VESSEL");
    expect(result.voyNo).toBe("001E");
    expect(result.etd).toBe("2025-01-01");
    expect(result.eta).toBe("2025-01-15");
    expect(result.cargoQty).toBe(100);
    expect(result.grossWt).toBe(5000);
    expect(result.totalCbm).toBe(20);
    expect(result.rton).toBe(20);
    expect(result.volWt).toBe(3000);
    expect(result.salesClass).toBe("A");
    // volumeDivisor → dimensionDivisor 필드명 변환
    expect(result.dimensionDivisor).toBe("CM6000");
    expect(result.remark).toBe("Test remark");
  });

  it("containers 배열이 form container 행으로 정확히 매핑", () => {
    const result = mapNonBlDetailToFormValues(BASE_DETAIL);

    expect(result.containers).toHaveLength(1);
    const c = result.containers![0];
    expect(c.id).toBe(10);
    expect(c.cno).toBe("ABCD1234567");
    expect(c.contType).toBe("20GP");
    expect(c.sealNo1).toBe("SEAL001");
    expect(c.pkg).toBe(50);
    expect(c.grossWt).toBe(2500);
    expect(c.cbm).toBe(10);
  });

  it("dims 배열이 form dimension 행으로 정확히 매핑 (숫자 → 문자열)", () => {
    const result = mapNonBlDetailToFormValues(BASE_DETAIL);

    expect(result.dimensions).toHaveLength(1);
    const d = result.dimensions![0];
    expect(d.id).toBe(20);
    expect(d.length).toBe("100");
    expect(d.width).toBe("80");
    expect(d.height).toBe("60");
    expect(d.qty).toBe("10");
    expect(d.cbm).toBe("4.8");
    expect(d.volWt).toBe("800");
  });

  it("nullable 문자열 필드가 null/undefined일 때 빈 문자열로 폴백", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      hblNo: undefined,
      workDivision: undefined,
      originalBlRef: undefined,
      linerCode: undefined,
      linerName: undefined,
      vesselName: undefined,
      voyageNo: undefined,
      etd: undefined,
      eta: undefined,
    };

    const result = mapNonBlDetailToFormValues(detail);

    expect(result.nonBlNo).toBe("");
    expect(result.workDiv).toBe("");
    expect(result.refNo).toBe("");
    expect(result.linerCode).toBe("");
    expect(result.linerName).toBe("");
    expect(result.vesselName).toBe("");
    expect(result.voyNo).toBe("");
    expect(result.etd).toBe("");
    expect(result.eta).toBe("");
  });

  it("nullable 숫자 필드가 undefined일 때 undefined 유지", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      pkgQty: undefined,
      grossWeightKg: undefined,
      cbm: undefined,
      rton: undefined,
      volumeWtKg: undefined,
    };

    const result = mapNonBlDetailToFormValues(detail);

    expect(result.cargoQty).toBeUndefined();
    expect(result.grossWt).toBeUndefined();
    expect(result.totalCbm).toBeUndefined();
    expect(result.rton).toBeUndefined();
    expect(result.volWt).toBeUndefined();
  });

  it("volumeDivisor가 null일 때 dimensionDivisor는 기본값 CM6000", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      volumeDivisor: null,
    };

    const result = mapNonBlDetailToFormValues(detail);

    expect(result.dimensionDivisor).toBe("CM6000");
  });

  it("salesClass가 null일 때 undefined로 매핑", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      salesClass: null,
    };

    const result = mapNonBlDetailToFormValues(detail);

    expect(result.salesClass).toBeUndefined();
  });

  it("container id가 없을 때 배열 인덱스를 id로 사용", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      containers: [
        { containerNo: "CNT001" },
        { containerNo: "CNT002" },
      ],
    };

    const result = mapNonBlDetailToFormValues(detail);

    expect(result.containers![0].id).toBe(0);
    expect(result.containers![1].id).toBe(1);
  });

  it("dims id가 없을 때 배열 인덱스를 id로 사용", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      dims: [
        { lengthCm: 10 },
        { widthCm: 20 },
      ],
    };

    const result = mapNonBlDetailToFormValues(detail);

    expect(result.dimensions![0].id).toBe(0);
    expect(result.dimensions![1].id).toBe(1);
  });

  it("dims 숫자 필드가 null일 때 빈 문자열로 매핑", () => {
    const detail: NonBlDetail = {
      ...BASE_DETAIL,
      dims: [
        {
          id: 99,
          lengthCm: undefined,
          widthCm: undefined,
          heightCm: undefined,
          quantity: undefined,
          cbm: undefined,
          volumeWeightKg: undefined,
        },
      ],
    };

    const result = mapNonBlDetailToFormValues(detail);

    const d = result.dimensions![0];
    expect(d.length).toBe("");
    expect(d.width).toBe("");
    expect(d.height).toBe("");
    expect(d.qty).toBe("");
    expect(d.cbm).toBe("");
    expect(d.volWt).toBe("");
  });
});
