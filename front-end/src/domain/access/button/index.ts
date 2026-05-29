export type ButtonActionType = "CREATE" | "UPDATE" | "DELETE" | "EXPORT" | "CUSTOM";

export interface ButtonRow {
  id: number;
  menuId: number;
  buttonCode: string;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string | null;
  apiPath: string | null;
  sortOrder: number | null;
  active: boolean;
  updatedAt: string;
}

export interface ButtonDetail extends ButtonRow {
  createdAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

// save-changes DTOs (batch inline edit)

export interface CreateButtonItem {
  buttonCode: string;
  menuId: number;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string | null;
  apiPath: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateButtonItem {
  id: number;
  menuId: number;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string | null;
  apiPath: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface SaveButtonChangesRequest {
  creates: CreateButtonItem[];
  updates: UpdateButtonItem[];
}

// 공용 타입 — menu와 동일 구조
export interface SaveChangesResult {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}

export interface ButtonAutocompleteItem {
  code: string;
  name: string;
}
