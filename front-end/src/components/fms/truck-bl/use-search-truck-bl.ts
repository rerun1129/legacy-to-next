import type { MutableRefObject } from "react";
import type { UseFormReturn } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { truckBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { useEntryFocusStore } from "@/lib/use-entry-focus-store";
import type { TruckBlFormValues } from "./truck-bl-schema";

export function useSearchTruckBl(args: {
  form: UseFormReturn<TruckBlFormValues>;
  id: number | null;
  detailLoadedRef: MutableRefObject<boolean>;
}): { handleSearch: () => Promise<void> } {
  const { form, id, detailLoadedRef } = args;
  const router = useRouter();
  const queryClient = useQueryClient();

  async function handleSearch() {
    const truckBlNo = form.getValues("truckBlNo")?.trim();
    if (!truckBlNo) {
      toast.info("Truck B/L No를 입력하세요.");
      return;
    }

    const ids = await truckBlPort.findByHblNo(truckBlNo);

    if (ids.length === 0) {
      toast.info("조회 결과가 없습니다.");
      return;
    }

    if (ids.length > 1) {
      toast.info("여러 건이 검색되었습니다. List에서 선택하세요.");
      router.push("/fms/truck-bl/list");
      return;
    }

    const targetId = ids[0];
    if (targetId === id) {
      // 동일 id 재조회 — detail invalidate로 refetch 트리거
      queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", id] });
      detailLoadedRef.current = false;
    } else {
      // 다른 id — hot-marker 세팅 후 focus 이동 (§6.16)
      sessionStorage.setItem(`truck-bl-entry:hot:${targetId}`, "1");
      useEntryFocusStore.getState().setFocus("truckBl", targetId);
    }
  }

  return { handleSearch };
}
