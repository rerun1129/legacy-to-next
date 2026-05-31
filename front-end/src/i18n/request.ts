import { cookies } from 'next/headers';
import { getRequestConfig } from 'next-intl/server';
import { LOCALE_COOKIE } from './config';
import { resolveLocale } from './locale';
import { staticMessageSource } from '@/adapter/out/i18n/static-message-source';

export default getRequestConfig(async () => {
  const store = await cookies();
  const locale = resolveLocale(store.get(LOCALE_COOKIE)?.value);
  const messages = await staticMessageSource.load(locale);
  return { locale, messages };
});
