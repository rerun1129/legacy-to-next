export type JobDiv = 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL';
export type Bound = 'EXP' | 'IMP';

export interface HouseBlRow {
  id: number;
  hblNo: string | null;
  jobDiv: JobDiv;
  bound: Bound;
  polCode: string | null;
  podCode: string | null;
  etd: string | null;
  eta: string | null;
  shipperCode: string | null;
  consigneeCode: string | null;
  pkgQty: number | null;
  pkgUnit: string | null;
  weightUnit?: string;
  createdAt: string;
}

export interface HouseBlDetail extends HouseBlRow {
  shipmentType: 'HOUSE' | 'DIRECT' | null;
  blType: 'OBL' | 'SWB' | 'SURRENDER' | null;
  freightTerm: 'PREPAID' | 'COLLECT' | null;
  notifyCode: string | null;
  deliveryCode: string | null;
  grossWeightKg: number | null;
  cbm: number | null;
  operatorCode: string | null;
  teamCode: string | null;
  salesManCode: string | null;
  masterBlId: number | null;
  updatedAt: string | null;
  docPartnerCode: string | null;
  actualCustomerCode: string | null;
  // §6.48 ⑧ — BE 860abfb에서 노출된 party address 4필드
  shipperAddress: string | null;
  consigneeAddress: string | null;
  notifyAddress: string | null;
  docPartnerAddress: string | null;
  linerCode?: string;
  linerName?: string;
  vesselName?: string;
  voyageNo?: string;
  finalDestCode?: string;
  finalDestName?: string;
  finalEta?: string;
  volumeWeightKg?: number;
  rton?: number;
  remark?: string;
}

export interface HouseBlFilter {
  jobDiv: JobDiv;
  bound: Bound;
  hblNo?: string;
  mblNo?: string;
  shipperCode?: string;
  consigneeCode?: string;
  polCode?: string;
  podCode?: string;
  etdFrom?: string;
  etdTo?: string;
  page?: number;
  size?: number;
}

// ── Sub-entity request types ────────────────────────────────

export interface SeaDetailRequest {
  loadType?: string;
  linerCode?: string;
  vesselCode?: string;
  vesselName?: string;
  voyageNo?: string;
  onboardDate?: string;
  porCode?: string;
  finalDestCode?: string;
  issueDate?: string;
  noOfBl?: number;
  issuePlace?: string;
  doDate?: string;
  payableAt?: string;
  triangle?: boolean;
  serviceTerm?: string;
  vesselNationality?: string;
  rton?: number;
  sayInformation?: string;
  noOfContainerOrPackages?: string;
  blType?: string;
  deliveryCode?: string;
}

export interface DescRequest {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

export interface DimRequest {
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface ContainerRequest {
  // §6.28 — UPDATE 분기에서 row id 포함 필수 (신규 생성 시 undefined)
  id?: number;
  containerNo?: string;
  containerType?: string;
  lengthFeet?: number;
  sealNo1?: string;
  sealNo2?: string;
  sealNo3?: string;
  sealNo4?: string;
  sealNo5?: string;
  sealNo6?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  netWeightKg?: number;
  cbm?: number;
  vgmKg?: number;
  soc?: boolean;
  seq?: number;
}

export interface ScheduleLegRequest {
  // §6.28 — UPDATE 분기에서 row id 포함 필수 (신규 생성 시 undefined)
  id?: number;
  toCode?: string;
  byCarrier?: string;
  flightNo?: string;
  onBoardDt?: string;
  onBoardTm?: string;
  arrivalDt?: string;
  arrivalTm?: string;
}

export interface LicenseRequest {
  licenseNo?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  combinedPackingMark?: string;
  combinedPackingQty?: number;
  combinedPackingUnit?: string;
  partialShipment?: boolean;
  partialShipmentSeq?: number;
  hsnNo?: string;
}

export interface TruckOrderRequest {
  truckOrderNo?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  truckNo?: string;
  truckType?: string;
  driver?: string;
  mobileNo?: string;
  containerNo?: string;
  containerType?: string;
  sealNo1?: string;
  sealNo2?: string;
  sealNo3?: string;
}

export interface AirChargeRequest {
  freightCode?: string;
  currencyCode?: string;
  per?: string;
  freightTerm?: string;
  grossWeightKg?: number;
  rateClass?: string;
  chargeWeightKg?: number;
  rate?: number;
}

// ── Create / Update request ────────────────────────────────

export interface CreateHouseBlRequest {
  jobDiv: JobDiv;
  bound: Bound;
  hblNo?: string;
  shipmentType: 'HOUSE' | 'DIRECT';
  freightTerm: 'PREPAID' | 'COLLECT';
  shipperCode?: string;
  shipperAddress?: string;
  consigneeCode?: string;
  consigneeAddress?: string;
  notifyCode?: string;
  notifyAddress?: string;
  docPartnerCode?: string;
  docPartnerAddress?: string;
  settlePartnerCode?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  weightUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  actualCustomerCode?: string;
  operatorCode?: string;
  teamCode?: string;
  salesManCode?: string;
  masterBlId?: number;
  incoterms?: string;
  salesClass?: string;
  mainItemName?: string;
  hsCode?: string;
  mblNo?: string;
  masterRefNo?: string;
  seaDetail?: SeaDetailRequest;
  desc?: DescRequest;
  dims?: DimRequest[];
  containers?: ContainerRequest[];
  scheduleLegs?: ScheduleLegRequest[];
  licenses?: LicenseRequest[];
  truckOrders?: TruckOrderRequest[];
  airCharges?: AirChargeRequest[];
}

export type UpdateHouseBlRequest = Partial<CreateHouseBlRequest>;
