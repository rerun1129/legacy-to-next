import type { SwitchBlPort } from '@/application/switch-bl/ports';
import type { SwitchBl, CreateSwitchBlRequest, UpdateSwitchBlRequest } from '@/domain/switch-bl';

// mock: 인메모리 스토어 (houseBlId 기준 단건)
const store = new Map<number, SwitchBl>();

export const mockSwitchBlPort: SwitchBlPort = {
  async getByHouseBlId(houseBlId: number): Promise<SwitchBl | null> {
    return store.get(houseBlId) ?? null;
  },

  async create(req: CreateSwitchBlRequest): Promise<SwitchBl> {
    const record: SwitchBl = {
      id: Date.now(),
      houseBlId: req.houseBlId,
      switchBlNo: req.switchBlNo ?? null,
      shipperCode: req.shipperCode,
      shipperAddress: req.shipperAddress ?? null,
      consigneeCode: req.consigneeCode ?? null,
      consigneeAddress: req.consigneeAddress ?? null,
      notifyCode: req.notifyCode ?? null,
      notifyAddress: req.notifyAddress ?? null,
      description: req.description ?? null,
    };
    store.set(req.houseBlId, record);
    return record;
  },

  async update(id: number, req: UpdateSwitchBlRequest): Promise<SwitchBl> {
    const existing = [...store.values()].find((r) => r.id === id);
    if (!existing) throw new Error(`SwitchBl not found: ${id}`);
    const updated: SwitchBl = { ...existing, ...req };
    store.set(existing.houseBlId, updated);
    return updated;
  },

  async delete(id: number): Promise<void> {
    const key = [...store.entries()].find(([, v]) => v.id === id)?.[0];
    if (key !== undefined) store.delete(key);
  },
};
