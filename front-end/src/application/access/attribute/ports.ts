import type {
  AttributeDefinitionRow,
  AttributeDefinitionDetail,
  CreateAttributeDefinitionDto,
  UpdateAttributeDefinitionDto,
  ModuleAttributeDto,
  SaveAttributeDefinitionChangesRequest,
  SaveChangesResult,
  AttributeAutocompleteItem,
} from "@/domain/access/attribute";

export interface AttributeDefinitionPageResult {
  content: AttributeDefinitionRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface AttributeDefinitionPort {
  search(page: number, size?: number): Promise<AttributeDefinitionPageResult>;
  getById(attributeKey: string): Promise<AttributeDefinitionDetail>;
  create(req: CreateAttributeDefinitionDto): Promise<string>;
  update(attributeKey: string, req: UpdateAttributeDefinitionDto): Promise<void>;
  delete(attributeKey: string): Promise<void>;
  deleteMany(keys: string[]): Promise<void>;
  getByModule(moduleCode: string): Promise<ModuleAttributeDto[]>;
  saveChanges(req: SaveAttributeDefinitionChangesRequest): Promise<SaveChangesResult>;
  autocomplete(query: string): Promise<AttributeAutocompleteItem[]>;
}
