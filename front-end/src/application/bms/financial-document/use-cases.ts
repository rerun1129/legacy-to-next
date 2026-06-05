import { queryOptions } from "@tanstack/react-query";
import { financialDocumentPort } from "@/lib/ports";
import type { IssueDocumentInput, AmendDocumentInput, SearchFinancialDocumentInput } from "./ports";

// === 쿼리키 팩토리 ===

export const financialDocumentKeys = {
  all: ["bms", "financial-documents"] as const,
  listByBl: (blType: string, blId: string) =>
    [...financialDocumentKeys.all, "list", blType, blId] as const,
  issuableLines: (blType: string, blId: string, freightType: string) =>
    [...financialDocumentKeys.all, "issuable-lines", blType, blId, freightType] as const,
  search: (filter: SearchFinancialDocumentInput, page: number, size: number) =>
    [...financialDocumentKeys.all, "search", filter, page, size] as const,
  lines: (documentId: number) =>
    [...financialDocumentKeys.all, "lines", documentId] as const,
};

// === Query Options ===

export function listByBlQueryOptions(blType: string, blId: string) {
  return queryOptions({
    queryKey: financialDocumentKeys.listByBl(blType, blId),
    queryFn: () => financialDocumentPort.listByBl(blType, blId),
  });
}

export function issuableLinesQueryOptions(
  blType: string,
  blId: string,
  freightType: string
) {
  return queryOptions({
    queryKey: financialDocumentKeys.issuableLines(blType, blId, freightType),
    queryFn: () => financialDocumentPort.findIssuableLines(blType, blId, freightType),
  });
}

export function searchQueryOptions(
  filter: SearchFinancialDocumentInput,
  page: number,
  size: number,
  enabled: boolean
) {
  return queryOptions({
    queryKey: financialDocumentKeys.search(filter, page, size),
    queryFn: () => financialDocumentPort.search(filter, page, size),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });
}

export function linesQueryOptions(documentId: number | null) {
  return queryOptions({
    queryKey: financialDocumentKeys.lines(documentId ?? 0),
    queryFn: () => financialDocumentPort.findLines(documentId!),
    enabled: documentId !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });
}

// === Mutation Functions ===

export const financialDocumentUseCases = {
  issueDocument: (req: IssueDocumentInput) => financialDocumentPort.issueDocument(req),
  amendDocument: (req: AmendDocumentInput) => financialDocumentPort.amendDocument(req),
  deleteDocument: (id: number) => financialDocumentPort.deleteDocument(id),
  listByBl: (blType: string, blId: string) => financialDocumentPort.listByBl(blType, blId),
  findIssuableLines: (blType: string, blId: string, freightType: string) =>
    financialDocumentPort.findIssuableLines(blType, blId, freightType),
  search: (filter: SearchFinancialDocumentInput, page: number, size: number) =>
    financialDocumentPort.search(filter, page, size),
  findLines: (documentId: number) => financialDocumentPort.findLines(documentId),
};
