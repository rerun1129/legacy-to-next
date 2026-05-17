export type FaqScope = "ALL" | "ACTIVE" | "DELETED";

export interface FaqRow {
  id: number;
  faqCategoryId: number;
  question: string;
  sortOrder: number;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface FaqDetail {
  id: number;
  faqCategoryId: number;
  question: string;
  answer: string;
  sortOrder: number;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface FaqFilter {
  faqCategoryId: number | null;
  question: string;
  scope: FaqScope;
}

export interface CreateFaqRequestDto {
  faqCategoryId: number;
  question: string;
  answer: string;
  sortOrder: number;
  active: boolean;
}

export interface UpdateFaqRequestDto {
  faqCategoryId: number;
  question: string;
  answer: string;
  sortOrder: number;
  active: boolean;
}
