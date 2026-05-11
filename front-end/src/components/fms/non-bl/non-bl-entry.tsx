"use client";

import { useState, useEffect, useRef }                from "react";
import { useForm, FormProvider, Controller }            from "react-hook-form";
import { useMutation, useQuery, useQueryClient }       from "@tanstack/react-query";
import { useRouter }                                   from "next/navigation";
import { Save, Trash2, Package, FilePlus, Search, Copy, RefreshCw } from "lucide-react";
import { FreightTab }     from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }      from "./tabs/main-non-bl";
import type { NonBlFormValues }                        from "./non-bl-schema";
import { createEmptyNonBlFormValues }                  from "./non-bl-defaults";
import { buildNonBlRequest, buildNonBlUpdateRequest }   from "./non-bl-submit";
import { useBlDraftSync }                              from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                             from "@/lib/use-bl-draft-store";
import { TextBox, ComboBox }                            from "@/components/shared/inputs";
import { useEnumOptions }                              from "@/application/enums/use-enum";
import { nonBlPort }                                   from "@/lib/ports";
import { toast }                                       from "@/lib/toast-store";
import { useEntryFocusStore }                          from "@/lib/use-entry-focus-store";
import { ScreenGuard }                                 from "@/components/shared/screen-guard";
import { ChangeBlNoModal }                             from "./change-bl-no-modal";

