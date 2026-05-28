import type {
  PermissionPresetSummary,
  PermissionPreset,
  SearchPermissionPresetCriteria,
  CreatePermissionPresetCommand,
  UpdatePermissionPresetCommand,
  AssignAttributeValuesCommand,
} from "@/domain/access/permission-preset";

export interface PermissionPresetPort {
  search(criteria: SearchPermissionPresetCriteria): Promise<PermissionPresetSummary[]>;
  getById(id: number): Promise<PermissionPreset>;
  create(cmd: CreatePermissionPresetCommand): Promise<number>;
  update(id: number, cmd: UpdatePermissionPresetCommand): Promise<void>;
  delete(id: number): Promise<void>;
  assignAttributeValues(id: number, cmd: AssignAttributeValuesCommand): Promise<void>;
}
