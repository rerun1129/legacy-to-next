import { countryPort } from "@/lib/ports";
import type { CountryFilter, CreateCountryRequestDto, UpdateCountryRequestDto } from "@/domain/code/country";

export const countryUseCases = {
  search: (filter: CountryFilter, page: number, size?: number) => countryPort.search(filter, page, size),
  getById: (id: number) => countryPort.getById(id),
  create: (req: CreateCountryRequestDto) => countryPort.create(req),
  update: (id: number, req: UpdateCountryRequestDto) => countryPort.update(id, req),
  delete: (id: number) => countryPort.delete(id),
  deleteMany: (ids: number[]) => countryPort.deleteMany(ids),
};
