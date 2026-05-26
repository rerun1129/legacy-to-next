import { currencyPort } from "@/lib/ports";
import type { CurrencyFilter, CreateCurrencyRequestDto, UpdateCurrencyRequestDto } from "@/domain/code/currency";

export const currencyUseCases = {
  search: (filter: CurrencyFilter, page: number, size?: number) => currencyPort.search(filter, page, size),
  getById: (id: number) => currencyPort.getById(id),
  create: (req: CreateCurrencyRequestDto) => currencyPort.create(req),
  update: (id: number, req: UpdateCurrencyRequestDto) => currencyPort.update(id, req),
  delete: (id: number) => currencyPort.delete(id),
  deleteMany: (ids: number[]) => currencyPort.deleteMany(ids),
};
