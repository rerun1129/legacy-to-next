import { useEffect, useRef } from "react";
import type { UseFormReturn, FieldValues, DefaultValues } from "react-hook-form";
import { useListFilterStore } from "./use-list-filter-store";

/**
 * List filter 폼의 메뉴-이동 영속화 훅.
 * - mount 시: store에 저장된 filter가 있으면 1회 form.reset으로 복원
 * - 변경 시: form.watch 구독으로 store에 실시간 저장, unmount 시 unsubscribe
 */
export function useListFilterSync<T extends FieldValues>(
  form: UseFormReturn<T>,
  scope: string,
): void {
  const getFilter = useListFilterStore(state => state.getFilter);
  const setFilter = useListFilterStore(state => state.setFilter);
  const restoredRef = useRef(false);
  // 클로저 stale 방지용 — zustand 액션은 안정 참조이지만 ref로 명시
  const setFilterRef = useRef(setFilter);
  const getFilterRef = useRef(getFilter);

  // mount 1회만 filter 복원 — 최초 진입 시만 복원해야 함
  useEffect(() => {
    if (restoredRef.current) return;
    restoredRef.current = true;
    const stored = getFilterRef.current(scope);
    if (stored !== undefined) {
      form.reset(stored as DefaultValues<T>);
    }
  }, [form, scope]);

  // 변경 시 store 저장
  useEffect(() => {
    const subscription = form.watch((values) => {
      setFilterRef.current(scope, values);
    });
    return () => subscription.unsubscribe();
  }, [form, scope]);
}
