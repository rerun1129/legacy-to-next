import { teamPort } from "@/lib/ports";

export const teamUseCases = {
  listAll: () => teamPort.listAll(),
  autocomplete: (q: string, limit?: number) => teamPort.autocomplete(q, limit),
};
