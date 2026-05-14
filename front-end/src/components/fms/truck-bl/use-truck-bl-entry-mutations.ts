import type { MutableRefObject } from "react";
import type { UseFormReturn } from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { truckBlPort } from "@/lib/ports";
import { useEntryFocusStore } from "@/lib/use-entry-focus-store";
import { confirm } from "@/components/confirm";
import { createEmptyTruckBlFormValues } from "./truck-bl-defaults";
import { buildTruckBlCreateRequest, buildTruckBlUpdateRequest } from "./truck-bl-submit";
import type { TruckBlFormValues } from "./truck-bl-schema";

export function useTruckBlEntryMutations(args: {
  id: number | null;
  form: UseFormReturn<TruckBlFormValues>;
  detailLoadedRef: MutableRefObject<boolean>;
  clearDraft: (key: string) => void;
  bumpResetVersion: () => void;
}): {
  createMutation: UseMutationResult<{ id: number }, Error, TruckBlFormValues>;
  updateMutation: UseMutationResult<void, Error, TruckBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  isSavePending: boolean;
  handleSubmit: (data: TruckBlFormValues) => Promise<void>;
  handleDelete: () => Promise<void>;
} {
  const { id, form, detailLoadedRef, clearDraft, bumpResetVersion } = args;
  const queryClient = useQueryClient();
  const isEdit = Boolean(id);

  const createMutation = useMutation<{ id: number }, Error, TruckBlFormValues>({
    mutationFn: (data) => truckBlPort.create(buildTruckBlCreateRequest(data)),
    onSuccess: (saved) => {
      // detail cache 세팅 후 focus 이동 — List 자동 invalidate 금지 (§6.21)
      queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", saved.id] });
      // hot-marker: List 화면이 진입 시 하이라이트에 사용 (§6.16)
      sessionStorage.setItem(`truck-bl-entry:hot:${saved.id}`, "1");
      useEntryFocusStore.getState().setFocus("truckBl", saved.id);
      detailLoadedRef.current = false;
    },
  });

  const updateMutation = useMutation<void, Error, TruckBlFormValues>({
    mutationFn: (data) => truckBlPort.update(id!, buildTruckBlUpdateRequest(data)),
    onSuccess: () => {
      // List 자동 invalidate 금지 (§6.21) — detail만 갱신
      queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", id] });
      // refetch된 detail로 form.reset 재발동 (§10 86d4406 onChanged 콜백 패턴)
      detailLoadedRef.current = false;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => truckBlPort.delete(id!),
    onSuccess: () => {
      // 삭제 후 detail 자동 재조회 금지 — invalidateQueries는 active query를 refetch해
      // 이미 삭제된 id로 GET을 발생시켜 404(RESOURCE_NOT_FOUND)를 일으킨다.
      // 1) focus 먼저 해제해 isEdit=false → detail query 비활성화
      useEntryFocusStore.getState().clearFocus("truckBl");
      // 2) refetch 없이 캐시만 제거
      queryClient.removeQueries({ queryKey: ["truck-bl", "detail", id] });
      form.reset(createEmptyTruckBlFormValues());
      clearDraft(`truck::${id}`);
      clearDraft("truck::new");
      detailLoadedRef.current = false;
      bumpResetVersion();
    },
  });

  async function handleSubmit(data: TruckBlFormValues) {
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
