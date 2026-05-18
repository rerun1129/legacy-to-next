import type {
  NoticeRow,
  NoticeDetail,
  NoticeFilter,
  CreateNoticeRequestDto,
  UpdateNoticeRequestDto,
} from "@/domain/notice";

export interface NoticePageResult {
  content: NoticeRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface NoticePort {
  search(filter: NoticeFilter, page: number, size?: number): Promise<NoticePageResult>;
  getById(id: number): Promise<NoticeDetail>;
  create(req: CreateNoticeRequestDto): Promise<number>;
  update(id: number, req: UpdateNoticeRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
