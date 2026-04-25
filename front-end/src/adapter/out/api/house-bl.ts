import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlFilter } from '@/domain/house-bl';

function toSearchParams(filter: HouseBlFilter): URLSearchParams {
  return Object.entries(filter)
    .filter(([, v]) => v != null)
    .reduce((p, [k, v]) => { p.set(k, String(v)); return p; }, new URLSearchParams());
}

export const apiHouseBlPort: HouseBlPort = {
  async list(filter: HouseBlFilter): Promise<HouseBlRow[]> {
    const res = await fetch(`/api/v1/house-bl?${toSearchParams(filter)}`);
    if (!res.ok) throw new Error('Failed to fetch house B/L list');
    const json = await res.json();
    if (!Array.isArray(json.data?.content)) {
      throw new Error('Unexpected response: data.content is not an array');
    }
    return json.data.content as HouseBlRow[];
  },
  async getById(id: string): Promise<HouseBlRow> {
    const res = await fetch(`/api/v1/house-bl/${id}`);
    if (!res.ok) throw new Error(`Failed to fetch house B/L: ${id}`);
    const json = await res.json();
    if (!json.data) throw new Error('Unexpected response: data is missing');
    return json.data as HouseBlRow;
  },
  async save(data): Promise<HouseBlRow> {
    const res = await fetch('/api/v1/house-bl', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw new Error('Failed to save house B/L');
    const json = await res.json();
    if (!json.data) throw new Error('Unexpected response: data is missing');
    return json.data as HouseBlRow;
  },
  async delete(id: string): Promise<void> {
    const res = await fetch(`/api/v1/house-bl/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(`Failed to delete house B/L: ${id}`);
  },
};
