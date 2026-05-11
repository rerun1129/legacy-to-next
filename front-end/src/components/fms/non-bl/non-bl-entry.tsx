"use client";

import { useState, useEffect, useRef }               from "react";
import { useForm, FormProvider, Controller }           from "react-hook-form";
import { zodResolver }                                from "@hookform/resolvers/zod";
import { useQuery }                                   from "@tanstack/react-query";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }     from "./tabs/main-non-bl";
import type { NonBlFormValues }                       from "./non-bl-schema";
import { NON_BL_SCHEMA }                              from "./non-bl-schema";
import { createEmptyNonBlFormValues }                 from "./non-bl-defaults";
import { useBlDraftSync }                             from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                            from "@/lib/use-bl-draft-store";
import { TextBox, ComboBox }                          from "@/components/shared/inputs";
import { useEnumOptions }                             from "@/application/enums/use-enum";
import { nonBlPort }                                  from "@/lib/ports";
import { useEntryFocusStore }                         from "@/lib/use-entry-focus-store";
import { ScreenGuard }                                from "@/components/shared/screen-guard";
import { ChangeBlNoModal }                            from "./change-bl-no-modal";
import { toast }                                      from "@/lib/toast-store";
import { mapNonBlDetailToFormValues }                 from "./map-non-bl-detail";
import { useSearchNonBl }                             from "./use-search-non-bl";
import { useNonBlEntryMutations }                     from "./use-non-bl-entry-mutations";
import { NonBlEntryHeader }                           from "./non-bl-entry-header";

export function NonBLEntry() {
  const [tab, setTab] = useState("main");
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const id = useEntryFocusStore((s) => s.focus.nonBl);
  const isEdit = Boolean(id);
  const detailLoadedRef = useRef<boolean>(false);

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const methods = useForm<NonBlFormValues>({
    resolver: zodResolver(NON_BL_SCHEMA),
    defaultValues: createEmptyNonBlFormValues(),
  });

  // id 변경 시 form.reset 재트리거를 위해 ref 초기화
  useEffect(() => {
    detailLoadedRef.current = false;
  }, [id]);

  useBlDraftSync(methods, `non::${id ?? "new"}`);

  // unmount 시 draft 제거 — 재진입(remount) 시 이전 값 복원 방지
  useEffect(() => {
    const draftKey = `non::${id ?? "new"}`;
    return () => {
      clearDraft(draftKey);
    };
  }, [clearDraft, id]);

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
    methods.reset(mapNonBlDetailToFormValues(detail));
  }, [detail, methods]);

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

  return (
    <FormProvider {...methods}>
    <ScreenGuard visible={isLoading} message={loadingMessage} />
    <form
      onSubmit={methods.handleSubmit(handleSubmit)}
      onKeyDown={(e) => {
        // textarea 줄바꿈은 보존, 그 외 Enter는 implicit form submission 차단
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
      style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}
    >
      <NonBlEntryHeader
        isEdit={isEdit}
        isSavePending={isSavePending}
        isDeletePending={deleteMutation.isPending}
        onNew={handleResetEntry}
        onSearch={handleSearch}
        onSave={methods.handleSubmit(handleSubmit)}
        onDelete={handleDelete}
        onChangeBlNo={handleChangeBlNo}
      />

      {/* gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(6, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">Non B/L No</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...register("nonBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Work Division</div>
          <div className="field__input">
            <Controller
              name="workDiv"
              control={control}
              render={({ field }) => (
                <ComboBox variant="panel" options={workDivOptions} placeholder={workDivPlaceholder} value={field.value} onChange={field.onChange} />
              )}
            />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Bound</div>
          <div className="field__input">
            <Controller
              name="bound"
              control={control}
              render={({ field }) => (
                <ComboBox variant="panel" options={boundOptions} placeholder={boundPlaceholder} value={field.value} onChange={field.onChange} />
              )}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Ref. No.</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Ref. No." {...register("refNo")} />
          </div>
        </div>
      </div>

      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map(t => (
          <button
            key={t.key}
            type="button"
            className={`tabbar__tab${tab === t.key ? " is-active" : ""}`}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
      </div>

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MainNonBL    active={tab === "main"}    /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab   active={tab === "freight"} /></div>
    </form>
    {isEdit && id && (
      <ChangeBlNoModal
        houseBlId={id}
        currentHblNo={detail?.hblNo}
        isOpen={isChangeBlNoModalOpen}
        onClose={() => setIsChangeBlNoModalOpen(false)}
        onChanged={() => { detailLoadedRef.current = false; }}
      />
    )}
    </FormProvider>
  );
}
