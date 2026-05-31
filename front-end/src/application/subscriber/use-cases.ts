import { subscriberPort } from "@/lib/ports";
import type {
  SubscriberFilter,
  CreateSubscriberRequestDto,
  UpdateSubscriberRequestDto,
  SaveSubscriberChangesRequestDto,
} from "@/domain/subscriber";

export const subscriberUseCases = {
  search: (filter: SubscriberFilter, page: number, size?: number) =>
    subscriberPort.search(filter, page, size),
  listAll: () => subscriberPort.listAll(),
  getById: (id: number) => subscriberPort.getById(id),
  create: (req: CreateSubscriberRequestDto) => subscriberPort.create(req),
  update: (id: number, req: UpdateSubscriberRequestDto) => subscriberPort.update(id, req),
  delete: (id: number) => subscriberPort.delete(id),
  saveChanges: (req: SaveSubscriberChangesRequestDto) => subscriberPort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => subscriberPort.autocomplete(q, limit),
};
