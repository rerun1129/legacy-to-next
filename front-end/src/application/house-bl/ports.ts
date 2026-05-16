import type { HouseBlRow, HouseBlDetail, HouseBlFilter, CreateHouseBlRequest, UpdateHouseBlRequest, JobDiv } from '@/domain/house-bl';

export interface HouseBlPort {
  list(filter: HouseBlFilter): Promise<HouseBlRow[]>;
  getById(id: number): Promise<HouseBlDetail>;
  save(data: unknown): Promise<HouseBlDetail>;
  create(req: CreateHouseBlRequest): Promise<{ id: number }>;
  // §BE 통일 — 모든 jobDiv에서 ApiResponse<Void> 반환
  update(id: number, req: UpdateHouseBlRequest): Promise<void>;
  delete(id: number): Promise<void>;
  changeHblNo(id: number, hblNo: string): Promise<void>;
  // EXACT 매칭 — POST /api/house-bl/find-by-hbl-no
  findByHblNo(hblNo: string, jobDiv: JobDiv): Promise<number[]>;
}
