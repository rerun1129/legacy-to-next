import type { FinancialDocumentSearchRow, SearchFinancialDocumentInput } from "@/application/bms/financial-document/ports";

export interface FinancialDocumentGroupModalProps {
  isOpen: boolean;
  onClose: () => void;
  /** 그룹 모달에 들어온 선택된 서류 행들 (scopeDocumentIds 전체) */
  rows: FinancialDocumentSearchRow[];
  searchFilter: SearchFinancialDocumentInput | null;
  page: number;
  pageSize: number;
  onGroupSuccess: () => void;
}
