"use client";

import { Controller, type UseFormReturn } from "react-hook-form";
import { ComboBox, TextBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { SwitchBlFormValues } from "./switch-bl-modal";

export interface ToolbarProps {
  houseBlNo: string;
  form: UseFormReturn<SwitchBlFormValues>;
}

export function SwitchBlToolbar({ houseBlNo, form }: ToolbarProps) {
  const { control } = form;
  const { options: incotermsOptions, placeholder: incotermsPh } = useEnumOptions("Incoterms");
  const { options: blTypeOptions,    placeholder: blTypePh }    = useEnumOptions("BlType");

  return (
    <div className="toolbar" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
      <div className="field is-required">
        <div className="field__label is-required">Switch B/L No</div>
        <div className="field__input">
          <Controller
            name="switchBlNo"
            control={control}
            render={({ field }) => (
              <TextBox
                placeholder="Switch B/L No"
                value={field.value ?? ""}
                onChange={field.onChange}
                onBlur={field.onBlur}
              />
            )}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">House B/L No</div>
        <div className="field__input">
          <input value={houseBlNo} readOnly />
        </div>
      </div>
      <div className="field">
        <div className="field__label">Incoterms</div>
        <div className="field__input">
          <Controller
            name="incoterms"
            control={control}
            render={({ field }) => (
              <ComboBox
                options={incotermsOptions}
                placeholder={incotermsPh}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
      <div className="field is-required">
        <div className="field__label is-required">B/L Type</div>
        <div className="field__input">
          <Controller
            name="blType"
            control={control}
            render={({ field }) => (
              <ComboBox
                options={blTypeOptions}
                placeholder={blTypePh}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
    </div>
  );
}
