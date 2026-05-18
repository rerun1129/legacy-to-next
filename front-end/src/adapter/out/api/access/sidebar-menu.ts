import { z } from "zod";
import type { SidebarMenuPort } from "@/application/access/sidebar-menu/ports";
import type { SidebarMenuRow } from "@/domain/access/sidebar-menu";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const SIDEBAR_MENU_ROW_SCHEMA = z.object({
  id: z.number(),
  menuCode: z.string(),
  parentId: z.number().nullable(),
  path: z.string().nullable(),
  label: z.string(),
  labelEn: z.string().nullable(),
  icon: z.string().nullable(),
  // BE의 Integer는 nullable이지만 domain은 number — null이면 0 처리
  sortOrder: z.number().nullable().transform((v) => v ?? 0),
  moduleCode: z.string(),
}) satisfies z.ZodType<SidebarMenuRow>;

const ACCESSIBLE_MENUS_RESPONSE_SCHEMA = z.object({
  data: z.array(SIDEBAR_MENU_ROW_SCHEMA),
  message: z.string().optional(),
});

export const API_SIDEBAR_MENU_PORT: SidebarMenuPort = {
  async fetchAccessibleAdminMenus(): Promise<SidebarMenuRow[]> {
    const json = await adminFetchJson("/api/admin/access/menu/accessible");
    const parsed = ACCESSIBLE_MENUS_RESPONSE_SCHEMA.safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid accessible menus response: ${parsed.error.message}`
      );
    }
    return parsed.data.data;
  },
};
