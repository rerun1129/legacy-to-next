import type { Dispatch, MutableRefObject, RefObject, SetStateAction } from "react";
import type { UseFormReturn }    from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import { confirm }               from "@/components/confirm";
import { toast }                 from "@/lib/toast-store";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import type { BLVariantConfig }  from "@/lib/bl-variants";
import { createEmptyHouseBlFormValues } from "./house-bl-defaults";
import { useSearchBl }           from "./use-search-bl";
import type { HouseBlFormValues } from "./house-bl-schema";

export function useHouseBlEntryHandlers(args: {
  id: number | undefined;
  variant: BLVariantConfig;
  form: UseFormReturn<HouseBlFormValues>;
  formRef: RefObject<HTMLFormElement | null>;
  detailLoadedRef: MutableRefObject<boolean>;
  didRestoreFromDraftRef: MutableRefObject<boolean>;
  isEdit: boolean;
  mutation: UseMutationResult<unknown, Error, HouseBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  setResetVersion: Dispatch<SetStateAction<number>>;
  setIsChangeBlNoModalOpen: Dispatch<SetStateAction<boolean>>;
  clearDraft: (key: string) => void;
}): {
  handleSearchBl: () => void;
  handleResetEntry: () => void;
  handleChangeBlNo: () => void;
  handleSubmit: (raw: HouseBlFormValues) => Promise<void>;
  handleDelete: () => Promise<void>;
} {
  const {
    id,
    variant,
    form,
    formRef,
    detailLoadedRef,
    didRestoreFromDraftRef,
    isEdit,
    mutation,
    deleteMutation,
    setResetVersion,
    setIsChangeBlNoModalOpen,
    clearDraft,
  } = args;

  const { handleSearchBl } = useSearchBl(form, variant, {
    id,
    onAfterFound: (_targetId, _sameAsCurrent) => {
      // useSearchBl은 외부 ref를 직접 mutate할 수 없으므로 콜백으로 위임
      detailLoadedRef.current = false;
      didRestoreFromDraftRef.current = false;
    },
  });

  function handleResetEntry() {
    form.reset(createEmptyHouseBlFormValues());
    clearDraft(`house:${variant.key}:${id ?? "new"}`);
    clearDraft(`house:${variant.key}:new`);
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus(entryFocusKeys.houseBl(variant.key));
    formRef.current?.reset();
    setResetVersion((v) => v + 1);
  }

  function handleChangeBlNo() {
    if (!isEdit || !id) {
      toast.info("먼저 House B/L을 조회해주세요.");
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  async function handleSubmit(raw: HouseBlFormValues) {
    const ok = await confirm({
      title: "저장하시겠습니까?",
      variant: "default",
    });
    if (!ok) return;
    mutation.mutate(raw);
  }

  async function handleDelete() {
    if (!isEdit) return;
    const ok = await confirm({
      variant: "destructive",
      title: "삭제하시겠습니까?",
      confirmText: "삭제",
      description: "삭제된 데이터는 복구할 수 없습니다.",
    });
    if (!ok) return;
    deleteMutation.mutate();
  }

  return { handleSearchBl, handleResetEntry, handleChangeBlNo, handleSubmit, handleDelete };
}
