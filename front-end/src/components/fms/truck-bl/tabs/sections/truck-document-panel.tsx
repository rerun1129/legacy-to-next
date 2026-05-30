"use client";

import { useFormContext, Controller } from "react-hook-form";
import { CodeBox, TextBox, DateBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function TruckDocumentPanel() {
  const { register, control, setValue } = useFormContext<TruckBlFormValues>();
  const trucker = useCodeAutocomplete(CODE_SOURCES.trucker);
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1, padding: "8px 0" }}>
        {/* Pick-up Date */}
        <div className="li">
          <span className="li__label">Pick-up Date</span>
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
        {/* Trucker */}
        <CodeBox
          kind="lcn"
          label="Trucker"
          codeProps={{ ...register("truckerCode"), placeholder: "Code" }}
          nameProps={{ ...register("truckerName"), placeholder: "Trucker Name" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={trucker.onSearch}
          suggestions={trucker.suggestions}
          suggestionsLoading={trucker.suggestionsLoading}
          onSelect={(it) => { setValue("truckerCode", it.code); setValue("truckerName", it.name); }}
        />
        {/* Trucker PIC */}
        <div className="li">
          <span className="li__label">Trucker PIC</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Trucker PIC" {...register("truckerPic")} />
          </div>
        </div>
      </div>
    </div>
  );
}
