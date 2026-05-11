import type { TruckBlFormValues } from "./truck-bl-schema";

export function createEmptyTruckBlFormValues(): TruckBlFormValues {
  return {
    // 툴바
    truckBlNo:   "",
    bound:       "",
    loadType:    "",
    serviceTerm: "",

    // Party
    shipperCode:       "", shipperName:       "", shipperAddr:       "",
    consigneeCode:     "", consigneeName:     "", consigneeAddr:     "",
    notifyCode:        "", notifyName:        "", notifyAddr:        "",
    docPartnerCode:    "", docPartnerName:    "", docPartnerAddress: "",

    // Schedule
    vesselName: "", voyNo: "",
    etd: "", eta: "",
    polCode: "", polName: "",
    podCode: "", podName: "",

    // Cargo
    pkgQty:        undefined,
    pkgUnit:       "",
    weightUnit:    "",
    grossWeightKg: undefined,
    cbm:           undefined,
    chargeWeightKg: undefined,

    // Document
    pickupDate:  "",
    truckerCode: "", truckerName: "",
    truckerPic:  "",

    // Performance
    actualCustomerCode: "", actualCustomerName: "",
    customerPic:        "",
    settlePartnerCode:  "", settlePartnerName:  "",
    salesManCode:       "", salesManName:       "",
    operatorCode:       "", operatorName:       "",
    teamCode:           "", teamName:           "",

    // Marks & Description
    marks:       "",
    description: "",
    descClause1: "",
    descClause2: "",

    // Freight
    freightSelling: [],
    freightBuying:  [],

    // 자식 그리드
    truckOrders: [],
  };
}
