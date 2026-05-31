import { useState, useEffect, useRef, useCallback } from "react";
import { useForm }                                from "react-hook-form";
import { zodResolver }                           from "@hookform/resolvers/zod";
import { useQuery }                              from "@tanstack/react-query";
import { useBlDraftSync }                        from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                       from "@/lib/use-bl-draft-store";
import { useEnumOptions }                        from "@/application/enums/use-enum";
import { truckBlPort }                           from "@/lib/ports";
import { useEntryFocusStore }                    from "@/lib/use-entry-focus-store";
import { toast }                                 from "@/lib/toast-store";
import type { TruckBlFormValues }                from "./truck-bl-schema";
import { TRUCK_BL_SCHEMA }                       from "./truck-bl-schema";
import { createEmptyTruckBlFormValues }          from "./truck-bl-defaults";
import { useTruckBlEntryMutations }              from "./use-truck-bl-entry-mutations";
import { useSearchTruckBl }                      from "./use-search-truck-bl";

export function useTruckBlEntry() {
  const [tab, setTab] = useState("main");
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const [resetVersion, setResetVersion] = useState(0);
  const bumpResetVersion = useCallback(() => setResetVersion(v => v + 1), []);
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

  const { didRestoreFromDraftRef } = useBlDraftSync(form, "truck::" + (id ?? "new"));

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
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
    refetchOnMount: false,
    // refetch 결과가 직전 cache와 deep equal이어도 새 reference를 발급해
    // useEffect(detail)의 form.reset이 항상 트리거되도록 강제
    structuralSharing: false,
  });

  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    // 이번 mount에서 useBlDraftSync가 stored draft로 실제 form.reset을 호출했으면 detail로 덮어쓰지 않음
    if (didRestoreFromDraftRef.current) return;
    form.reset({
      ...createEmptyTruckBlFormValues(),
      truckBlNo:          detail.hblNo             ?? "",
      bound:              detail.bound              ?? "",
      loadType:           detail.loadType           ?? "",
      serviceTerm:        detail.serviceTerm        ?? "",
      shipperCode:        detail.shipperCode        ?? "",
      shipperAddr:        detail.shipperAddr        ?? "",
      consigneeCode:      detail.consigneeCode      ?? "",
      consigneeAddr:      detail.consigneeAddr      ?? "",
      notifyCode:         detail.notifyCode         ?? "",
      notifyAddr:         detail.notifyAddr         ?? "",
      docPartnerCode:     detail.docPartnerCode     ?? "",
      docPartnerAddress:  detail.docPartnerAddress  ?? "",
      polCode:            detail.polCode            ?? "",
      polName:            "",
      podCode:            detail.podCode            ?? "",
      podName:            "",
      etd:                detail.etd                ?? "",
      eta:                detail.eta                ?? "",
      voyNo:              detail.voyageNo           ?? "",
      vesselName:         detail.vesselName         ?? "",
      pkgQty:             detail.pkgQty             != null ? detail.pkgQty : undefined,
      pkgUnit:            detail.pkgUnit            ?? "",
      weightUnit:         detail.weightUnit         ?? "",
      grossWeightKg:      detail.grossWeightKg      != null ? detail.grossWeightKg : undefined,
      cbm:                detail.cbm                != null ? detail.cbm : undefined,
      chargeWeightKg:     detail.chargeWeightKg     != null ? detail.chargeWeightKg : undefined,
      actualCustomerCode: detail.actualCustomerCode ?? "",
      operatorCode:       detail.operatorCode       ?? "",
      teamCode:           detail.teamCode           ?? "",
      teamName:           detail.teamName           ?? "",
      salesManCode:       detail.salesManCode       ?? "",
      settlePartnerCode:  detail.settlePartnerCode  ?? "",
      truckerCode:        detail.truckerCode        ?? "",
      truckerPic:         detail.truckerPic         ?? "",
      pickupDate:         detail.pickupDate         ?? "",
      hsCode:             detail.hsCode             ?? "",
      hsCodeName:         detail.hsCodeName         ?? "",
      truckOrders: detail.truckOrders?.map((o) => ({
        id:            o.id,
        truckOrderNo:  o.truckOrderNo   ?? "",
        pkgQty:        o.pkgQty         != null ? String(o.pkgQty)         : "",
        pkgUnit:       o.pkgUnit        ?? "",
        grossWeightKg: o.grossWeightKg  != null ? String(o.grossWeightKg)  : "",
        cbm:           o.cbm            != null ? String(o.cbm)            : "",
        truckNo:       o.truckNo        ?? "",
        truckType:     o.truckType      ?? "",
        driver:        o.driver         ?? "",
        mobileNo:      o.mobileNo       ?? "",
        containerNo:   o.containerNo    ?? "",
        containerType: o.containerType  ?? "",
        sealNo1:       o.sealNo1        ?? "",
        sealNo2:       o.sealNo2        ?? "",
        sealNo3:       o.sealNo3        ?? "",
      })) ?? [],
      marks:       detail.desc?.marks       ?? "",
      description: detail.desc?.description ?? "",
      descClause1: detail.desc?.descClause1 ?? "",
      descClause2: detail.desc?.descClause2 ?? "",
      remark:      detail.remark            ?? "",
      dimensionDivisor: detail.volumeDivisor ?? "",
      dimensions: detail.dims?.map((d, idx) => ({
        id:     d.id ?? idx,
        length: d.lengthCm       != null ? String(d.lengthCm)       : "",
        width:  d.widthCm        != null ? String(d.widthCm)        : "",
        height: d.heightCm       != null ? String(d.heightCm)       : "",
        qty:    d.quantity       != null ? String(d.quantity)       : "",
        cbm:    d.cbm            != null ? String(d.cbm)            : "",
        volWt:  d.volumeWeightKg != null ? String(d.volumeWeightKg) : "",
      })) ?? [],
    });
  }, [detail, form, didRestoreFromDraftRef]);

  const { deleteMutation, isSavePending, handleSubmit, handleDelete } = useTruckBlEntryMutations({
    id: id ?? null,
    form,
    detailLoadedRef,
    clearDraft,
    bumpResetVersion,
  });

  const resetDetailLoaded = useCallback(() => {
    detailLoadedRef.current = false;
  }, []);

  const { handleSearch } = useSearchTruckBl({ form, id: id ?? null, detailLoadedRef });

  function handleResetEntry() {
    form.reset(createEmptyTruckBlFormValues());
    clearDraft("truck::" + (id ?? "new"));
    clearDraft("truck::new");
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus("truckBl");
    bumpResetVersion();
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

  return {
    form,
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
    resetVersion,
    isChangeBlNoModalOpen,
    setIsChangeBlNoModalOpen,
    resetDetailLoaded,
    handleSubmit,
    handleDelete,
    handleSearch,
    handleResetEntry,
    handleChangeBlNo,
    boundOptions,
    boundPlaceholder,
    loadTypeOptions,
    loadTypePlaceholder,
    serviceTermOptions,
    serviceTermPlaceholder,
  };
}
