import type { InputHTMLAttributes, SelectHTMLAttributes } from "react";

export type BoxVariant = "panel" | "cell";

export type LabelOption = { value: string; label: string };

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
  onLookup: () => void;
  mono?: boolean;
  lookupAriaLabel?: string;
  labelOptions?: LabelOption[];
  labelValue?: string;
  onLabelChange?: (v: string) => void;
}

export interface DropBoxProps extends BoxBaseProps,
  Omit<SelectHTMLAttributes<HTMLSelectElement>, "children" | "required" | "readOnly" | "disabled"> {
  options: DropBoxOption[];
  placeholder?: string;
}

export interface LinkBoxProps extends BoxBaseProps {
  inputProps?: InputHTMLAttributes<HTMLInputElement>;
  label?: string;
  onLink?: () => void;
  linkAriaLabel?: string;
}

export interface RadioBoxOption { value: string; label: string }
export interface RadioBoxProps extends BoxBaseProps {
  label?: string;
  name: string;
  options: RadioBoxOption[];
  value?: string;
  defaultValue?: string;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
}

export type TimeBoxProps = BoxBaseProps & {
  value?: string;
  defaultValue?: string;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
  onBlur?: React.FocusEventHandler<HTMLInputElement>;
  name?: string;
};

export interface DateRangeBoxProps extends BoxBaseProps {
  label?: string;
  fromProps?: Omit<InputHTMLAttributes<HTMLInputElement>, 'defaultValue' | 'value'> & { defaultValue?: string; value?: string };
  toProps?: Omit<InputHTMLAttributes<HTMLInputElement>, 'defaultValue' | 'value'> & { defaultValue?: string; value?: string };
  tildeText?: string;
  labelOptions?: LabelOption[];
  labelValue?: string;
  onLabelChange?: (v: string) => void;
}
