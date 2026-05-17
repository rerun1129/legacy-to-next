import type {
  FaqCategoryRow,
  FaqCategoryDetail,
  CreateFaqCategoryRequestDto,
  UpdateFaqCategoryRequestDto,
} from "@/domain/faq-category";

export interface FaqCategoryPort {
  search(): Promise<FaqCategoryRow[]>;
  getById(id: number): Promise<FaqCategoryDetail>;
  create(req: CreateFaqCategoryRequestDto): Promise<number>;
  update(id: number, req: UpdateFaqCategoryRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
}
