import { faqPort } from "@/lib/ports";
import type { FaqFilter, CreateFaqRequestDto, UpdateFaqRequestDto } from "@/domain/faq";

export const faqUseCases = {
  search: (filter: FaqFilter, page: number, size?: number) => faqPort.search(filter, page, size),
  getById: (id: number) => faqPort.getById(id),
  create: (req: CreateFaqRequestDto) => faqPort.create(req),
  update: (id: number, req: UpdateFaqRequestDto) => faqPort.update(id, req),
  delete: (id: number) => faqPort.delete(id),
};
