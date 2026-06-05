"use client";

import type { UseFormReturn } from "react-hook-form";
import type { SeaHouseFilter } from "@/domain/sea-house";
import { useSeaHouseListFilterModel } from "./use-sea-house-list-filter-model";
import { SeaHouseFilterDateFields } from "./sea-house-filter-date-fields";
import { SeaHouseFilterPartyFields } from "./sea-house-filter-party-fields";
import { SeaHouseFilterLogisticsFields } from "./sea-house-filter-logistics-fields";
import { SeaHouseFilterClassFields } from "./sea-house-filter-class-fields";

interface Props {
  form: UseFormReturn<SeaHouseFilter>;
}

export function SeaHouseListFilter({ form }: Props) {
  const model = useSeaHouseListFilterModel(form);

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          <SeaHouseFilterDateFields
            control={form.control}
            dateKindOptions={model.dateKindOptions}
          />
          <SeaHouseFilterPartyFields
            control={form.control}
            register={model.register}
            setValue={model.setValue}
            t={model.t}
            masterBlKindOptions={model.masterBlKindOptions}
            partyKindOptions={model.partyKindOptions}
            partnerKindOptions={model.partnerKindOptions}
            party={model.party}
            actualCustomer={model.actualCustomer}
            partner={model.partner}
          />
          <SeaHouseFilterLogisticsFields
            control={form.control}
            register={model.register}
            setValue={model.setValue}
            t={model.t}
            portKindOptions={model.portKindOptions}
            shipmentTypeOptionsWithAll={model.shipmentTypeOptionsWithAll}
            shipmentType={model.shipmentType}
            liner={model.liner}
            port={model.port}
            operator={model.operator}
            team={model.team}
          />
          <SeaHouseFilterClassFields
            control={form.control}
            register={model.register}
            setValue={model.setValue}
            t={model.t}
            salesClassOptionsWithAll={model.salesClassOptionsWithAll}
            incotermsOptionsWithAll={model.incotermsOptionsWithAll}
            loadTypeOptionsWithAll={model.loadTypeOptionsWithAll}
            salesClass={model.salesClass}
            incoterms={model.incoterms}
            loadType={model.loadType}
            salesMan={model.salesMan}
          />
        </div>
      </div>
    </div>
  );
}
