import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlDetail, MasterBlFilter, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';
import { NotFoundError } from '@/adapter/out/api/errors';

// Master B/L fixture data: basic rows for filter/search smoke test
const masterBlRows: MasterBlRow[] = [
  { id: 1, mblNo: 'MBLKR24041956', masterRefNo: null, jobDiv: 'SEA', bound: 'EXP', shipperCode: 'HANJIN', consigneeCode: 'SHANGHAI_TRADING', polCode: 'KRBSAN', podCode: 'CNSHA', etd: '2024-04-20', eta: null, operatorCode: null, createdAt: '2024-04-20' },
  { id: 2, mblNo: 'MBLKR24041901', masterRefNo: null, jobDiv: 'SEA', bound: 'EXP', shipperCode: 'SAMSUNG',  consigneeCode: 'SAMSUNG_EU',        polCode: 'KRICN',  podCode: 'DEHAM', etd: '2024-04-19', eta: null, operatorCode: null, createdAt: '2024-04-19' },
  { id: 3, mblNo: 'MBLKR24041623', masterRefNo: null, jobDiv: 'SEA', bound: 'IMP', shipperCode: 'NINGBO',   consigneeCode: 'KR_IMPORT',          polCode: 'CNNGB',  podCode: 'KRICN', etd: '2024-04-16', eta: null, operatorCode: null, createdAt: '2024-04-16' },
];

export const mockMasterBlPort: MasterBlPort = {
  async list(filter: MasterBlFilter): Promise<MasterBlRow[]> {
    let rows = filter.jobDiv
      ? masterBlRows.filter((r) => r.jobDiv === filter.jobDiv && r.bound === filter.bound)
      : masterBlRows.filter((r) => r.bound === filter.bound);
    if (filter.mblNo) {
      rows = rows.filter((r) => r.mblNo?.toLowerCase().includes(filter.mblNo!.toLowerCase()));
    }
    if (filter.shipperCode) {
      rows = rows.filter((r) => r.shipperCode?.toLowerCase().includes(filter.shipperCode!.toLowerCase()));
    }
    if (filter.consigneeCode) {
      rows = rows.filter((r) => r.consigneeCode?.toLowerCase().includes(filter.consigneeCode!.toLowerCase()));
    }
    if (filter.polCode) {
      rows = rows.filter((r) => r.polCode === filter.polCode);
    }
    if (filter.podCode) {
      rows = rows.filter((r) => r.podCode === filter.podCode);
    }
    if (filter.etdFrom) {
      rows = rows.filter((r) => r.etd != null && r.etd >= filter.etdFrom!);
    }
    if (filter.etdTo) {
      rows = rows.filter((r) => r.etd != null && r.etd <= filter.etdTo!);
    }
    return rows;
  },
  async getById(id: number): Promise<MasterBlDetail> {
    const row = masterBlRows.find((r) => r.id === id);
    if (!row) throw new NotFoundError('MasterBl', String(id));
    return row as unknown as MasterBlDetail;
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
