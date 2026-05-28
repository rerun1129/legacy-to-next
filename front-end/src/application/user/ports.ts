import type {
  UserRow,
  UserDetail,
  UserFilter,
  CreateUserRequestDto,
  UpdateUserRequestDto,
  SaveUserChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/user";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface UserPageResult {
  content: UserRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface UserPort {
  search(filter: UserFilter, page: number, size?: number): Promise<UserPageResult>;
  getById(id: number): Promise<UserDetail>;
  create(req: CreateUserRequestDto): Promise<number>;
  update(id: number, req: UpdateUserRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SaveUserChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
