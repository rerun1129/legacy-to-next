import type {
  ButtonRow,
  ButtonDetail,
  CreateButtonDto,
  UpdateButtonDto,
} from "@/domain/access/button";

export interface ButtonPageResult {
  content: ButtonRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface ButtonPort {
  search(page: number, size?: number): Promise<ButtonPageResult>;
  getById(id: number): Promise<ButtonDetail>;
  create(req: CreateButtonDto): Promise<number>;
  update(id: number, req: UpdateButtonDto): Promise<void>;
  delete(id: number): Promise<void>;
}
