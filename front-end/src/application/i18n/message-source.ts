import type { Locale } from '@/i18n/config';

export type Messages = Record<string, unknown>;

export interface MessageSource {
  load(locale: Locale): Promise<Messages>;
}
