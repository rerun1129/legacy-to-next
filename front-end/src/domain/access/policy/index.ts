export interface MenuPolicyRow {
  id: number;
  menuId: number;
  attributeKey: string;
  requiredValue: string;
  updatedAt: string;
}

export interface ButtonPolicyRow {
  id: number;
  buttonId: number;
  attributeKey: string;
  requiredValue: string;
  updatedAt: string;
}

export interface CreateMenuPolicyDto {
  menuId: number;
  attributeKey: string;
  requiredValue: string;
}

export interface CreateButtonPolicyDto {
  buttonId: number;
  attributeKey: string;
  requiredValue: string;
}
