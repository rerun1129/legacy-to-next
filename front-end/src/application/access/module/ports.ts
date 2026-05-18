import type {
  ModuleRow,
  ModuleDetail,
  CreateModuleDto,
  UpdateModuleDto,
} from "@/domain/access/module";

export interface ModulePageResult {
  content: ModuleRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface ModulePort {
  search(page: number, size?: number): Promise<ModulePageResult>;
  getById(moduleCode: string): Promise<ModuleDetail>;
  create(req: CreateModuleDto): Promise<string>;
  update(moduleCode: string, req: UpdateModuleDto): Promise<void>;
  delete(moduleCode: string): Promise<void>;
}
