"use client";

import { useForm } from "react-hook-form";
import { useState } from "react";
import type { BoxVariant } from "@/components/shared/inputs";
import { type FormValues, defaultValues } from "./inputs/_shared";
import { InputsToolbar } from "./inputs/_toolbar";
import { TextSection } from "./inputs/text-section";
import { CodeSection } from "./inputs/code-section";
import { NumberSection } from "./inputs/number-section";
import { ComboSection } from "./inputs/combo-section";
import { MultiSelectSection } from "./inputs/multi-select-section";
import { DateSection } from "./inputs/date-section";
import { TimeSection } from "./inputs/time-section";
import { LinkRadioSection } from "./inputs/link-radio-section";

export function InputsSection() {
  const form = useForm<FormValues>({ defaultValues });
  const [variant, setVariant] = useState<BoxVariant>("panel");
  const [required, setRequired] = useState(false);
  const [readOnly, setReadOnly] = useState(false);
  const [disabled, setDisabled] = useState(false);

  const toolbarProps = {
    variant,
    setVariant,
    required,
    setRequired,
    readOnly,
    setReadOnly,
    disabled,
    setDisabled,
    getValues: form.getValues,
  };
  const sectionProps = { form, variant, required, readOnly, disabled };

  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 800, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Inputs Preview</h1>
      <InputsToolbar {...toolbarProps} />
      <TextSection {...sectionProps} />
      <CodeSection {...sectionProps} />
      <NumberSection {...sectionProps} />
      <ComboSection {...sectionProps} />
      <MultiSelectSection {...sectionProps} />
      <DateSection {...sectionProps} />
      <TimeSection {...sectionProps} />
      <LinkRadioSection {...sectionProps} />
    </div>
  );
}
