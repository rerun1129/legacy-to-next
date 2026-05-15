import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlDetail, MasterBlFilter, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';
import { NotFoundError } from '@/adapter/out/api/errors';

// Master B/L fixture data: basic rows for filter/search smoke test
const masterBlRows: MasterBlRow[] = [
  { id: 1, mblNo: 'MBLKR24041956', masterRefNo: null, jobDiv: 'SEA', bound: 'EXP', shipperCode: 'HANJIN', consigneeCode: 'SHANGHAI_TRADING', polCode: 'KRBSAN', podCode: 'CNSHA', etd: '20240420', eta: null, operatorCode: null, createdAt: '2024-04-20' },
  { id: 2, mblNo: 'MBLKR24041901', masterRefNo: null, jobDiv: 'SEA', bound: 'EXP', shipperCode: 'SAMSUNG',  consigneeCode: 'SAMSUNG_EU',        polCode: 'KRICN',  podCode: 'DEHAM', etd: '20240419', eta: null, operatorCode: null, createdAt: '2024-04-19' },
  { id: 3, mblNo: 'MBLKR24041623', masterRefNo: null, jobDiv: 'SEA', bound: 'IMP', shipperCode: 'NINGBO',   consigneeCode: 'KR_IMPORT',          polCode: 'CNNGB',  podCode: 'KRICN', etd: '20240416', eta: null, operatorCode: null, createdAt: '2024-04-16' },
];

// SEA detail 샘플 (§BE-sync — SeaDetailProjection 16 필드 정합)
const seaDetailSample = {
  loadType: 'FCL',
  linerCode: 'HMM',
  vesselCode: 'HANJIN001',
  vesselName: 'HANJIN BUSAN',
  voyageNo: '024E',
  onboardDate: '20240420',
  vesselNationality: 'KR',
  serviceTerm: 'CY/CY',
  blType: 'OBL',
  porCode: 'KRBSAN',
  finalDestCode: 'CNSHA',
  rton: null,
  lineBkgNo: 'BKG240419001',
  issueDate: '20240421',
  desc: {
    marks: 'MARKS AND NUMBERS',
    description: 'FREIGHT ALL KINDS',
    descClause1: undefined,
    descClause2: undefined,
  },
  remark: 'SEA remark sample',
};

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
    return {
      ...row,
      shipmentType: 'HOUSE',
      freightTerm: 'PREPAID',
      pkgQty: 10,
      weightUnit: 'KG',
      grossWeightKg: 1000,
      cbm: 5,
      consolidatedHouseBls: [],
      consoledSeaContainers: [],
      updatedAt: null,
      remark: undefined,
      teamCode: null,
      shipperAddress: 'BUSAN, KOREA',
      consigneeAddress: 'SHANGHAI, CHINA',
      notifyCode: null,
      notifyAddress: null,
      seaDetail: row.jobDiv === 'SEA' ? seaDetailSample : null,
    };
  },

  // §6.54 — create는 ID-only 반환 (BE Phase 3 정합)
  async create(_req: CreateMasterBlRequest): Promise<{ id: number }> {
    return { id: 9999 };
  },

  async update(id: number, _req: UpdateMasterBlRequest): Promise<void> {
    const row = masterBlRows.find((r) => r.id === id);
    if (!row) throw new NotFoundError('MasterBl', String(id));
    // mock: no-op (void)
  },

  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
