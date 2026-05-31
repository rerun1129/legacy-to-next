import { LOCALES, DEFAULT_LOCALE, type Locale } from './config';

export function resolveLocale(value?: string | null): Locale {
  return LOCALES.includes(value as Locale) ? (value as Locale) : DEFAULT_LOCALE;
}
