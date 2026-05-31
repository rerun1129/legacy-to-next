import type { MutableRefObject } from "react";
import type { UseFormReturn } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { truckBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { useEntryFocusStore } from "@/lib/use-entry-focus-store";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import type { TruckBlFormValues } from "./truck-bl-schema";

export function useSearchTruckBl(args: {
  form: UseFormReturn<TruckBlFormValues>;
  id: number | null;
  detailLoadedRef: MutableRefObject<boolean>;
}): { handleSearch: () => Promise<void> } {
  const { form, id, detailLoadedRef } = args;
  const t = useTranslations("fms.truckBl.entry.msg");
  const router = useRouter();
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((s) => s.clearDraft);

  async function handleSearch() {
    const truckBlNo = form.getValues("truckBlNo")?.trim();
    if (!truckBlNo) {
      toast.info(t("enterBlNo"));
      return;
    }

    const ids = await truckBlPort.findByHblNo(truckBlNo);

    if (ids.length === 0) {
      toast.info(t("noResults"));
      return;
    }

    if (ids.length > 1) {
      toast.info(t("multipleFound"));
      router.push("/fms/truck-bl/list");
      return;
    }

    const targetId = ids[0];
    if (targetId === id) {
      // 동일 id 재조회 — detail invalidate + draft 클리어로 프레시 조회
      queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", id] });
      clearDraft("truck::" + id);
      detailLoadedRef.current = false;
    } else {
      // 다른 id — 프레시 조회: stale 캐시·draft 제거 후 focus 이동 (§6.16)
      queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", targetId] });
      clearDraft("truck::" + targetId);
      sessionStorage.setItem(`truck-bl-entry:hot:${targetId}`, "1");
      useEntryFocusStore.getState().setFocus("truckBl", targetId);
    }
  }

  return { handleSearch };
}
