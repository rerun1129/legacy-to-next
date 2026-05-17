import { termsPort } from "@/lib/ports";
import type { TermsFilter, CreateTermsRequestDto, UpdateTermsRequestDto } from "@/domain/terms";

export const termsUseCases = {
  search: (filter: TermsFilter, page: number, size?: number) => termsPort.search(filter, page, size),
  getById: (id: number) => termsPort.getById(id),
  create: (req: CreateTermsRequestDto) => termsPort.create(req),
  update: (id: number, req: UpdateTermsRequestDto) => termsPort.update(id, req),
  delete: (id: number) => termsPort.delete(id),
};
