import type { SidebarMenuRow } from "@/domain/access/sidebar-menu";

export type { SidebarMenuRow };

export interface SidebarMenuPort {
  fetchAccessibleAdminMenus(): Promise<SidebarMenuRow[]>;
}
