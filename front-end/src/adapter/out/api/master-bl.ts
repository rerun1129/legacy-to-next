import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';

function toSearchParams(filter: MasterBlFilter): URLSearchParams {
  return Object.entries(filter)
    .filter(([, v]) => v != null)
    .reduce((p, [k, v]) => { p.set(k, String(v)); return p; }, new URLSearchParams());
}

export const apiMasterBlPort: MasterBlPort = {
  async list(filter: MasterBlFilter): Promise<MasterBlRow[]> {
    const res = await fetch(`/api/v1/master-bl?${toSearchParams(filter)}`);
    if (!res.ok) throw new Error('Failed to fetch master B/L list');
    const json = await res.json();
    if (!Array.isArray(json.data?.content)) {
      throw new Error('Unexpected response: data.content is not an array');
    }
    return json.data.content as MasterBlRow[];
  },
  async getById(id: string): Promise<MasterBlRow> {
    const res = await fetch(`/api/v1/master-bl/${id}`);
    if (!res.ok) throw new Error(`Failed to fetch master B/L: ${id}`);
    const json = await res.json();
    if (!json.data) throw new Error('Unexpected response: data is missing');
    return json.data as MasterBlRow;
  },
  async delete(id: string): Promise<void> {
    const res = await fetch(`/api/v1/master-bl/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(`Failed to delete master B/L: ${id}`);
  },
};
