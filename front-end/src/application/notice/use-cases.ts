import { noticePort } from "@/lib/ports";
import type { NoticeFilter, CreateNoticeRequestDto, UpdateNoticeRequestDto } from "@/domain/notice";

export const noticeUseCases = {
  search: (filter: NoticeFilter, page: number, size?: number) => noticePort.search(filter, page, size),
  getById: (id: number) => noticePort.getById(id),
  create: (req: CreateNoticeRequestDto) => noticePort.create(req),
  update: (id: number, req: UpdateNoticeRequestDto) => noticePort.update(id, req),
  delete: (id: number) => noticePort.delete(id),
  deleteMany: (ids: number[]) => noticePort.deleteMany(ids),
};
