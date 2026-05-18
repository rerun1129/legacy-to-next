export type Permission = "CODE_MANAGE" | "USER_MANAGE" | "CUSTOMER_MANAGE" | "CMS_MANAGE";

export const ALL_PERMISSIONS: readonly Permission[] = [
  "CODE_MANAGE",
  "USER_MANAGE",
  "CUSTOMER_MANAGE",
  "CMS_MANAGE",
] as const;

export const PERMISSION_LABEL: Record<Permission, string> = {
  CODE_MANAGE: "코드 관리",
  USER_MANAGE: "사용자 관리",
  CUSTOMER_MANAGE: "고객 관리",
  CMS_MANAGE: "CMS 관리",
};
