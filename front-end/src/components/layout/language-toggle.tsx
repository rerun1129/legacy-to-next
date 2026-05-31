'use client';

import { Globe } from 'lucide-react';
import { useLocale, useTranslations } from 'next-intl';
import { useRouter } from 'next/navigation';
import { useTransition } from 'react';
import { setLocale } from '@/app/actions/set-locale';

export function LanguageToggle() {
  const locale = useLocale();
  const t = useTranslations('shell.lang');
  const router = useRouter();
  const [pending, startTransition] = useTransition();
  const next = locale === 'ko' ? 'en' : 'ko';

  return (
    <button
      type="button"
      className="topbar-icon"
      title={t('toggleTitle')}
      disabled={pending}
      onClick={() =>
        startTransition(async () => {
          await setLocale(next);
          router.refresh();
        })
      }
    >
      <Globe size={18} />
      <span style={{ fontSize: 11, fontWeight: 600 }}>{locale.toUpperCase()}</span>
    </button>
  );
}
