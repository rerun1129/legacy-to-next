export interface AttributeValueRow {
  id: number;
  attributeKey: string;
  value: string;
  label: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface CreateAttributeValueDto {
  attributeKey: string;
  value: string;
  label: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateAttributeValueDto {
  label: string | null;
  sortOrder: number | null;
  active: boolean;
}

// save-changes DTOs

export interface CreateAttributeValueItem {
  attributeKey: string;
  value: string;
  label: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateAttributeValueItem {
  id: number;
  label: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface SaveAttributeValueChangesRequest {
  attributeKey: string;
  creates: CreateAttributeValueItem[];
  updates: UpdateAttributeValueItem[];
  deleteIds: number[];
}
