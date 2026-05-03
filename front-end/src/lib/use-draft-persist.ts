"use client";

import { useCallback, useEffect } from "react";
import type { UseFormReturn, FieldValues } from "react-hook-form";

export type DraftPersistResult = {
  /** sessionStorage에서 해당 키를 제거한다. Save / Delete 성공 시 호출. */
  clearDraft: () => void;
  /** 마운트 시점에 복원할 draft가 있는지 동기적으로 반환. form.reset(serverData) 전에 체크해 draft 우선 여부 결정용. */
  hasDraft: () => boolean;
};

/**
 * form 값을 sessionStorage에 자동 저장·복원한다.
 *
 * storageKey는 record 단위로 분리해 전달한다.
 *   신규: `draft:master-bl:sea-exp:new`
 *   수정: `draft:master-bl:sea-exp:42`
 *
 * isEdit 모드에서 서버 데이터와 충돌을 막으려면,
 * form.reset(serverDetail) 호출 전에 `hasDraft()` 로 체크해 skip한다.
 *
 *   const { clearDraft, hasDraft } = useDraftPersist(form, key);
 *   useEffect(() => {
 *     if (!detail || hasDraft()) return;
 *     form.reset(mapDetail(detail));
 *   }, [detail]);
 */
export function useDraftPersist<T extends FieldValues>(
  form: UseFormReturn<T>,
  storageKey: string,
): DraftPersistResult {
  // 최초 마운트 시 저장된 초안 복원
  useEffect(() => {
    const raw = sessionStorage.getItem(storageKey);
    if (!raw) return;
    try {
      form.reset(JSON.parse(raw) as T);
    } catch {
      sessionStorage.removeItem(storageKey);
    }
  // storageKey/form은 마운트 시점 고정값 — 의도적 deps 생략
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // form 변경 시마다 sessionStorage 갱신
  useEffect(() => {
    const { unsubscribe } = form.watch((values) => {
      sessionStorage.setItem(storageKey, JSON.stringify(values));
    });
    return unsubscribe;
  }, [form, storageKey]);

  const clearDraft = useCallback(
    () => sessionStorage.removeItem(storageKey),
    [storageKey],
  );

  const hasDraft = useCallback(
    () => sessionStorage.getItem(storageKey) !== null,
    [storageKey],
  );

  return { clearDraft, hasDraft };
}
