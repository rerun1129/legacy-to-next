import { portPort } from "@/lib/ports";
import type { PortFilter, CreatePortRequestDto, UpdatePortRequestDto, SavePortChangesRequestDto } from "@/domain/code/port";

export const portUseCases = {
  search: (filter: PortFilter, page: number, size?: number) => portPort.search(filter, page, size),
  getById: (id: number) => portPort.getById(id),
  create: (req: CreatePortRequestDto) => portPort.create(req),
  update: (id: number, req: UpdatePortRequestDto) => portPort.update(id, req),
  delete: (id: number) => portPort.delete(id),
  deleteMany: (ids: number[]) => portPort.deleteMany(ids),
  saveChanges: (req: SavePortChangesRequestDto) => portPort.saveChanges(req),
  autocomplete: (q: string, limit?: number, type?: string) => portPort.autocomplete(q, limit, type),
};
