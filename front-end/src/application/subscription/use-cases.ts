import { subscriptionPort } from "@/lib/ports";
import type { SaveSubscriptionChangesRequestDto } from "@/domain/subscription";

export const subscriptionUseCases = {
  listBySubscriber: (subscriberId: number) =>
    subscriptionPort.listBySubscriber(subscriberId),
  saveChanges: (subscriberId: number, req: SaveSubscriptionChangesRequestDto) =>
    subscriptionPort.saveChanges(subscriberId, req),
};
