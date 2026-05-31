'use server';

import { cookies } from 'next/headers';
import { LOCALE_COOKIE, type Locale } from '@/i18n/config';
import { resolveLocale } from '@/i18n/locale';

export async function setLocale(locale: Locale) {
  const store = await cookies();
  store.set(LOCALE_COOKIE, resolveLocale(locale), {
    path: '/',
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 365,
  });
}
