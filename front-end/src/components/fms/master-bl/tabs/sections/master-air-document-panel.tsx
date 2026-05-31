"use client";

import { useFormContext } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

// Master AIR Document: Operator / Team / Settle Partner 3슬롯
// Sales Man / Sales Class 제외 (House 4슬롯과의 차이)
// Master schema: operatorCode/teamCode/settlePartnerCode (operatorName/teamName 표시 전용)
export function MasterAirDocumentPanel() {
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const operator       = useCodeAutocomplete(CODE_SOURCES.user);
  const settlePartner  = useCodeAutocomplete(CODE_SOURCES.partner);
  const team           = useCodeAutocomplete(CODE_SOURCES.team);

  const DOCUMENT_ITEMS: FieldItemDef[] = [
    {
      key: "operator",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Operator"
          required
          codeProps={{ ...register("operatorCode") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={operator.onSearch}
          suggestions={operator.suggestions}
          suggestionsLoading={operator.suggestionsLoading}
          onSelect={(it) => { setValue("operatorCode", it.code); }}
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
    {
      key: "settle-partner",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Settle Partner"
          codeProps={{ ...register("settlePartnerCode") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={settlePartner.onSearch}
          suggestions={settlePartner.suggestions}
          suggestionsLoading={settlePartner.suggestionsLoading}
          onSelect={(it) => { setValue("settlePartnerCode", it.code); }}
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
        <FieldItemGrid itemScope="master-air-document-panel" items={DOCUMENT_ITEMS} cols={1} />
      </div>
    </div>
  );
}
