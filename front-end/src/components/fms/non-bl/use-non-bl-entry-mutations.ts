import type { MutableRefObject } from "react";
import type { UseFormReturn } from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { nonBlPort } from "@/lib/ports";
import { useEntryFocusStore } from "@/lib/use-entry-focus-store";
import { confirm } from "@/components/confirm";
import { createEmptyNonBlFormValues } from "./non-bl-defaults";
import { buildNonBlRequest, buildNonBlUpdateRequest } from "./non-bl-submit";
import type { NonBlFormValues } from "./non-bl-schema";

export function useNonBlEntryMutations(args: {
  id: number | null;
  methods: UseFormReturn<NonBlFormValues>;
  detailLoadedRef: MutableRefObject<boolean>;
  clearDraft: (key: string) => void;
  bumpResetVersion: () => void;
}): {
  createMutation: UseMutationResult<{ id: number }, Error, NonBlFormValues>;
  updateMutation: UseMutationResult<void, Error, NonBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  isSavePending: boolean;
  handleSubmit: (data: NonBlFormValues) => Promise<void>;
  handleDelete: () => Promise<void>;
} {
  const { id, methods, detailLoadedRef, clearDraft, bumpResetVersion } = args;
  const queryClient = useQueryClient();
  const isEdit = Boolean(id);

  const createMutation = useMutation<{ id: number }, Error, NonBlFormValues>({
    mutationFn: (data) => nonBlPort.create(buildNonBlRequest(data)),
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      useEntryFocusStore.getState().setFocus("nonBl", saved.id);
      detailLoadedRef.current = false;
    },
  });

  const updateMutation = useMutation<void, Error, NonBlFormValues>({
    mutationFn: (data) => nonBlPort.update(id!, buildNonBlUpdateRequest(data)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", id] });
      // refetch 된 detail 로 form.reset 재발동
      detailLoadedRef.current = false;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => nonBlPort.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      // detail cache 제거 + ref 해제 (Delete→Cargo 클리어 race 차단, Truck 패턴 정합)
      queryClient.removeQueries({ queryKey: ["non-bl", "detail", id] });
      methods.reset(createEmptyNonBlFormValues());
      // hook 인자로 받은 id를 캡처 — hook 재호출 시점의 최신 값 사용
      clearDraft(`non::${id}`);
      clearDraft("non::new");
      detailLoadedRef.current = false;
      useEntryFocusStore.getState().clearFocus("nonBl");
      bumpResetVersion();
    },
  });

  async function handleSubmit(data: NonBlFormValues) {
    const ok = await confirm({
      title: "저장하시겠습니까?",
      variant: "default",
    });
    if (!ok) return;
    if (isEdit) {
      updateMutation.mutate(data);
    } else {
      createMutation.mutate(data);
    }
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

  const isSavePending = createMutation.isPending || updateMutation.isPending;

  return {
    createMutation,
    updateMutation,
    deleteMutation,
    isSavePending,
    handleSubmit,
    handleDelete,
  };
}
