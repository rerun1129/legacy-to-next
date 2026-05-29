import { accessButtonPort } from "@/lib/ports";
import type { SaveButtonChangesRequest } from "@/domain/access/button";

export const accessButtonUseCases = {
  search: (page: number, size?: number) => accessButtonPort.search(page, size),
  getById: (id: number) => accessButtonPort.getById(id),
  saveChanges: (req: SaveButtonChangesRequest) => accessButtonPort.saveChanges(req),
  autocomplete: (query: string) => accessButtonPort.autocomplete(query),
};
