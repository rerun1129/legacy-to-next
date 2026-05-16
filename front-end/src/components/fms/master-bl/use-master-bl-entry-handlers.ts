import type { Dispatch, MutableRefObject, RefObject, SetStateAction } from "react";
import type { UseFormReturn }   from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import type { QueryClient }     from "@tanstack/react-query";
import { confirm }              from "@/components/confirm";
import { masterBlPort }         from "@/lib/ports";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { toast }                from "@/lib/toast-store";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import type { MasterBlFormValues } from "./master-bl-schema";

export function useMasterBlEntryHandlers(args: {
  id: number | undefined;
  variantKey: string;
  variant: MasterVariantConfig;
  form: UseFormReturn<MasterBlFormValues>;
  formRef: RefObject<HTMLFormElement | null>;
  detailLoadedRef: MutableRefObject<boolean>;
  didRestoreFromDraftRef: MutableRefObject<boolean>;
  isEdit: boolean;
  mutation: UseMutationResult<{ id: number } | void, Error, MasterBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  setResetVersion: Dispatch<SetStateAction<number>>;
  setIsChangeBlNoModalOpen: Dispatch<SetStateAction<boolean>>;
  queryClient: QueryClient;
  clearDraft: (key: string) => void;
}): {
  handleSearchBl: () => void;
  handleResetEntry: () => void;
  handleChangeBlNo: () => void;
  handleSave: (raw: MasterBlFormValues) => Promise<void>;
  handleDelete: () => Promise<void>;
} {
  const {
    id,
    variantKey,
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
    queryClient,
    clearDraft,
  } = args;

  function handleSearchBl() {
    const mblValue = form.getValues("mblNo")?.trim();
    if (!mblValue) return;

    masterBlPort
      .findByMblNo(mblValue)
      .then((ids) => {
        if (ids.length === 0) {
          alert("해당 B/L을 찾을 수 없습니다.");
          return;
        }
        if (ids.length > 1) {
          alert("동일 MBL No. 다건 발견 — List 화면에서 선택해주세요.");
          return;
        }
        const targetId = ids[0];
        if (targetId === id) {
          // 동일 id 재조회: detail cache invalidate 후 useEffect가 form.reset을 다시 실행
          queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", id] });
          clearDraft(`master:${variantKey}:${id}`);
          detailLoadedRef.current = false;
          didRestoreFromDraftRef.current = false;
        } else {
          // 다른 id: focus 변경 → useQuery 자동 트리거 → useEffect에서 form.reset
          queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", targetId] });
          clearDraft(`master:${variantKey}:${targetId}`);
          sessionStorage.setItem(`master-bl-entry:hot:${targetId}`, "1");
          useEntryFocusStore.getState().setFocus(entryFocusKeys.masterBl(variantKey), targetId);
        }
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : String(err);
        alert(`B/L 조회 중 오류가 발생했습니다: ${message}`);
      });
  }

  function handleResetEntry() {
    form.reset({
      ...createEmptyMasterBlFormValues(),
      jobDiv: variant.mode,
      bound: variant.direction ?? "EXP",
    });
    clearDraft(`master:${variantKey}:${id ?? "new"}`);
    clearDraft(`master:${variantKey}:new`);
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus(entryFocusKeys.masterBl(variantKey));
    formRef.current?.reset();
    setResetVersion((v) => v + 1);
  }

  function handleChangeBlNo() {
    if (!isEdit || !id) {
      toast.info("먼저 Master B/L을 조회해주세요.");
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  // Save confirm 모달 (House 패턴 정합 — Non B/L 16dbc0b 패턴)
  async function handleSave(raw: MasterBlFormValues) {
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
      title: "삭제하시겠습니까?",
      description: "삭제된 데이터는 복구할 수 없습니다.",
      variant: "destructive",
      confirmText: "삭제",
    });
    if (!ok) return;
    deleteMutation.mutate();
  }

  return { handleSearchBl, handleResetEntry, handleChangeBlNo, handleSave, handleDelete };
}
