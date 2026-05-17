import type { Permission } from "@/domain/permission";

export type UserRole = "ADMIN" | "USER";

export interface UserRow {
  id: number;
  username: string;
  email: string | null;
  role: UserRole;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface UserDetail {
  id: number;
  username: string;
  email: string | null;
  role: UserRole;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  permissions: Permission[];
}

export type UserScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";
export type RoleUserFilter = "ALL" | "ADMIN" | "USER";

export interface UserFilter {
  username: string;
  role: RoleUserFilter;
  scope: UserScope;
}

export interface CreateUserRequestDto {
  username: string;
  email: string | null;
  password: string;
  role: UserRole;
  active: boolean;
  permissions: Permission[];
}

export interface UpdateUserRequestDto {
  email: string | null;
  password: string | null; // null/empty → BE 미갱신
  role: UserRole;
  active: boolean;
  permissions: Permission[];
}
