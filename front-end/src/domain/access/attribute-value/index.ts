export interface AttributeValueRow {
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
