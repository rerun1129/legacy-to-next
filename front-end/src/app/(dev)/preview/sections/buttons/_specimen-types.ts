import type { LucideIcon } from 'lucide-react';

export type BtnVariant = 'primary' | 'ghost' | 'danger';

export interface ActionSpecimen {
  id: string;
  label: string;
  icon: LucideIcon;
  defaultVariant: BtnVariant;
  confirmMessage: string;
}

export interface ButtonInBundle {
  id: string;
  label: string;
  icon?: LucideIcon;
  initialVariant: BtnVariant | 'default';
  type?: 'submit' | 'button';
  confirmMessage: string;
}

export interface PageBundle {
  pageId: string;
  pageLabel: string;
  sourceFile: string;
  buttons: ButtonInBundle[];
}
