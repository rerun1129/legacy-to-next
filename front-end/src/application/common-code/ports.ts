import type {
  CommonCodeGroupRow,
  CommonCodeRow,
  SaveCommonCodeChangesRequest,
  SaveChangesResult,
} from "@/domain/common-code";

export interface CommonCodePort {
  listGroups(): Promise<CommonCodeGroupRow[]>;
  listByGroup(groupCode: string): Promise<CommonCodeRow[]>;
  saveChanges(req: SaveCommonCodeChangesRequest): Promise<SaveChangesResult>;
}
