import type { InputHTMLAttributes } from "react";

export interface CodeBoxSuggestion {
  id?: number;
  code: string;
  name: string;
  address?: string;
}

export type BoxVariant = "panel" | "cell" | "label";

export type LabelOption = { value: string; label: string };

export interface BoxBaseProps {
  variant?: BoxVariant;
  required?: boolean;
  readOnly?: boolean;
  disabled?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

export type CodeBoxKind = "lcn" | "party-cn" | "code-only";

export interface ComboBoxOption {
  value: string;
  label: string;
}

export interface CodeBoxProps extends BoxBaseProps {
  kind?: CodeBoxKind;
  label?: string;
  codeProps: InputHTMLAttributes<HTMLInputElement>;
  nameProps?: InputHTMLAttributes<HTMLInputElement>;
  onLookup?: () => void;
  mono?: boolean;
  lookupAriaLabel?: string;
  labelOptions?: LabelOption[];
  labelValue?: string;
  onLabelChange?: (v: string) => void;
  // 자동완성 props
  suggestions?: CodeBoxSuggestion[];
  onSearch?: (query: string) => void;
  onSelect?: (item: CodeBoxSuggestion) => void;
  suggestionsLoading?: boolean;
  /**
   * 기본 true. false 이면 invalid blur 시 비우지 않음 —
   * 호출측이 onBlur에서 직전 유효값 복원을 책임진다.
   */
  clearInvalidOnBlur?: boolean;
}

export interface ComboBoxProps extends BoxBaseProps,
  Omit<InputHTMLAttributes<HTMLInputElement>, "children" | "required" | "readOnly" | "disabled"> {
  options: ComboBoxOption[];
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

export type DateBoxProps = BoxBaseProps & {
  value?: string;
  defaultValue?: string;
  name?: string;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
  onBlur?: React.FocusEventHandler<HTMLInputElement>;
};
