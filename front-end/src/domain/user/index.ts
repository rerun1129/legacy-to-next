export type UserRole = "ADMIN" | "USER";

export interface UserRow {
  id: number;
  username: string;
  email: string | null;
  role: UserRole;
  active: boolean;
  updatedAt: string;
}

export interface UserDetail {
  id: number;
  username: string;
  email: string | null;
  role: UserRole;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export type ActiveUserFilter = "ALL" | "ACTIVE" | "INACTIVE";
export type RoleUserFilter = "ALL" | "ADMIN" | "USER";

export interface UserFilter {
  username: string;
  role: RoleUserFilter;
  active: ActiveUserFilter;
}

export interface CreateUserRequestDto {
  username: string;
  email: string | null;
  password: string;
  role: UserRole;
  active: boolean;
}

export interface UpdateUserRequestDto {
  email: string | null;
  password: string | null; // null/empty → BE 미갱신
  role: UserRole;
  active: boolean;
}
