import type { Dispatch, MutableRefObject, RefObject, SetStateAction } from "react";
import type { UseFormReturn }   from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import type { QueryClient }     from "@tanstack/react-query";
import { useTranslations }      from "next-intl";
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
  const t  = useTranslations("fms.masterBl.entry.msg");
  const tc = useTranslations("common");

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
          toast.info(t("noResults"));
          return;
        }
        if (ids.length > 1) {
          toast.info(t("multipleFound"));
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
        toast.error(t("searchError", { message }));
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
      toast.info(t("searchBlFirst"));
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  // Save confirm 모달 (House 패턴 정합 — Non B/L 16dbc0b 패턴)
  async function handleSave(raw: MasterBlFormValues) {
    const ok = await confirm({
      title: t("confirmSave"),
      variant: "default",
    });
    if (!ok) return;
    mutation.mutate(raw);
  }

  async function handleDelete() {
    if (!isEdit) return;
    const ok = await confirm({
      title: t("confirmDelete"),
      description: t("deleteWarning"),
      variant: "destructive",
      confirmText: tc("delete"),
    });
    if (!ok) return;
    deleteMutation.mutate();
  }

  return { handleSearchBl, handleResetEntry, handleChangeBlNo, handleSave, handleDelete };
}
