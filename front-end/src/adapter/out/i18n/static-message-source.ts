import type { MessageSource, Messages } from '@/application/i18n/message-source';
import type { Locale } from '@/i18n/config';

// Explicit loader map (not a template-literal dynamic import) so webpack and turbopack
// can statically analyze the import graph and include both JSON files in the bundle.
const LOADERS: Record<Locale, () => Promise<{ default: Messages }>> = {
  ko: () => import('@/messages/ko.json'),
  en: () => import('@/messages/en.json'),
};

export const staticMessageSource: MessageSource = {
  async load(locale) {
    return (await LOADERS[locale]()).default;
  },
};
