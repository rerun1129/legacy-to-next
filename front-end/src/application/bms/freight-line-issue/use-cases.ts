import { freightLineIssuePort } from "@/lib/ports";
import type { SearchFreightLineInput, IssueFreightLineInput, CancelFreightLineInput } from "./ports";

// === 쿼리키 팩토리 ===

export const freightLineIssueKeys = {
  all: ["bms", "freight-line-issues"] as const,
  search: (filter: SearchFreightLineInput, page: number, size: number) =>
    [...freightLineIssueKeys.all, "search", filter, page, size] as const,
};

// === Mutation Functions ===

export const freightLineIssueUseCases = {
  search: (filter: SearchFreightLineInput, page: number, size: number) =>
    freightLineIssuePort.search(filter, page, size),
  issueTax: (req: IssueFreightLineInput) => freightLineIssuePort.issueTax(req),
  issueSlip: (req: IssueFreightLineInput) => freightLineIssuePort.issueSlip(req),
  cancelTax: (req: CancelFreightLineInput) => freightLineIssuePort.cancelTax(req),
  cancelSlip: (req: CancelFreightLineInput) => freightLineIssuePort.cancelSlip(req),
};
