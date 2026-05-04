import type { LucideIcon } from 'lucide-react';
import type { ButtonVariant } from '@/components/shared/button';

export type { ButtonVariant };

export type ToggleableVariant = Extract<ButtonVariant, "search" | "transaction" | "danger" | "normal">;

export interface ActionSpecimen {
  id: string;
  label: string;
  icon: LucideIcon;
  defaultVariant: ToggleableVariant;
  confirmMessage: string;
}

export interface ButtonInBundle {
  id: string;
  label: string;
  icon?: LucideIcon;
  initialVariant: ToggleableVariant | 'default';
  type?: 'submit' | 'button';
  confirmMessage: string;
}

export interface PageBundle {
  pageId: string;
  pageLabel: string;
  sourceFile: string;
  buttons: ButtonInBundle[];
}
