"use client";

import { useToastStore } from '@/lib/toast-store';

export function ToastViewport() {
  const { toasts, remove } = useToastStore();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
      {toasts.map((t) => (
        <div
          key={t.id}
          onClick={() => remove(t.id)}
          className={[
            'cursor-pointer rounded-md px-4 py-3 text-sm text-white shadow-lg whitespace-pre-line',
            t.type === 'error' ? 'bg-red-600' :
            t.type === 'success' ? 'bg-green-600' : 'bg-blue-600',
          ].join(' ')}
        >
          {t.message}
        </div>
      ))}
    </div>
  );
}
