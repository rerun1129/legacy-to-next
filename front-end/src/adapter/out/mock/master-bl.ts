import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlDetail, MasterBlFilter, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';
import { NotFoundError } from '@/adapter/out/api/errors';

// Master B/L fixture data: empty until backend integration is completed
const masterBlRows: MasterBlRow[] = [];

export const mockMasterBlPort: MasterBlPort = {
  async list(_filter: MasterBlFilter): Promise<MasterBlRow[]> {
    return masterBlRows;
  },
  async getById(id: number): Promise<MasterBlRow> {
    const row = masterBlRows.find((r) => r.id === id);
    if (!row) throw new NotFoundError('MasterBl', String(id));
    return row;
  },
  async create(req: CreateMasterBlRequest): Promise<MasterBlDetail> {
    return {
      id: 0,
      mblNo: req.mblNo ?? null,
      masterRefNo: req.masterRefNo ?? null,
      jobDiv: req.jobDiv,
      bound: req.bound,
      freightTerm: req.freightTerm,
      shipperCode: req.shipperCode ?? null,
      consigneeCode: req.consigneeCode ?? null,
      polCode: req.polCode ?? null,
      podCode: req.podCode ?? null,
      etd: req.etd ?? null,
      eta: req.eta ?? null,
      pkgQty: req.pkgQty ?? null,
      grossWeightKg: req.grossWeightKg ?? null,
      cbm: req.cbm ?? null,
      operatorCode: req.operatorCode ?? null,
      consolidatedHouseBls: [],
      createdAt: new Date().toISOString(),
      updatedAt: null,
    };
  },
  async update(id: number, req: UpdateMasterBlRequest): Promise<MasterBlDetail> {
    const row = masterBlRows.find((r) => r.id === id);
    if (!row) throw new NotFoundError('MasterBl', String(id));
    return {
      ...row,
      freightTerm: req.freightTerm ?? null,
      pkgQty: req.pkgQty ?? null,
      grossWeightKg: req.grossWeightKg ?? null,
      cbm: req.cbm ?? null,
      consolidatedHouseBls: [],
      updatedAt: new Date().toISOString(),
      ...req,
    };
  },
  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
