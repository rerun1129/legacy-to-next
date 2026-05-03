import type { SwitchBl, CreateSwitchBlRequest, UpdateSwitchBlRequest } from '@/domain/switch-bl';

export interface SwitchBlPort {
  getByHouseBlId(houseBlId: number): Promise<SwitchBl | null>;
  create(req: CreateSwitchBlRequest): Promise<SwitchBl>;
  update(id: number, req: UpdateSwitchBlRequest): Promise<SwitchBl>;
  delete(id: number): Promise<void>;
}
