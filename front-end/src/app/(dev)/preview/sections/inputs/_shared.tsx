"use client";

import type { BoxVariant } from "@/components/shared/inputs";

export type FormValues = {
  text: string;
  area: string;
  code: string;
  codeName: string;
  partyCn: string;
  partyName: string;
  amount: string;
  amountInt: string;
  amountDec2: string;
  amountDec3: string;
  amountIntNumber: number;
  unit: string;
  linkUrl: string;
  linkMenu: string;
  radioMode: string;
  time: string;
  timeCell: string;
  date: string;
};

export const defaultValues: FormValues = {
  text: "sample text",
  area: "line 1\nline 2",
  code: "KRPUS",
  codeName: "Busan Port",
  partyCn: "CONSIG",
  partyName: "Test Consignee",
  amount: "1000",
  amountInt: "",
  amountDec2: "",
  amountDec3: "",
  amountIntNumber: 0,
  unit: "KG",
  linkUrl: "External Docs",
  linkMenu: "User Management",
  radioMode: "A",
  time: "0930",
  timeCell: "",
  date: "",
};

export const sectionStyle: React.CSSProperties = {
  borderTop: "1px solid #ddd",
  padding: "12px 16px",
};

export const toggleStyle = (active: boolean): React.CSSProperties => ({
  padding: "4px 10px",
  fontSize: 11,
  border: "1px solid #ccc",
  borderRadius: 4,
  cursor: "pointer",
  background: active ? "#1d4ed8" : "#fff",
  color: active ? "#fff" : "#333",
});

export type SectionProps = {
  form: import("react-hook-form").UseFormReturn<FormValues>;
  variant: BoxVariant;
  required: boolean;
  readOnly: boolean;
  disabled: boolean;
};
