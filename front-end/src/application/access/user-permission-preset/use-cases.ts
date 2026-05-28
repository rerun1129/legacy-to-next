import { accessUserPermissionPresetPort } from "@/lib/ports";

export const userPermissionPresetUseCases = {
  listByUser: (userId: number) => accessUserPermissionPresetPort.listByUser(userId),
  assign: (userId: number, presetId: number) => accessUserPermissionPresetPort.assign(userId, presetId),
  revoke: (id: number) => accessUserPermissionPresetPort.revoke(id),
};
