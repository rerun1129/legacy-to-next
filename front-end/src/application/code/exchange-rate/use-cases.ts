import { exchangeRatePort } from "@/lib/ports";
import type { ExchangeRateFilter, CreateExchangeRateRequestDto, UpdateExchangeRateRequestDto, SaveExchangeRateChangesRequestDto } from "@/domain/code/exchange-rate";

export const exchangeRateUseCases = {
  search: (filter: ExchangeRateFilter, page: number, size?: number) => exchangeRatePort.search(filter, page, size),
  getById: (id: number) => exchangeRatePort.getById(id),
  create: (req: CreateExchangeRateRequestDto) => exchangeRatePort.create(req),
  update: (id: number, req: UpdateExchangeRateRequestDto) => exchangeRatePort.update(id, req),
  delete: (id: number) => exchangeRatePort.delete(id),
  deleteMany: (ids: number[]) => exchangeRatePort.deleteMany(ids),
  saveChanges: (req: SaveExchangeRateChangesRequestDto) => exchangeRatePort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => exchangeRatePort.autocomplete(q, limit),
  findRatesByDateCurrency: (exchangeDate: string, fromCurrencyCode: string, toCurrencyCode: string) =>
    exchangeRatePort.findRatesByDateCurrency(exchangeDate, fromCurrencyCode, toCurrencyCode),
};