export function NonBLEntry() {
  const [tab, setTab] = useState("main");
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const id = useEntryFocusStore((s) => s.focus.nonBl);
  const isEdit = Boolean(id);
  const queryClient = useQueryClient();
  const router = useRouter();
  const detailLoadedRef = useRef<boolean>(false);

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const methods = useForm<NonBlFormValues>({
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
    // refetch 결과가 직전 cache 와 deep equal 이어도 새 reference 를 발급해
    // useEffect(detail) 의 form.reset 가 항상 트리거되도록 강제
    structuralSharing: false,
  });

  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    methods.reset({
      ...createEmptyNonBlFormValues(),
      nonBlNo:            detail.hblNo ?? "",
      workDiv:            detail.workDivision ?? "",
      bound:              detail.bound ?? "",
      refNo:              detail.originalBlRef ?? "",
      shipperCode:        detail.shipperCode ?? "",
      consigneeCode:      detail.consigneeCode ?? "",
      notifyCode:         detail.notifyCode ?? "",
      settlePartnerCode:  detail.settlePartnerCode ?? "",
      actualCustomerCode: detail.actualCustomerCode ?? "",
      linerCode:          detail.linerCode ?? "",
      linerName:          detail.linerName ?? "",
      vesselName:         detail.vesselName ?? "",
      voyNo:              detail.voyageNo ?? "",
      etd:                detail.etd ?? "",
      eta:                detail.eta ?? "",
      polCode:            detail.polCode ?? "",
      podCode:            detail.podCode ?? "",
      finalDestCode:      detail.finalDestCode ?? "",
      finalDestName:      detail.finalDestName ?? "",
      finalEta:           detail.finalEta ?? "",
      mainItem:           detail.mainItemName ?? "",
      hsCode:             detail.hsCode ?? "",
      cargoQty:           detail.pkgQty,
      pkgUnit:            detail.pkgUnit ?? "",
      weightUnit:         detail.weightUnit ?? "",
      grossWt:            detail.grossWeightKg,
      totalCbm:           detail.cbm,
      rton:               detail.rton,
      volWt:              detail.volumeWtKg,
      operatorCode:       detail.operatorCode ?? "",
      salesManCode:       detail.salesManCode ?? "",
      teamCode:           detail.teamCode ?? "",
      salesClass:         detail.salesClass ?? undefined,
      // BE 응답 volumeDivisor → form 필드 dimensionDivisor
      dimensionDivisor:   detail.volumeDivisor ?? "CM6000",
      remark:             detail.remark ?? "",
      containers: detail.containers.map((c, idx) => ({
        id:       c.id ?? idx,
        cno:      c.containerNo ?? "",
        contType: c.containerType ?? "",
        sealNo1:  c.sealNo1 ?? "",
        sealNo2:  c.sealNo2 ?? "",
        sealNo3:  c.sealNo3 ?? "",
        pkg:      c.pkgQty,
        pkgUnit:  c.pkgUnit ?? "",
        grossWt:  c.grossWeightKg,
        cbm:      c.cbm,
      })),
      dimensions: detail.dims.map((d, idx) => ({
        id:     d.id ?? idx,
        length: d.lengthCm != null ? String(d.lengthCm) : "",
        width:  d.widthCm  != null ? String(d.widthCm)  : "",
        height: d.heightCm != null ? String(d.heightCm) : "",
        qty:    d.quantity != null ? String(d.quantity)  : "",
        cbm:    d.cbm      != null ? String(d.cbm)      : "",
        volWt:  d.volumeWeightKg != null ? String(d.volumeWeightKg) : "",
      })),
    });
  }, [detail, methods]);

  const mutation = useMutation({
    mutationFn: (data: NonBlFormValues) => {
      return isEdit
        ? nonBlPort.update(id!, buildNonBlUpdateRequest(data))
        : nonBlPort.create(buildNonBlRequest(data));
    },
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      if (!isEdit) {
        useEntryFocusStore.getState().setFocus("nonBl", saved.id);
        detailLoadedRef.current = false;
      } else {
        queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", id] });
        // refetch 된 detail 로 form.reset 재발동
        detailLoadedRef.current = false;
      }
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => nonBlPort.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      methods.reset(createEmptyNonBlFormValues());
      clearDraft(`non::${id}`);
      useEntryFocusStore.getState().clearFocus("nonBl");
    },
  });

  function handleResetEntry() {
    methods.reset(createEmptyNonBlFormValues());
    clearDraft(`non::${id ?? "new"}`);
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus("nonBl");
  }

  async function handleSearch() {
    const nonBlNo = methods.getValues("nonBlNo")?.trim();
    if (!nonBlNo) {
      toast.info("hbl_no를 입력하세요.");
      return;
    }

    const ids = await nonBlPort.findByHblNo(nonBlNo);

    if (ids.length === 0) {
      toast.info("조회된 건이 없습니다.");
      return;
    }

    if (ids.length > 1) {
      toast.info("여러 건이 검색되었습니다. List에서 선택하세요.");
      router.push("/fms/non-bl/list");
      return;
    }

    const targetId = ids[0];
    if (targetId === id) {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", id] });
      detailLoadedRef.current = false;
    } else {
      useEntryFocusStore.getState().setFocus("nonBl", targetId);
    }
  }

  function handleDelete() {
    if (!isEdit) return;
    if (window.confirm("삭제하시겠습니까?")) {
      deleteMutation.mutate();
    }
  }

  function handleSubmit(data: NonBlFormValues) {
    mutation.mutate(data);
  }

  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : mutation.isPending ? "저장 중..." : "조회 중...";

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
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L Entry
        </div>
        <div className="page-head__meta">
          <span className={`badge ${isEdit ? "badge--saved" : "badge--draft"}`}>
            {isEdit ? "SAVED" : "DRAFT"}
          </span>
        </div>
        <div className="page-head__actions">
          <button type="button" className="btn btn--sm" onClick={handleResetEntry}>
            <FilePlus size={12} />New
          </button>
          <button type="button" className="btn btn--sm btn--search" onClick={handleSearch}>
            <Search size={12} />Search
          </button>
          <button
            type="button"
            className="btn btn--sm btn--transaction"
            disabled={mutation.isPending}
            onClick={methods.handleSubmit(handleSubmit)}
          >
            <Save size={12} />{mutation.isPending ? "Saving..." : "Save"}
          </button>
          <button
            type="button"
            className="btn btn--sm btn--danger"
            onClick={handleDelete}
            disabled={!isEdit || deleteMutation.isPending}
          >
            <Trash2 size={12} />Delete
          </button>
          <button type="button" className="btn btn--sm">
            <Copy size={12} />Copy
          </button>
          <button
            type="button"
            className="btn btn--sm btn--transaction"
            onClick={() => {
              if (!isEdit || !id) {
                toast.info("먼저 Non B/L을 조회해주세요.");
                return;
              }
              setIsChangeBlNoModalOpen(true);
            }}
          >
            <RefreshCw size={12} />Change B/L No
          </button>
        </div>
      </div>

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
        <div className="field">
          <div className="field__label">Bound</div>
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
