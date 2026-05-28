import { userPort } from "@/lib/ports";
import type { UserFilter, CreateUserRequestDto, UpdateUserRequestDto, SaveUserChangesRequestDto } from "@/domain/user";

export const userUseCases = {
  search: (filter: UserFilter, page: number, size?: number) => userPort.search(filter, page, size),
  getById: (id: number) => userPort.getById(id),
  create: (req: CreateUserRequestDto) => userPort.create(req),
  update: (id: number, req: UpdateUserRequestDto) => userPort.update(id, req),
  delete: (id: number) => userPort.delete(id),
  deleteMany: (ids: number[]) => userPort.deleteMany(ids),
  saveChanges: (req: SaveUserChangesRequestDto) => userPort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => userPort.autocomplete(q, limit),
};
