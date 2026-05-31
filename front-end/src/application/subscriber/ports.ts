import type {
  SubscriberRow,
  SubscriberDetail,
  SubscriberFilter,
  CreateSubscriberRequestDto,
  UpdateSubscriberRequestDto,
  SaveSubscriberChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/subscriber";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface SubscriberPageResult {
  content: SubscriberRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface SubscriberPort {
  search(filter: SubscriberFilter, page: number, size?: number): Promise<SubscriberPageResult>;
  listAll(): Promise<SubscriberRow[]>;
  getById(id: number): Promise<SubscriberDetail>;
  create(req: CreateSubscriberRequestDto): Promise<number>;
  update(id: number, req: UpdateSubscriberRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  saveChanges(req: SaveSubscriberChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
