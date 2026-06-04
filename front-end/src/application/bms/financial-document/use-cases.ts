import type { QueryOptions } from "@tanstack/react-query";
import { financialDocumentPort } from "@/lib/ports";
import type { IssueDocumentInput } from "./ports";

// === 쿼리키 팩토리 ===

export const financialDocumentKeys = {
  all: ["bms", "financial-documents"] as const,
  listByBl: (blType: string, blId: string) =>
    [...financialDocumentKeys.all, "list", blType, blId] as const,
  issuableLines: (blType: string, blId: string, freightType: string) =>
    [...financialDocumentKeys.all, "issuable-lines", blType, blId, freightType] as const,
};

// === Query Options ===

export function listByBlQueryOptions(blType: string, blId: string): QueryOptions {
  return {
    queryKey: financialDocumentKeys.listByBl(blType, blId),
    queryFn: () => financialDocumentPort.listByBl(blType, blId),
  };
}

export function issuableLinesQueryOptions(
  blType: string,
  blId: string,
  freightType: string
): QueryOptions {
  return {
    queryKey: financialDocumentKeys.issuableLines(blType, blId, freightType),
    queryFn: () => financialDocumentPort.findIssuableLines(blType, blId, freightType),
  };
}

// === Mutation Functions ===

export const financialDocumentUseCases = {
  issueDocument: (req: IssueDocumentInput) => financialDocumentPort.issueDocument(req),
  deleteDocument: (id: number) => financialDocumentPort.deleteDocument(id),
  listByBl: (blType: string, blId: string) => financialDocumentPort.listByBl(blType, blId),
  findIssuableLines: (blType: string, blId: string, freightType: string) =>
    financialDocumentPort.findIssuableLines(blType, blId, freightType),
};
