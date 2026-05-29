import { accessMenuPort } from "@/lib/ports";
import type { SaveMenuChangesRequest } from "@/domain/access/menu";

export const accessMenuUseCases = {
  search: (page: number, size?: number) => accessMenuPort.search(page, size),
  getById: (id: number) => accessMenuPort.getById(id),
  saveChanges: (req: SaveMenuChangesRequest) => accessMenuPort.saveChanges(req),
  autocomplete: (q: string) => accessMenuPort.autocomplete(q),
};
