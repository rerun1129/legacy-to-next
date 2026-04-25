import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlFilter } from '@/domain/house-bl';

export const apiHouseBlPort: HouseBlPort = {
  async list(filter: HouseBlFilter): Promise<HouseBlRow[]> {
    const params = new URLSearchParams(filter as Record<string, string>);
    const res = await fetch(`/api/v1/house-bl?${params}`);
    if (!res.ok) throw new Error('Failed to fetch house B/L list');
    const json = await res.json();
    return json.data?.content ?? [];
  },
  async getById(id: string): Promise<HouseBlRow> {
    const res = await fetch(`/api/v1/house-bl/${id}`);
    if (!res.ok) throw new Error(`Failed to fetch house B/L: ${id}`);
    const json = await res.json();
    return json.data;
  },
  async save(data): Promise<HouseBlRow> {
    const res = await fetch('/api/v1/house-bl', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw new Error('Failed to save house B/L');
    const json = await res.json();
    return json.data;
  },
  async delete(id: string): Promise<void> {
    const res = await fetch(`/api/v1/house-bl/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(`Failed to delete house B/L: ${id}`);
  },
};
