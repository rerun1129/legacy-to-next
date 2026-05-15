import type { Dispatch, MutableRefObject, SetStateAction } from "react";
import type { UseFormReturn } from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { masterBlPort } from "@/lib/ports";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import { buildCreateMasterBlPayload, buildUpdateMasterBlPayload } from "./master-bl-submit";
import type { MasterBlFormValues } from "./master-bl-schema";

export function useMasterBlEntryMutations(args: {
  id: number | undefined;
  variantKey: string;
  variant: MasterVariantConfig;
  form: UseFormReturn<MasterBlFormValues>;
  detailLoadedRef: MutableRefObject<boolean>;
  setResetVersion: Dispatch<SetStateAction<number>>;
}): {
  mutation: UseMutationResult<{ id: number } | void, Error, MasterBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
} {
  const { id, variantKey, variant, form, detailLoadedRef, setResetVersion } = args;
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((state) => state.clearDraft);
  const isEdit = Boolean(id);

  // §6.49 ⑩ — mutation onSuccess SSOT 5요소
  const mutation = useMutation<{ id: number } | void, Error, MasterBlFormValues>({
    mutationFn: (data: MasterBlFormValues) => {
      return isEdit
        ? masterBlPort.update(id!, buildUpdateMasterBlPayload(id!, data, variant))
        : masterBlPort.create(buildCreateMasterBlPayload(data, variant));
    },
    onSuccess: (result) => {
      if (!isEdit) {
        // create: result는 { id: number }
        const newId =
          result && typeof result === "object" && "id" in result
            ? (result as { id: number }).id
            : undefined;
        if (newId != null) {
          queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", newId] });
          sessionStorage.setItem(`master-bl-entry:hot:${newId}`, "1");
          useEntryFocusStore.getState().setFocus(entryFocusKeys.masterBl(variantKey), newId);
          clearDraft(`master:${variantKey}:new`);
          detailLoadedRef.current = false;
        }
      } else {
        // update: List 자동 invalidate 금지 (§6.21, memory [feedback_list_entry_invalidate])
        // update 시 router.push 자동 이동 금지
        queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", id] });
        sessionStorage.setItem(`master-bl-entry:hot:${id}`, "1");
        useEntryFocusStore.getState().setFocus(entryFocusKeys.masterBl(variantKey), id!);
        clearDraft(`master:${variantKey}:${id}`);
        detailLoadedRef.current = false;
      }
    },
  });

  const deleteMutation = useMutation<void, Error, void>({
    mutationFn: () => masterBlPort.delete(id!),
    onSuccess: () => {
      // List 자동 invalidate 금지 (§6.21) — detail cache 제거 + draft 정리 + ref 해제
      queryClient.removeQueries({ queryKey: ["master-bl", "detail", id] });
      form.reset({
        ...createEmptyMasterBlFormValues(),
        jobDiv: variant.mode,
        bound: variant.direction ?? "EXP",
      });
      clearDraft(`master:${variantKey}:${id ?? "new"}`);
      clearDraft(`master:${variantKey}:new`);
      detailLoadedRef.current = false;
      useEntryFocusStore.getState().clearFocus(entryFocusKeys.masterBl(variantKey));
      setResetVersion((v) => v + 1);
    },
  });

  return { mutation, deleteMutation };
}
