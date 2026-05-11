import type { MutableRefObject } from "react";
import type { UseFormReturn } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { nonBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { useEntryFocusStore } from "@/lib/use-entry-focus-store";
import { listFilterStore } from "@/lib/use-list-filter-store";
import type { NonBlFormValues } from "./non-bl-schema";

// List 화면과 동일한 SCOPE를 사용하여 inject 슬롯 키 일치 보장
export const NON_BL_LIST_SCOPE = "/fms/non-bl/list";

export function useSearchNonBl(args: {
  methods: UseFormReturn<NonBlFormValues>;
  id: number | null;
  detailLoadedRef: MutableRefObject<boolean>;
}): { handleSearch: () => Promise<void> } {
  const { methods, id, detailLoadedRef } = args;
  const router = useRouter();
  const queryClient = useQueryClient();

  async function handleSearch() {
    const nonBlNo = methods.getValues("nonBlNo")?.trim();
    if (!nonBlNo) {
      toast.info("hbl_no를 입력하세요.");
      return;
    }

    const ids = await nonBlPort.findByHblNo(nonBlNo);

    if (ids.length === 0) {
      toast.info("조회된 건이 없습니다.");
      return;
    }

    if (ids.length > 1) {
      toast.info("여러 건이 검색되었습니다. List에서 선택하세요.");
      // setInject → router.push 순서 동기 호출 유지 (commit a7f9e17 회귀 방지)
      listFilterStore.getState().setInject(NON_BL_LIST_SCOPE, { nonBlNo: nonBlNo });
      router.push("/fms/non-bl/list");
      return;
    }

    const targetId = ids[0];
    if (targetId === id) {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", id] });
      detailLoadedRef.current = false;
    } else {
      useEntryFocusStore.getState().setFocus("nonBl", targetId);
    }
  }

  return { handleSearch };
}
