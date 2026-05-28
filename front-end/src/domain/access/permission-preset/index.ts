export interface AttributeValueRef {
  id: number;
  attributeKey: string;
  value: string;
  label: string;
}

export interface PermissionPresetSummary {
  id: number;
  code: string;
  name: string;
  description: string | null;
  active: boolean;
  attributeValueIds: number[];
}

export interface PermissionPreset extends PermissionPresetSummary {
  attributeValues: AttributeValueRef[];
}

export interface SearchPermissionPresetCriteria {
  code?: string;
  name?: string;
  activeOnly?: boolean;
}

export interface CreatePermissionPresetCommand {
  code: string;
  name: string;
  description?: string;
  active: boolean;
}

export interface UpdatePermissionPresetCommand {
  name: string;
  description?: string;
  active: boolean;
}

export interface AssignAttributeValuesCommand {
  addIds: number[];
  removeIds: number[];
}
