"use client";

import { useState, useEffect, useRef }               from "react";
import { useForm, FormProvider, Controller }          from "react-hook-form";
import { zodResolver }                               from "@hookform/resolvers/zod";
import { useQuery }                                   from "@tanstack/react-query";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainTruck }     from "./tabs/main-truck";
import type { TruckBlFormValues }                    from "./truck-bl-schema";
import { TRUCK_BL_SCHEMA }                           from "./truck-bl-schema";
import { createEmptyTruckBlFormValues }              from "./truck-bl-defaults";
import { useBlDraftSync }                            from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                           from "@/lib/use-bl-draft-store";
import { TextBox, ComboBox }                         from "@/components/shared/inputs";
import { useEnumOptions }                            from "@/application/enums/use-enum";
import { truckBlPort }                               from "@/lib/ports";
import { useEntryFocusStore }                        from "@/lib/use-entry-focus-store";
import { ScreenGuard }                               from "@/components/shared/screen-guard";
import { toast }                                     from "@/lib/toast-store";
import { TruckBlEntryHeader }                        from "./truck-bl-entry-header";
import { TruckChangeBlNoModal }                      from "./truck-change-bl-no-modal";
import { useTruckBlEntryMutations }                  from "./use-truck-bl-entry-mutations";
import { useSearchTruckBl }                          from "./use-search-truck-bl";

export function TruckBLEntry() {
  const [tab, setTab] = useState("main");
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const id = useEntryFocusStore((s) => s.focus.truckBl);
  const isEdit = Boolean(id);
  const detailLoadedRef = useRef<boolean>(false);

  const clearDraft = useBLDraftStore((state) => state.clearDraft);

  const form = useForm<TruckBlFormValues>({
    resolver: zodResolver(TRUCK_BL_SCHEMA),
    defaultValues: createEmptyTruckBlFormValues(),
  });

  // id 변경 시 form.reset 재트리거를 위해 ref 초기화
  useEffect(() => {
    detailLoadedRef.current = false;
  }, [id]);

  useBlDraftSync(form, "truck::" + (id ?? "new"));

  // unmount 시 draft 제거 — 재진입(remount) 시 이전 값 복원 방지
  useEffect(() => {
    const draftKey = "truck::" + (id ?? "new");
    return () => {
      clearDraft(draftKey);
    };
  }, [clearDraft, id]);

  const { register, control } = form;

  const { options: boundOptions, placeholder: boundPlaceholder } = useEnumOptions("Bound");
  const { options: loadTypeOptions, placeholder: loadTypePlaceholder } = useEnumOptions("LoadType");
  const { options: serviceTermOptions, placeholder: serviceTermPlaceholder } = useEnumOptions("ServiceTerm");

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["truck-bl", "detail", id],
    queryFn: () => truckBlPort.getById(id!),
    enabled: isEdit,
    // 다른 화면 이동 후 재진입 시 자동 재조회 차단 — invalidateQueries(mutation 후) 시에는 active query 이므로 refetch 정상 동작
    staleTime: Infinity,
    refetchOnMount: false,
    // refetch 결과가 직전 cache와 deep equal이어도 새 reference를 발급해
    // useEffect(detail)의 form.reset이 항상 트리거되도록 강제
    structuralSharing: false,
  });

  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    form.reset({
      ...createEmptyTruckBlFormValues(),
      truckBlNo:          detail.hblNo             ?? "",
      bound:              detail.bound              ?? "",
      loadType:           detail.loadType           ?? "",
      serviceTerm:        detail.serviceTerm        ?? "",
      shipperCode:        detail.shipperCode        ?? "",
      consigneeCode:      detail.consigneeCode      ?? "",
      notifyCode:         detail.notifyCode         ?? "",
      polCode:            detail.polCode            ?? "",
      polName:            "",
      podCode:            detail.podCode            ?? "",
      podName:            "",
      etd:                detail.etd                ?? "",
      eta:                detail.eta                ?? "",
      voyNo:              detail.voyageNo           ?? "",
      pkgQty:             detail.pkgQty             != null ? detail.pkgQty : undefined,
      pkgUnit:            detail.pkgUnit            ?? "",
      grossWeightKg:      detail.grossWeightKg      != null ? detail.grossWeightKg : undefined,
      cbm:                detail.cbm                != null ? detail.cbm : undefined,
      chargeWeightKg:     detail.chargeWeightKg     != null ? detail.chargeWeightKg : undefined,
      actualCustomerCode: detail.actualCustomerCode ?? "",
      operatorCode:       detail.operatorCode       ?? "",
      teamCode:           detail.teamCode           ?? "",
      salesManCode:       detail.salesManCode       ?? "",
      settlePartnerCode:  detail.settlePartnerCode  ?? "",
      truckerCode:        detail.truckerCode        ?? "",
      truckerPic:         detail.truckerPic         ?? "",
      pickupDate:         detail.pickupDate         ?? "",
    });
  }, [detail, form]);

  const { deleteMutation, isSavePending, handleSubmit, handleDelete } = useTruckBlEntryMutations({
    id: id ?? null,
    form,
    detailLoadedRef,
    clearDraft,
  });

  const { handleSearch } = useSearchTruckBl({ form, id: id ?? null, detailLoadedRef });

  function handleResetEntry() {
    form.reset(createEmptyTruckBlFormValues());
    clearDraft("truck::" + (id ?? "new"));
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus("truckBl");
  }

  function handleChangeBlNo() {
    if (!isEdit || !id) {
      toast.info("먼저 Truck B/L을 조회해주세요.");
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  const isLoading = isDetailFetching || isSavePending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : isSavePending ? "저장 중..." : "조회 중...";

  return (
    <FormProvider {...form}>
    <ScreenGuard visible={isLoading} message={loadingMessage} />
    <form
      onSubmit={form.handleSubmit(handleSubmit)}
      onKeyDown={(e) => {
        // textarea 줄바꿈은 보존, 그 외 Enter는 implicit form submission 차단
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
      style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}
    >
      <TruckBlEntryHeader
        isEdit={isEdit}
        isSavePending={isSavePending}
        isDeletePending={deleteMutation.isPending}
        onNew={handleResetEntry}
        onSearch={handleSearch}
        onSave={() => {
          if (!isEdit) {
            toast.info("먼저 Truck B/L을 조회해주세요.");
            return;
          }
          form.handleSubmit(handleSubmit)();
        }}
        onDelete={handleDelete}
        onChangeBlNo={handleChangeBlNo}
      />

      {/* Toolbar: 4필드 — gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">Truck B/L No</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...register("truckBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Bound</div>
          <div className="field__input">
            <Controller
              name="bound"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={boundOptions}
                  placeholder={boundPlaceholder}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Load Type</div>
          <div className="field__input">
            <Controller
              name="loadType"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={loadTypeOptions}
                  placeholder={loadTypePlaceholder}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Service Term</div>
          <div className="field__input">
            <Controller
              name="serviceTerm"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={serviceTermOptions}
                  placeholder={serviceTermPlaceholder}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
      </div>

      {/* Tabbar — 2 tabs only */}
      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map((t) => (
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
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MainTruck   active={tab === "main"}    /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab active={tab === "freight"} /></div>
    </form>
    {isEdit && id && (
      <TruckChangeBlNoModal
        truckBlId={id}
        currentHblNo={detail?.hblNo}
        isOpen={isChangeBlNoModalOpen}
        onClose={() => setIsChangeBlNoModalOpen(false)}
        onChanged={() => { detailLoadedRef.current = false; }}
      />
    )}
    </FormProvider>
  );
}
