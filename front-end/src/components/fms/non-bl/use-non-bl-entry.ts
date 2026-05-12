import { useState, useEffect, useRef, useCallback } from "react";
import { useForm }                                from "react-hook-form";
import { zodResolver }                           from "@hookform/resolvers/zod";
import { useQuery }                              from "@tanstack/react-query";
import { useBlDraftSync }                        from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                       from "@/lib/use-bl-draft-store";
import { useEnumOptions }                        from "@/application/enums/use-enum";
import { nonBlPort }                             from "@/lib/ports";
import { useEntryFocusStore }                    from "@/lib/use-entry-focus-store";
import { toast }                                 from "@/lib/toast-store";
import type { NonBlFormValues }                  from "./non-bl-schema";
import { NON_BL_SCHEMA }                         from "./non-bl-schema";
import { createEmptyNonBlFormValues }            from "./non-bl-defaults";
import { mapNonBlDetailToFormValues }            from "./map-non-bl-detail";
import { useSearchNonBl }                        from "./use-search-non-bl";
import { useNonBlEntryMutations }                from "./use-non-bl-entry-mutations";

export function useNonBlEntry() {
  const [tab, setTab] = useState("main");
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const id = useEntryFocusStore((s) => s.focus.nonBl);
  const isEdit = Boolean(id);
  const detailLoadedRef = useRef<boolean>(false);

  const clearDraft = useBLDraftStore((state) => state.clearDraft);

  const methods = useForm<NonBlFormValues>({
    resolver: zodResolver(NON_BL_SCHEMA),
    defaultValues: createEmptyNonBlFormValues(),
  });

  // id 변경 시 form.reset 재트리거를 위해 ref 초기화
  useEffect(() => {
    detailLoadedRef.current = false;
  }, [id]);

  const { didRestoreFromDraftRef } = useBlDraftSync(methods, `non::${id ?? "new"}`);

  const { register, control } = methods;

  // status: 백엔드 관리 필드 — UI 노출 없이 form에만 등록
  register("status");

  const { options: workDivOptions, placeholder: workDivPlaceholder } = useEnumOptions("WorkDivision");
  const { options: boundOptions, placeholder: boundPlaceholder } = useEnumOptions("Bound");

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["non-bl", "detail", id],
    queryFn: () => nonBlPort.getById(id!),
    enabled: isEdit,
    // 다른 화면 이동 후 재진입 시 자동 재조회 차단 — invalidateQueries(mutation 후) 시에는 active query 이므로 refetch 정상 동작
    staleTime: Infinity,
    refetchOnMount: false,
    // refetch 결과가 직전 cache 와 deep equal 이어도 새 reference 를 발급해
    // useEffect(detail) 의 form.reset 가 항상 트리거되도록 강제
    structuralSharing: false,
  });

  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    // 이번 mount에서 useBlDraftSync가 stored draft로 실제 form.reset을 호출했으면 detail로 덮어쓰지 않음
    if (didRestoreFromDraftRef.current) return;
    methods.reset(mapNonBlDetailToFormValues(detail));
  }, [detail, methods, didRestoreFromDraftRef]);

  const resetDetailLoaded = useCallback(() => {
    detailLoadedRef.current = false;
  }, []);

  const { handleSearch } = useSearchNonBl({ methods, id: id ?? null, detailLoadedRef });

  const { deleteMutation, isSavePending, handleSubmit, handleDelete } = useNonBlEntryMutations({
    id: id ?? null,
    methods,
    detailLoadedRef,
    clearDraft,
  });

  function handleResetEntry() {
    methods.reset(createEmptyNonBlFormValues());
    clearDraft(`non::${id ?? "new"}`);
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus("nonBl");
  }

  function handleChangeBlNo() {
    if (!isEdit || !id) {
      toast.info("먼저 Non B/L을 조회해주세요.");
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  const isLoading = isDetailFetching || isSavePending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : isSavePending ? "저장 중..." : "조회 중...";

  return {
    methods,
    register,
    control,
    isEdit,
    id,
    detail,
    isLoading,
    loadingMessage,
    deleteMutation,
    isSavePending,
    tab,
    setTab,
    isChangeBlNoModalOpen,
    setIsChangeBlNoModalOpen,
    resetDetailLoaded,
    handleSubmit,
    handleDelete,
    handleSearch,
    handleResetEntry,
    handleChangeBlNo,
    workDivOptions,
    workDivPlaceholder,
    boundOptions,
    boundPlaceholder,
  };
}
