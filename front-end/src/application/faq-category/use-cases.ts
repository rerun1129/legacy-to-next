import { faqCategoryPort } from "@/lib/ports";
import type { CreateFaqCategoryRequestDto, UpdateFaqCategoryRequestDto } from "@/domain/faq-category";

export const faqCategoryUseCases = {
  search: () => faqCategoryPort.search(),
  getById: (id: number) => faqCategoryPort.getById(id),
  create: (req: CreateFaqCategoryRequestDto) => faqCategoryPort.create(req),
  update: (id: number, req: UpdateFaqCategoryRequestDto) => faqCategoryPort.update(id, req),
  delete: (id: number) => faqCategoryPort.delete(id),
};
