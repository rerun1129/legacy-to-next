import type { InputHTMLAttributes, SelectHTMLAttributes } from "react";

export type BoxVariant = "panel" | "cell";

export interface BoxBaseProps {
  variant?: BoxVariant;
  required?: boolean;
  readOnly?: boolean;
  disabled?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

export type CodeBoxKind = "lcn" | "party-cn";

export interface DropBoxOption {
  value: string;
  label: string;
}

export interface CodeBoxProps extends BoxBaseProps {
  kind?: CodeBoxKind;
  label?: string;
  codeProps: InputHTMLAttributes<HTMLInputElement>;
  nameProps: InputHTMLAttributes<HTMLInputElement>;
  onLookup?: () => void;
  mono?: boolean;
  lookupAriaLabel?: string;
}

export interface DropBoxProps extends BoxBaseProps,
  Omit<SelectHTMLAttributes<HTMLSelectElement>, "children" | "required" | "readOnly" | "disabled"> {
  options: DropBoxOption[];
  placeholder?: string;
}
