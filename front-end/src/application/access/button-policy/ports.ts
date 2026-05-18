import type { ButtonPolicyRow, CreateButtonPolicyDto } from "@/domain/access/policy";

export interface ButtonPolicyPort {
  listByButton(buttonId: number): Promise<ButtonPolicyRow[]>;
  create(req: CreateButtonPolicyDto): Promise<number>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
