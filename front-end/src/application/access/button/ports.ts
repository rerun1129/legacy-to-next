import type {
  ButtonRow,
  ButtonDetail,
  SaveButtonChangesRequest,
  SaveChangesResult,
  ButtonAutocompleteItem,
} from "@/domain/access/button";

export interface ButtonPageResult {
  content: ButtonRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface ButtonPort {
  search(page: number, size?: number): Promise<ButtonPageResult>;
  getById(id: number): Promise<ButtonDetail>;
  saveChanges(req: SaveButtonChangesRequest): Promise<SaveChangesResult>;
  autocomplete(query: string): Promise<ButtonAutocompleteItem[]>;
}
