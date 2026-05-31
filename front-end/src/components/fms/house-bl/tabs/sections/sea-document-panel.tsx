"use client";

import { useFormContext, Controller } from "react-hook-form";
import { CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function SeaDocumentPanel() {
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();

  const salesMan = useCodeAutocomplete(CODE_SOURCES.user);
  const operator = useCodeAutocomplete(CODE_SOURCES.user);
  const team     = useCodeAutocomplete(CODE_SOURCES.team);
  const { options: salesClassOptions, placeholder: salesClassPlaceholder } = useEnumOptions("SalesClass");

  const DOCUMENT_ITEMS: FieldItemDef[] = [
    {
      key: "sales-class",
      render: () => (
        <div className="li">
          <span className="li__label">Sales Class</span>
          <div className="li__input">
            <Controller
              name="salesClass"
              control={control}
              render={({ field }) => (
                <ComboBox variant="panel" options={salesClassOptions} placeholder={salesClassPlaceholder} value={field.value} onChange={field.onChange} />
              )}
            />
          </div>
        </div>
      ),
    },
    {
      key: "sales-man",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Sales Man"
          codeProps={{ ...register("salesManCode") }}
          nameProps={{ ...register("salesManName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={salesMan.onSearch}
          suggestions={salesMan.suggestions}
          suggestionsLoading={salesMan.suggestionsLoading}
          onSelect={(it) => { setValue("salesManCode", it.code); setValue("salesManName", it.name); }}
        />
      ),
    },
    {
      key: "operator",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Operator"
          required
          codeProps={{ ...register("operatorCode") }}
          nameProps={{ ...register("operatorName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={operator.onSearch}
          suggestions={operator.suggestions}
          suggestionsLoading={operator.suggestionsLoading}
          onSelect={(it) => { setValue("operatorCode", it.code); setValue("operatorName", it.name); }}
        />
      ),
    },
    {
      key: "team",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Team"
          required
          codeProps={{ ...register("teamCode") }}
          nameProps={{ ...register("teamName") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={team.onSearch}
          suggestions={team.suggestions}
          suggestionsLoading={team.suggestionsLoading}
          onSelect={(it) => { setValue("teamCode", it.code); setValue("teamName", it.name); }}
        />
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Document</span>
      </div>
      <div className="panel__body panel__body--scroll-flex2">
        <FieldItemGrid itemScope="sea-document-panel" items={DOCUMENT_ITEMS} cols={1} />
      </div>
    </div>
  );
}
