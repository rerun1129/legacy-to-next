import type { UserRow } from "@/domain/user";
import type { UserFormRow } from "./user-grid-columns";

export const PASTE_COLS = ["username", "email", "role", "modules", "active"] as const;

// password는 보안상 paste 제외

export const ROW_IS_EQUAL = (a: UserFormRow, b: UserFormRow): boolean =>
  a.email === b.email &&
  a.role === b.role &&
  a.modules === b.modules &&
  a.active === b.active;

// password는 비교 제외 (original은 항상 "")

export const TO_CREATE = (row: UserFormRow) => ({
  username: row.username,
  email: row.email.trim() || null,
  password: row.password,
  active: row.active,
  attributes: {
    role: [row.role],
    module: row.modules.split(",").map((s) => s.trim()).filter(Boolean),
  },
});

export const TO_UPDATE = (row: UserFormRow) => ({
  id: row.entityId,
  email: row.email.trim() || null,
  password: null, // 기존 행은 비밀번호 수정 불가
  active: row.active,
  attributes: {
    ...row._originalAttributes,
    role: [row.role],
    module: row.modules.split(",").map((s) => s.trim()).filter(Boolean),
  },
});

export function toFormRow(row: UserRow): UserFormRow {
  return {
    entityId: row.id,
    username: row.username,
    email: row.email ?? "",
    password: "",
    role: row.attributes?.role?.[0] ?? "USER",
    modules: (row.attributes?.module ?? []).join(","),
    active: row.active,
    _originalAttributes: row.attributes ?? {},
  };
}
