import { useEffect, useRef } from "react";
import type { UseFormReturn, FieldValues, DefaultValues } from "react-hook-form";
import { useBLDraftStore } from "./use-bl-draft-store";

/**
 * BL entry 폼의 메뉴-이동 영속화 훅.
 * - key 변경마다: store에 저장된 draft가 있으면 form.reset으로 복원
 * - 변경 시: form.watch 구독으로 store에 실시간 저장, unmount 시 unsubscribe
 *
 * 반환값 didRestoreFromDraftRef: 이번 key 기준으로 stored draft로 실제 form.reset을 호출했는지 여부.
 * key 변경 시마다 false로 초기화된 뒤 복원 여부에 따라 갱신됨.
 * 호출처에서 deps에 넣지 않아도 됨 — useRef 객체 자체는 안정 참조.
 */
export function useBlDraftSync<T extends FieldValues>(
  form: UseFormReturn<T>,
  key: string,
): { didRestoreFromDraftRef: React.MutableRefObject<boolean> } {
  const getDraft = useBLDraftStore(state => state.getDraft);
  const setDraft = useBLDraftStore(state => state.setDraft);
  const didRestoreFromDraftRef = useRef(false);
  // 클로저 stale 방지용 — zustand 액션은 안정 참조이지만 ref로 명시
  const setDraftRef = useRef(setDraft);
  const getDraftRef = useRef(getDraft);

  // key 변경마다 복원 시도. 복원 여부는 didRestoreFromDraftRef로 노출
  useEffect(() => {
    didRestoreFromDraftRef.current = false;
    const stored = getDraftRef.current(key);
    if (stored !== undefined) {
      didRestoreFromDraftRef.current = true;
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

  return { didRestoreFromDraftRef };
}
