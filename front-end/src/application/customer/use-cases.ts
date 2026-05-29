import { customerPort } from "@/lib/ports";
import type { CustomerFilter, CreateCustomerRequestDto, UpdateCustomerRequestDto, SaveCustomerChangesRequestDto } from "@/domain/customer";

export const customerUseCases = {
  search: (filter: CustomerFilter, page: number, size?: number) => customerPort.search(filter, page, size),
  getById: (id: number) => customerPort.getById(id),
  create: (req: CreateCustomerRequestDto) => customerPort.create(req),
  update: (id: number, req: UpdateCustomerRequestDto) => customerPort.update(id, req),
  delete: (id: number) => customerPort.delete(id),
  deleteMany: (ids: number[]) => customerPort.deleteMany(ids),
  saveChanges: (req: SaveCustomerChangesRequestDto) => customerPort.saveChanges(req),
  autocomplete: (q: string, limit?: number, type?: string) => customerPort.autocomplete(q, limit, type),
};
