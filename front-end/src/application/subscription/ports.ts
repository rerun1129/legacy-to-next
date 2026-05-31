import type {
  SubscriptionItem,
  SaveSubscriptionChangesRequestDto,
  SaveSubscriptionChangesResultDto,
} from "@/domain/subscription";

export interface SubscriptionPort {
  listBySubscriber(subscriberId: number): Promise<SubscriptionItem[]>;
  saveChanges(
    subscriberId: number,
    req: SaveSubscriptionChangesRequestDto,
  ): Promise<SaveSubscriptionChangesResultDto>;
}
