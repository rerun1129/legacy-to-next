import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';

export const apiMasterBlPort: MasterBlPort = {
  async list(filter: MasterBlFilter): Promise<MasterBlRow[]> {
    const params = new URLSearchParams(filter as Record<string, string>);
    const res = await fetch(`/api/v1/master-bl?${params}`);
    if (!res.ok) throw new Error('Failed to fetch master B/L list');
    const json = await res.json();
    return json.data?.content ?? [];
  },
  async getById(id: string): Promise<MasterBlRow> {
    const res = await fetch(`/api/v1/master-bl/${id}`);
    if (!res.ok) throw new Error(`Failed to fetch master B/L: ${id}`);
    const json = await res.json();
    return json.data;
  },
  async delete(id: string): Promise<void> {
    const res = await fetch(`/api/v1/master-bl/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(`Failed to delete master B/L: ${id}`);
  },
};
