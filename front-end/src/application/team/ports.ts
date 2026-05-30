import type { TeamRow } from "@/domain/team";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface TeamPort {
  listAll(): Promise<TeamRow[]>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
