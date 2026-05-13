import type { UseFormReturn } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { houseBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { listFilterStore } from "@/lib/use-list-filter-store";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import type { BLVariantConfig } from "@/lib/bl-variants";
import type { JobDiv } from "@/domain/house-bl";
import type { HouseBlFormValues } from "./house-bl-schema";

export function useSearchBl(
  form: UseFormReturn<HouseBlFormValues>,
  variant: BLVariantConfig,
  options?: {
    id?: number | null;
    onAfterFound?: (targetId: number, sameAsCurrent: boolean) => void;
  },
) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const id = options?.id ?? null;

  async function handleSearchBl() {
    const blNo = form.getValues("hbl")?.trim();
    if (!blNo) {
      toast.info("B/L No.를 입력하세요.");
      return;
    }

    const jobDiv = variant.mode as JobDiv;
    let ids: number[];
    try {
      ids = await houseBlPort.findByHblNo(blNo, jobDiv);
    } catch (err) {
      const message = err instanceof Error ? err.message : String(err);
      toast.error(`B/L 조회 중 오류가 발생했습니다: ${message}`);
      return;
    }

    if (ids.length === 0) {
      toast.info("일치하는 B/L이 없습니다.");
      return;
    }

    if (ids.length > 1) {
      toast.info("동일 B/L No. 다건 발견 — List 화면에서 선택해주세요.");
      // setInject → router.push 순서 동기 호출 유지 (§non-bl 패턴 정합)
      listFilterStore.getState().setInject(`/fms/house-bl/${variant.key}/list`, { hblNo: blNo });
      router.push(`/fms/house-bl/${variant.key}/list`);
      return;
    }

    const targetId = ids[0];
    if (targetId === id) {
      // 동일 id 재조회 — detail invalidate + draft 클리어로 프레시 조회
      queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", id] });
      clearDraft(`house:${variant.key}:${id}`);
      options?.onAfterFound?.(targetId, true);
    } else {
      // 다른 id — 프레시 조회: stale 캐시·draft 제거 후 focus 이동
      queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", targetId] });
      clearDraft(`house:${variant.key}:${targetId}`);
      // hot-marker: List 화면 진입 시 하이라이트에 사용 (§6.16)
      sessionStorage.setItem(`house-bl-entry:hot:${targetId}`, "1");
      useEntryFocusStore.getState().setFocus(entryFocusKeys.houseBl(variant.key), targetId);
      options?.onAfterFound?.(targetId, false);
    }
  }

  return { handleSearchBl };
}
