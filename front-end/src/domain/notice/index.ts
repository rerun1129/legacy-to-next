export type NoticeScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export type NoticePinnedFilter = "ALL" | "PINNED" | "UNPINNED";

export interface NoticeRow {
  id: number;
  title: string;
  pinned: boolean;
  active: boolean;
  publishedAt: string | null;
  expiresAt: string | null;
  deletedAt: string | null;
  updatedAt: string;
}

export interface NoticeDetail {
  id: number;
  title: string;
  content: string;
  pinned: boolean;
  active: boolean;
  publishedAt: string | null;
  expiresAt: string | null;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface NoticeFilter {
  title: string;
  pinned: NoticePinnedFilter;
  scope: NoticeScope;
  publishedOnly: boolean;
}

export interface CreateNoticeRequestDto {
  title: string;
  content: string;
  pinned: boolean;
  active: boolean;
  publishedAt: string | null;
  expiresAt: string | null;
}

export interface UpdateNoticeRequestDto {
  title: string;
  content: string;
  pinned: boolean;
  active: boolean;
  publishedAt: string | null;
  expiresAt: string | null;
}
