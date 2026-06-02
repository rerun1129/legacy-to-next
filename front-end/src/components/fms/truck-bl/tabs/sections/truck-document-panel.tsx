"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { CodeBox, TextBox, DateBox } from "@/components/shared/inputs";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function TruckDocumentPanel() {
  // Rules of Hooks: unconditionally at top
  const tf = useTranslations("fms.truckBl.entry.fields");
  const tp = useTranslations("fms.truckBl.entry.panels");

  const { register, control, setValue } = useFormContext<TruckBlFormValues>();
  const trucker = useCodeAutocomplete(CODE_SOURCES.trucker);

  const docItems: FieldItemDef[] = [
    {
      key: "pickup-date",
      label: tf("pickupDate"),
      render: () => (
        <div className="li">
          <span className="li__label">{tf("pickupDate")}</span>
          <div className="li__input">
            <Controller
              control={control}
              name="pickupDate"
              render={({ field }) => (
                <DateBox
                  ref={field.ref}
                  name={field.name}
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                />
              )}
            />
          </div>
        </div>
      ),
    },
    {
      key: "trucker",
      fullWidth: true,
      label: tf("trucker"),
      render: () => (
        <CodeBox
          kind="lcn"
          label={tf("trucker")}
          codeProps={{ ...register("truckerCode"), placeholder: "Code" }}
          nameProps={{ ...register("truckerName"), placeholder: "Trucker Name" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={trucker.onSearch}
          suggestions={trucker.suggestions}
          suggestionsLoading={trucker.suggestionsLoading}
          onSelect={(it) => { setValue("truckerCode", it.code); setValue("truckerName", it.name); }}
        />
      ),
    },
    {
      key: "trucker-pic",
      label: tf("truckerPic"),
      render: () => (
        <div className="li">
          <span className="li__label">{tf("truckerPic")}</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Trucker PIC" {...register("truckerPic")} />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tp("document")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1, padding: "8px 0" }}>
        <FieldItemGrid itemScope="truck-document-panel" items={docItems} cols={2} />
      </div>
    </div>
  );
}
