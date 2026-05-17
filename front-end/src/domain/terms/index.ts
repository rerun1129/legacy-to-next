export type TermsType = "TOS" | "PRIVACY" | "MARKETING";
export type TermsScope = "ALL" | "ACTIVE" | "DELETED";

export interface TermsRow {
  id: number;
  type: TermsType;
  version: number;
  effectiveAt: string;
  summary: string | null;
  deletedAt: string | null;
  updatedAt: string;
}

export interface TermsDetail {
  id: number;
  type: TermsType;
  version: number;
  effectiveAt: string;
  content: string;
  summary: string | null;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface TermsFilter {
  type: TermsType | "ALL";
  scope: TermsScope;
  version: number | "";
  summary: string;
}

export interface CreateTermsRequestDto {
  type: TermsType;
  version: number;
  effectiveAt: string;
  content: string;
  summary: string | null;
}

export interface UpdateTermsRequestDto {
  content: string;
  summary: string | null;
  effectiveAt: string;
}
