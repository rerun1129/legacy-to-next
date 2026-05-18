import type {
  MenuRow,
  MenuDetail,
  CreateMenuDto,
  UpdateMenuDto,
} from "@/domain/access/menu";

export interface MenuPageResult {
  content: MenuRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface MenuPort {
  search(page: number, size?: number): Promise<MenuPageResult>;
  getById(id: number): Promise<MenuDetail>;
  create(req: CreateMenuDto): Promise<number>;
  update(id: number, req: UpdateMenuDto): Promise<void>;
  delete(id: number): Promise<void>;
}
