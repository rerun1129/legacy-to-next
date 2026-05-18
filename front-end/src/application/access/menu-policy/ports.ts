import type { MenuPolicyRow, CreateMenuPolicyDto } from "@/domain/access/policy";

export interface MenuPolicyPort {
  listByMenu(menuId: number): Promise<MenuPolicyRow[]>;
  create(req: CreateMenuPolicyDto): Promise<number>;
  delete(id: number): Promise<void>;
}
