import type { Dispatch, MutableRefObject, SetStateAction } from "react";
import type { UseFormReturn }    from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import type { HouseBlDetail }    from "@/domain/house-bl";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { houseBlPort }           from "@/lib/ports";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useBLDraftStore }       from "@/lib/use-bl-draft-store";
import type { BLVariantConfig }  from "@/lib/bl-variants";
import { createEmptyHouseBlFormValues } from "./house-bl-defaults";
import { buildHouseBlRequest, buildHouseBlUpdateRequest } from "./house-bl-submit";
import type { HouseBlFormValues } from "./house-bl-schema";

export function useHouseBlEntryMutations(args: {
  id: number | undefined;
  variant: BLVariantConfig;
  form: UseFormReturn<HouseBlFormValues>;
  detailLoadedRef: MutableRefObject<boolean>;
  setResetVersion: Dispatch<SetStateAction<number>>;
}): {
  // §6.29 — create: { id: number }, update: HouseBlDetail | null (SEA jobDiv 분기 → null)
  mutation: UseMutationResult<{ id: number } | HouseBlDetail | null, Error, HouseBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
} {
  const { id, variant, form, detailLoadedRef, setResetVersion } = args;
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((state) => state.clearDraft);
  const isEdit = Boolean(id);

  const mutation = useMutation({
    mutationFn: (data: HouseBlFormValues) => {
      return isEdit
        ? houseBlPort.update(id!, buildHouseBlUpdateRequest(data, variant))
        : houseBlPort.create(buildHouseBlRequest(data, variant));
    },
    onSuccess: (saved) => {
      if (!isEdit) {
        // create 시: saved는 HouseBlDetail (non-null). §6.29 — SEA update는 null 반환이므로
        // isEdit=false 분기에서만 saved.id에 접근해 타입 안전성 보장.
        const newId = saved?.id;
        if (newId != null) {
          // hot-marker: List 화면 진입 시 하이라이트에 사용 (§6.16, Truck 정합)
          sessionStorage.setItem(`house-bl-entry:hot:${newId}`, "1");
          useEntryFocusStore.getState().setFocus(entryFocusKeys.houseBl(variant.key), newId);
          clearDraft(`house:${variant.key}:new`);
          detailLoadedRef.current = false;
        }
      } else {
        // List 자동 invalidate 금지 (§6.21) — detail만 갱신
        queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", id] });
        clearDraft(`house:${variant.key}:${id}`);
        // refetch된 detail로 form.reset 재발동 (Truck 패턴 정합)
        detailLoadedRef.current = false;
      }
    },
  });

  const deleteMutation = useMutation<void, Error, void>({
    mutationFn: () => houseBlPort.delete(id!),
    onSuccess: () => {
      // List 자동 invalidate 금지 (§6.21) — detail cache 제거 + draft 정리 + ref 해제
      // Delete→Cargo 클리어 race 차단 (Truck 패턴 정합)
      queryClient.removeQueries({ queryKey: ["house-bl", "detail", id] });
      form.reset(createEmptyHouseBlFormValues());
      clearDraft(`house:${variant.key}:${id ?? "new"}`);
      clearDraft(`house:${variant.key}:new`);
      detailLoadedRef.current = false;
      useEntryFocusStore.getState().clearFocus(entryFocusKeys.houseBl(variant.key));
      setResetVersion((v) => v + 1);
    },
  });

  return { mutation, deleteMutation };
}
