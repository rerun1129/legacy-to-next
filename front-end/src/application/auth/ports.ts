import type { Permission } from "@/domain/permission";

export interface MeInfo {
  id: number;
  username: string;
  email: string | null;
  role: "ADMIN" | "USER";
  permissions: Permission[];
}

export interface AuthPort {
  me(authHeader: string): Promise<MeInfo>;
}
