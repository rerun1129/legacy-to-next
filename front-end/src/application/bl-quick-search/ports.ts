import type { BlQuickSearchFilters, BlQuickSearchItem } from "@/domain/bl-quick-search";

export interface BlQuickSearchPort {
  autocomplete(
    q: string,
    filters: BlQuickSearchFilters,
    limit?: number
  ): Promise<BlQuickSearchItem[]>;
}
