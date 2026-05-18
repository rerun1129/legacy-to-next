import type {
  AttributeValueRow,
  CreateAttributeValueDto,
  UpdateAttributeValueDto,
} from "@/domain/access/attribute-value";

export interface AttributeValuePort {
  listByKey(attributeKey: string): Promise<AttributeValueRow[]>;
  create(req: CreateAttributeValueDto): Promise<void>;
  update(attributeKey: string, value: string, req: UpdateAttributeValueDto): Promise<void>;
  delete(attributeKey: string, value: string): Promise<void>;
}
