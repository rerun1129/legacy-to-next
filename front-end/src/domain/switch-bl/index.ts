export interface SwitchBlDescription {
  marks?: string | null;
  natureQuantity?: string | null;
}

export interface SwitchBl {
  id: number;
  houseBlId: number;
  switchBlNo?: string | null;
  blType?: string | null;
  incoterms?: string | null;
  shipperCode: string;
  shipperAddress?: string | null;
  consigneeCode?: string | null;
  consigneeAddress?: string | null;
  notifyCode?: string | null;
  notifyAddress?: string | null;
  description?: SwitchBlDescription | null;
}

export interface CreateSwitchBlRequest {
  houseBlId: number;
  switchBlNo?: string;
  blType?: string;
  incoterms?: string;
  shipperCode: string;
  shipperAddress?: string;
  consigneeCode?: string;
  consigneeAddress?: string;
  notifyCode?: string;
  notifyAddress?: string;
  description?: {
    marks?: string;
    natureQuantity?: string;
  };
}

export type UpdateSwitchBlRequest = Partial<CreateSwitchBlRequest>;
