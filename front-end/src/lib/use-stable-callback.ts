"use client";

import { useLayoutEffect, useMemo, useRef } from "react";

/**
 * 인라인 콜백을 안정적인 참조로 감싸 React.memo 자식의 props 비교를 통과시킨다.
 * 콜백 본체가 매 렌더 새 함수여도 반환된 wrapper의 참조는 콜백 truthiness 변화 시에만 갱신된다.
 */
export function useStableOptionalCallback<TArgs extends unknown[], TRet>(
  cb: ((...args: TArgs) => TRet) | undefined,
): ((...args: TArgs) => TRet) | undefined {
  const ref = useRef(cb);
  useLayoutEffect(() => {
    ref.current = cb;
  });
  const exists = cb != null;
  return useMemo(() => {
    if (!exists) return undefined;
    return (...args: TArgs): TRet => {
      const fn = ref.current;
      return fn ? fn(...args) : (undefined as unknown as TRet);
    };
  }, [exists]);
}
