// BE AccessibleMenuResponse 필드와 1:1 대응
export interface SidebarMenuRow {
  id: number;
  menuCode: string;
  parentId: number | null;
  path: string | null;
  label: string;
  labelEn: string | null;
  icon: string | null;
  sortOrder: number;
  moduleCode: string;
}
