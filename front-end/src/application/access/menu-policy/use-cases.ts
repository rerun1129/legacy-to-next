import { accessMenuPolicyPort } from "@/lib/ports";
import type { CreateMenuPolicyDto } from "@/domain/access/policy";

export const accessMenuPolicyUseCases = {
  listByMenu: (menuId: number) => accessMenuPolicyPort.listByMenu(menuId),
  create: (req: CreateMenuPolicyDto) => accessMenuPolicyPort.create(req),
  delete: (id: number) => accessMenuPolicyPort.delete(id),
  deleteMany: (ids: number[]) => accessMenuPolicyPort.deleteMany(ids),
};
