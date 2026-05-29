import type {
  MenuRow,
  MenuDetail,
  SaveMenuChangesRequest,
  SaveChangesResult,
  MenuAutocompleteItem,
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
  saveChanges(req: SaveMenuChangesRequest): Promise<SaveChangesResult>;
  autocomplete(query: string): Promise<MenuAutocompleteItem[]>;
}
