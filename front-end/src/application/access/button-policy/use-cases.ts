import { accessButtonPolicyPort } from "@/lib/ports";
import type { CreateButtonPolicyDto } from "@/domain/access/policy";

export const accessButtonPolicyUseCases = {
  listByButton: (buttonId: number) => accessButtonPolicyPort.listByButton(buttonId),
  create: (req: CreateButtonPolicyDto) => accessButtonPolicyPort.create(req),
  delete: (id: number) => accessButtonPolicyPort.delete(id),
  deleteMany: (ids: number[]) => accessButtonPolicyPort.deleteMany(ids),
};
