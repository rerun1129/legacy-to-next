import type {
  PackageUnitRow,
  PackageUnitDetail,
  PackageUnitFilter,
  CreatePackageUnitRequestDto,
  UpdatePackageUnitRequestDto,
  SavePackageUnitChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/package-unit";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface PackageUnitPageResult {
  content: PackageUnitRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface PackageUnitPort {
  search(filter: PackageUnitFilter, page: number, size?: number): Promise<PackageUnitPageResult>;
  getById(id: number): Promise<PackageUnitDetail>;
  create(req: CreatePackageUnitRequestDto): Promise<number>;
  update(id: number, req: UpdatePackageUnitRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SavePackageUnitChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
