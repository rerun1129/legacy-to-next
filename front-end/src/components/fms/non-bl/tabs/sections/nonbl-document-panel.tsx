"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function NonBLDocumentPanel() {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tf = useTranslations("fms.nonBl.entry.fields");
  const tp = useTranslations("fms.nonBl.entry.panels");

  const { register, control, setValue } = useFormContext<NonBlFormValues>();
  const { options: salesClassOptions, placeholder: salesClassPlaceholder } = useEnumOptions("SalesClass");
  const salesMan = useCodeAutocomplete(CODE_SOURCES.user);
  const operator = useCodeAutocomplete(CODE_SOURCES.user);
  const team     = useCodeAutocomplete(CODE_SOURCES.team);

  const documentItems: FieldItemDef[] = [
    {
      key: "sales-class",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("salesClass")}</span>
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
          label={tf("salesMan")}
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
          label={tf("operator")}
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
          label={tf("team")}
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
        <span className="panel__title">{tp("document")}</span>
      </div>
      <div className="panel__body panel__body--scroll-flex2">
        <FieldItemGrid itemScope="nonbl-document-panel" items={documentItems} />
      </div>
    </div>
  );
}
