import type {
  CountryRow,
  CountryDetail,
  CountryFilter,
  CreateCountryRequestDto,
  UpdateCountryRequestDto,
  SaveCountryChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/country";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface CountryPageResult {
  content: CountryRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface CountryPort {
  search(filter: CountryFilter, page: number, size?: number): Promise<CountryPageResult>;
  getById(id: number): Promise<CountryDetail>;
  create(req: CreateCountryRequestDto): Promise<number>;
  update(id: number, req: UpdateCountryRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SaveCountryChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
