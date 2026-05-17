export type Permission = "CODE_MANAGE" | "USER_MANAGE" | "PARTNER_MANAGE" | "CMS_MANAGE";

export const ALL_PERMISSIONS: readonly Permission[] = [
  "CODE_MANAGE",
  "USER_MANAGE",
  "PARTNER_MANAGE",
  "CMS_MANAGE",
] as const;

export const PERMISSION_LABEL: Record<Permission, string> = {
  CODE_MANAGE: "코드 관리",
  USER_MANAGE: "사용자 관리",
  PARTNER_MANAGE: "협력사 관리",
  CMS_MANAGE: "CMS 관리",
};
