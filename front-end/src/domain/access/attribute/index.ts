export type AttributeValueType = "STRING" | "NUMBER" | "BOOLEAN" | "ENUM";

export interface AttributeDefinitionRow {
  attributeKey: string;
  name: string;
  valueType: AttributeValueType;
  allowMulti: boolean;
  active: boolean;
  updatedAt: string;
}

export interface AttributeDefinitionDetail extends AttributeDefinitionRow {
  createdAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CreateAttributeDefinitionDto {
  attributeKey: string;
  name: string;
  valueType: AttributeValueType;
  allowMulti: boolean;
  active: boolean;
}

export interface UpdateAttributeDefinitionDto {
  name: string;
  valueType: AttributeValueType;
  allowMulti: boolean;
  active: boolean;
}

export interface ModuleAttributeValueDto {
  value: string;
  label: string;
}

export interface ModuleAttributeDto {
  attributeKey: string;
  name: string;
  valueType: AttributeValueType;
  allowMulti: boolean;
  values: ModuleAttributeValueDto[];
}
