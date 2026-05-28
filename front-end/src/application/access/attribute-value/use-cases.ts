import { accessAttributeValuePort } from "@/lib/ports";
import type { CreateAttributeValueDto, UpdateAttributeValueDto, SaveAttributeValueChangesRequest } from "@/domain/access/attribute-value";

export const accessAttributeValueUseCases = {
  listByKey: (attributeKey: string) => accessAttributeValuePort.listByKey(attributeKey),
  listAll: () => accessAttributeValuePort.listAll(),
  create: (req: CreateAttributeValueDto) => accessAttributeValuePort.create(req),
  update: (attributeKey: string, value: string, req: UpdateAttributeValueDto) =>
    accessAttributeValuePort.update(attributeKey, value, req),
  delete: (attributeKey: string, value: string) => accessAttributeValuePort.delete(attributeKey, value),
  deleteMany: (attributeKey: string, values: string[]) =>
    accessAttributeValuePort.deleteMany(attributeKey, values),
  saveChanges: (req: SaveAttributeValueChangesRequest) => accessAttributeValuePort.saveChanges(req),
};
