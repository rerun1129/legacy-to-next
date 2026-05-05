import type { NonBlRow, NonBlFilter } from '@/domain/non-bl';

export interface NonBlPort {
  list(filter: NonBlFilter): Promise<NonBlRow[]>;
}
