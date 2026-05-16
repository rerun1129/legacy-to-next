"use client";

import { useFormContext } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";

// Master AIR Document: Operator / Team / Settle Partner 3슬롯
// Sales Man / Sales Class 제외 (House 4슬롯과의 차이)
// Master schema: operatorCode/teamCode/settlePartnerCode (name 필드 없음 — code-only)
export function MasterAirDocumentPanel() {
  const { register } = useFormContext<MasterBlFormValues>();

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
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
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
