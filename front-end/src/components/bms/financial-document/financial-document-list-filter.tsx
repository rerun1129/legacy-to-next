"use client";

import type { UseFormReturn } from "react-hook-form";
import type { FinancialDocumentFilter } from "./use-financial-document-list-filter-model";
import { useFinancialDocumentListFilterModel } from "./use-financial-document-list-filter-model";
import { FinancialDocumentFilterBasicFields } from "./financial-document-filter-basic-fields";
import { FinancialDocumentFilterPartyFields } from "./financial-document-filter-party-fields";

interface Props {
  form: UseFormReturn<FinancialDocumentFilter>;
  scope: string;
}

export function FinancialDocumentListFilter({ form, scope }: Props) {
  const model = useFinancialDocumentListFilterModel(form, scope);

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          <FinancialDocumentFilterBasicFields
            control={form.control}
            t={model.t}
            dateKindOptions={model.dateKindOptions}
            documentStatusOptions={model.documentStatusOptions}
            jobDivOptionsWithAll={model.jobDivOptionsWithAll}
            jobDivLoading={model.jobDivLoading}
            jobDivPlaceholder={model.jobDivPlaceholder}
            boundOptionsWithAll={model.boundOptionsWithAll}
            boundLoading={model.boundLoading}
            boundPlaceholder={model.boundPlaceholder}
          />
          <FinancialDocumentFilterPartyFields
            register={model.register}
            setValue={model.setValue}
            t={model.t}
            customer={model.customer}
            team={model.team}
            operator={model.operator}
          />
        </div>
      </div>
    </div>
  );
}
