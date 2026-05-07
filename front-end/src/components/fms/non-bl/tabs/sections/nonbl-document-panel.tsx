"use client";

import { useFormContext } from "react-hook-form";
import { CodeBox, DropBox } from "@/components/shared/inputs";
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
            <DropBox
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
          onLookup={() => {}}
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
          onLookup={() => {}}
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
          onLookup={() => {}}
        />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Document</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 2 }}>
        <FieldItemGrid itemScope="nonbl-document-panel" items={DOCUMENT_ITEMS} />
      </div>
    </div>
  );
}
