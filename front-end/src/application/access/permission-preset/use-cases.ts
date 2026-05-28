import { permissionPresetPort } from "@/lib/ports";
import type {
  SearchPermissionPresetCriteria,
  CreatePermissionPresetCommand,
  UpdatePermissionPresetCommand,
  AssignAttributeValuesCommand,
  SavePermissionPresetChangesRequest,
} from "@/domain/access/permission-preset";

export const permissionPresetUseCases = {
  search: (criteria: SearchPermissionPresetCriteria) => permissionPresetPort.search(criteria),
  getById: (id: number) => permissionPresetPort.getById(id),
  create: (cmd: CreatePermissionPresetCommand) => permissionPresetPort.create(cmd),
  update: (id: number, cmd: UpdatePermissionPresetCommand) => permissionPresetPort.update(id, cmd),
  delete: (id: number) => permissionPresetPort.delete(id),
  assignAttributeValues: (id: number, cmd: AssignAttributeValuesCommand) =>
    permissionPresetPort.assignAttributeValues(id, cmd),
  saveChanges: (req: SavePermissionPresetChangesRequest) =>
    permissionPresetPort.saveChanges(req),
  autocomplete: (query: string) => permissionPresetPort.autocomplete(query),
};
