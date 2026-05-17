import { partnerPort } from "@/lib/ports";
import type { PartnerFilter, CreatePartnerRequestDto, UpdatePartnerRequestDto } from "@/domain/partner";

export const partnerUseCases = {
  search: (filter: PartnerFilter, page: number, size?: number) => partnerPort.search(filter, page, size),
  getById: (id: number) => partnerPort.getById(id),
  create: (req: CreatePartnerRequestDto) => partnerPort.create(req),
  update: (id: number, req: UpdatePartnerRequestDto) => partnerPort.update(id, req),
  delete: (id: number) => partnerPort.delete(id),
};
