import type {
  AttributeValueRow,
  CreateAttributeValueDto,
  UpdateAttributeValueDto,
  SaveAttributeValueChangesRequest,
} from "@/domain/access/attribute-value";
import type { SaveChangesResult } from "@/domain/access/attribute";

export interface AttributeValuePort {
  listByKey(attributeKey: string): Promise<AttributeValueRow[]>;
  /** attributeKey 필터 없이 전체 attribute_value 목록을 조회한다. */
  listAll(): Promise<AttributeValueRow[]>;
  create(req: CreateAttributeValueDto): Promise<void>;
  update(attributeKey: string, value: string, req: UpdateAttributeValueDto): Promise<void>;
  delete(attributeKey: string, value: string): Promise<void>;
  deleteMany(attributeKey: string, values: string[]): Promise<void>;
  saveChanges(req: SaveAttributeValueChangesRequest): Promise<SaveChangesResult>;
}
