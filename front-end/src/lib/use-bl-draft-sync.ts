import { useEffect, useRef } from "react";
import type { UseFormReturn, FieldValues, DefaultValues } from "react-hook-form";
import { useBLDraftStore } from "./use-bl-draft-store";

/**
 * BL entry 폼의 메뉴-이동 영속화 훅.
 * - mount 시: store에 저장된 draft가 있으면 1회 form.reset으로 복원
 * - 변경 시: form.watch 구독으로 store에 실시간 저장, unmount 시 unsubscribe
 */
export function useBlDraftSync<T extends FieldValues>(
  form: UseFormReturn<T>,
  key: string,
): void {
  const getDraft = useBLDraftStore(state => state.getDraft);
  const setDraft = useBLDraftStore(state => state.setDraft);
  const restoredRef = useRef(false);
  // 클로저 stale 방지용 — zustand 액션은 안정 참조이지만 ref로 명시
  const setDraftRef = useRef(setDraft);
  const getDraftRef = useRef(getDraft);

  // mount 1회만 draft 복원 — detailLoadedRef 패턴과 동일한 이유: 최초 진입 시만 복원해야 함
  useEffect(() => {
    if (restoredRef.current) return;
    restoredRef.current = true;
    const stored = getDraftRef.current(key);
    if (stored !== undefined) {
      form.reset(stored as DefaultValues<T>);
    }
  }, [form, key]);

  // 변경 시 store 저장
  useEffect(() => {
    const subscription = form.watch((values) => {
      setDraftRef.current(key, values);
    });
    return () => subscription.unsubscribe();
  }, [form, key]);
}
