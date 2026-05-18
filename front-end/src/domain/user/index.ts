export interface UserRow {
  id: number;
  username: string;
  email: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface UserDetail {
  id: number;
  username: string;
  email: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  attributes: Record<string, string[]>;
}

export type UserScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface UserFilter {
  username: string;
  scope: UserScope;
}

export interface CreateUserRequestDto {
  username: string;
  email: string | null;
  password: string;
  active: boolean;
  attributes: Record<string, string[]>;
}

export interface UpdateUserRequestDto {
  email: string | null;
  password: string | null; // null/empty → BE 미갱신
  active: boolean;
  attributes: Record<string, string[]>;
}
