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

export interface CreateButtonDto {
  menuId: number;
  buttonCode: string;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string | null;
  apiPath: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateButtonDto {
  menuId: number;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string | null;
  apiPath: string | null;
  sortOrder: number | null;
  active: boolean;
}
