import { userPort } from "@/lib/ports";
import type { UserFilter, CreateUserRequestDto, UpdateUserRequestDto } from "@/domain/user";

export const userUseCases = {
  search: (filter: UserFilter, page: number, size?: number) => userPort.search(filter, page, size),
  getById: (id: number) => userPort.getById(id),
  create: (req: CreateUserRequestDto) => userPort.create(req),
  update: (id: number, req: UpdateUserRequestDto) => userPort.update(id, req),
  delete: (id: number) => userPort.delete(id),
  deleteMany: (ids: number[]) => userPort.deleteMany(ids),
};
