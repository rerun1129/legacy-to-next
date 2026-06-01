import { useState, useEffect, useRef, useCallback } from "react";
import { useForm }                                from "react-hook-form";
import { zodResolver }                           from "@hookform/resolvers/zod";
import { useQuery }                              from "@tanstack/react-query";
import { useTranslations }                       from "next-intl";
import { useBlDraftSync }                        from "@/lib/use-bl-draft-sync";
import { useBLDraftStore, blDraftStore }         from "@/lib/use-bl-draft-store";
import { useEnumOptions }                        from "@/application/enums/use-enum";
import { nonBlPort }                             from "@/lib/ports";
import { useEntryFocusStore, entryFocusKeys }    from "@/lib/use-entry-focus-store";
import { toast }                                 from "@/lib/toast-store";
import type { NonBlFormValues }                  from "./non-bl-schema";
import { NON_BL_SCHEMA }                         from "./non-bl-schema";
import { createEmptyNonBlFormValues }            from "./non-bl-defaults";
import { mapNonBlDetailToFormValues }            from "./map-non-bl-detail";
import { useSearchNonBl }                        from "./use-search-non-bl";
import { useNonBlEntryMutations }                from "./use-non-bl-entry-mutations";

export function useNonBlEntry() {
  const t = useTranslations("fms.nonBl.entry.msg");
  const [tab, setTab] = useState("main");
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const [resetVersion, setResetVersion] = useState(0);
  const bumpResetVersion = useCallback(() => setResetVersion(v => v + 1), []);
  const id = useEntryFocusStore((s) => s.focus.nonBl);
  const nonce = useEntryFocusStore((s) => s.resetNonce[entryFocusKeys.nonBl]);
  const isEdit = Boolean(id);
  const detailLoadedRef = useRef<boolean>(false);
  const prevNonceRef = useRef<number | undefined>(undefined);

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
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
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

  // B/L Copy 재초기화 신호 구독.
  // focus 불변(new→new)일 때 useBlDraftSync의 key 변경이 일어나지 않으므로
  // nonce 증가를 별도 트리거로 삼아 최신 non::new draft로 강제 reset한다.
  // set-state-in-effect 금지 준수 — useState setter 미사용, ref+methods.reset만 사용.
  useEffect(() => {
    // 초기 마운트(prevNonceRef가 아직 세팅되지 않은 시점)는 무시 — useBlDraftSync가 처리
    if (prevNonceRef.current === undefined) {
      prevNonceRef.current = nonce;
      return;
    }
    // nonce가 실제로 증가했을 때만 발동
    if (nonce === prevNonceRef.current) return;
    prevNonceRef.current = nonce;

    const draft = blDraftStore.getState().getDraft("non::new");
    if (draft !== undefined) {
      // detail 덮어쓰기 방지 + form reset.
      // main tab 리마운트는 non-bl-entry.tsx의 key에 nonce가 포함되어 자동 처리됨.
      didRestoreFromDraftRef.current = true;
      detailLoadedRef.current = true;
      methods.reset(draft as NonBlFormValues);
    }
  // methods/didRestoreFromDraftRef/detailLoadedRef는 컴포넌트 수명 내 안정 참조(ref).
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nonce]);

  const resetDetailLoaded = useCallback(() => {
    detailLoadedRef.current = false;
  }, []);

  const { handleSearch } = useSearchNonBl({ methods, id: id ?? null, detailLoadedRef });

  const { deleteMutation, isSavePending, handleSubmit, handleDelete } = useNonBlEntryMutations({
    id: id ?? null,
    methods,
    detailLoadedRef,
    clearDraft,
    bumpResetVersion,
  });

  function handleResetEntry() {
    methods.reset(createEmptyNonBlFormValues());
    clearDraft(`non::${id ?? "new"}`);
    clearDraft("non::new");
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus("nonBl");
    bumpResetVersion();
  }

  function handleChangeBlNo() {
    if (!isEdit || !id) {
      toast.info(t("searchBlFirst"));
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  const isLoading = isDetailFetching || isSavePending || deleteMutation.isPending;

  return {
    methods,
    register,
    control,
    isEdit,
    id,
    detail,
    isLoading,
    deleteMutation,
    isSavePending,
    tab,
    setTab,
    resetVersion,
    nonce,
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
