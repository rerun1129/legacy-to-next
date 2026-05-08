"use client";

import { useFormContext } from "react-hook-form";
import { CodeBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLDocumentPanel() {
  const { register } = useFormContext<NonBlFormValues>();
  const { options: salesClassOptions, placeholder: salesClassPlaceholder } = useEnumOptions("SalesClass");

  const DOCUMENT_ITEMS: FieldItemDef[] = [
    {
      key: "sales-class",
      render: () => (
        <div className="li">
          <span className="li__label">Sales Class</span>
          <div className="li__input">
            <ComboBox
              variant="panel"
              options={salesClassOptions}
              placeholder={salesClassPlaceholder}
              {...register("salesClass")}
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
        <FieldItemGrid itemScope="nonbl-document-panel" items={DOCUMENT_ITEMS} />
      </div>
    </div>
  );
}
