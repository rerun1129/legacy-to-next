import type { UserPermissionPresetRef } from "@/domain/access/user-permission-preset";

export interface UserPermissionPresetPort {
  listByUser(userId: number): Promise<UserPermissionPresetRef[]>;
  assign(userId: number, presetId: number): Promise<number>;
  revoke(id: number): Promise<void>;
}
