import { commonCodePort } from "@/lib/ports";
import type { SaveCommonCodeChangesRequest } from "@/domain/common-code";

export const commonCodeUseCases = {
  listGroups: () => commonCodePort.listGroups(),
  listByGroup: (groupCode: string) => commonCodePort.listByGroup(groupCode),
  saveChanges: (req: SaveCommonCodeChangesRequest) => commonCodePort.saveChanges(req),
};
