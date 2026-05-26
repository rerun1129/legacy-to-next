import { accessAttributePort } from "@/lib/ports";
import type { CreateAttributeDefinitionDto, UpdateAttributeDefinitionDto } from "@/domain/access/attribute";

export const accessAttributeUseCases = {
  search: (page: number, size?: number) => accessAttributePort.search(page, size),
  getById: (attributeKey: string) => accessAttributePort.getById(attributeKey),
  create: (req: CreateAttributeDefinitionDto) => accessAttributePort.create(req),
  update: (attributeKey: string, req: UpdateAttributeDefinitionDto) => accessAttributePort.update(attributeKey, req),
  delete: (attributeKey: string) => accessAttributePort.delete(attributeKey),
  deleteMany: (keys: string[]) => accessAttributePort.deleteMany(keys),
  getByModule: (moduleCode: string) => accessAttributePort.getByModule(moduleCode),
};
