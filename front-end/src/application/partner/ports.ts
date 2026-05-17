import type {
  PartnerRow,
  PartnerDetail,
  PartnerFilter,
  CreatePartnerRequestDto,
  UpdatePartnerRequestDto,
} from "@/domain/partner";

export interface PartnerPageResult {
  content: PartnerRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface PartnerPort {
  search(filter: PartnerFilter, page: number, size?: number): Promise<PartnerPageResult>;
  getById(id: number): Promise<PartnerDetail>;
  create(req: CreatePartnerRequestDto): Promise<number>;
  update(id: number, req: UpdatePartnerRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
}
