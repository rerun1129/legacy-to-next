"use client";

import { useState, useEffect, useRef }                from "react";
import { useForm, FormProvider }                       from "react-hook-form";
import { useMutation, useQuery, useQueryClient }       from "@tanstack/react-query";
import { useRouter }                                   from "next/navigation";
import { Save, Trash2, Package, FilePlus, Search, Copy, RefreshCw } from "lucide-react";
import { FreightTab }     from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }      from "./tabs/main-non-bl";
import type { NonBlFormValues }                        from "./non-bl-schema";
import { createEmptyNonBlFormValues }                  from "./non-bl-defaults";
import { buildNonBlRequest }                           from "./non-bl-submit";
import { useBlDraftSync }                              from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                             from "@/lib/use-bl-draft-store";
import { TextBox, DropBox }                            from "@/components/shared/inputs";
import { useEnumOptions }                              from "@/application/enums/use-enum";
import { nonBlPort }                                   from "@/lib/ports";

interface Props {
  id?: number;
}

export function NonBLEntry({ id }: Props = {}) {
  const [tab, setTab] = useState("main");
  const isEdit = Boolean(id);
  const queryClient = useQueryClient();
  const router = useRouter();
  const detailLoadedRef = useRef<boolean>(false);

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const methods = useForm<NonBlFormValues>({
    defaultValues: createEmptyNonBlFormValues(),
  });

  useBlDraftSync(methods, `non::${id ?? "new"}`);

  const { register } = methods;

  // status: 백엔드 관리 필드 — UI 노출 없이 form에만 등록
  register("status");

  const { options: workDivOptions, placeholder: workDivPlaceholder } = useEnumOptions("WorkDivision");
  const { options: boundOptions, placeholder: boundPlaceholder } = useEnumOptions("Bound");

  const { data: detail } = useQuery({
    queryKey: ["non-bl", "detail", id],
    queryFn: () => nonBlPort.getById(id!),
    enabled: isEdit,
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
      cargoUnit:          detail.pkgUnit ?? "",
      grossWt:            detail.grossWeightKg,
      totalCbm:           detail.cbm,
      rton:               detail.rton,
      volWt:              detail.volumeWtKg,
      operatorCode:       detail.operatorCode ?? "",
      salesManCode:       detail.salesManCode ?? "",
      teamCode:           detail.teamCode ?? "",
      containers: detail.containers.map((c, idx) => ({
        id:       idx,
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
        id:     idx,
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
      const req = buildNonBlRequest(data);
      return isEdit ? nonBlPort.update(id!, req) : nonBlPort.create(req);
    },
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      if (!isEdit) {
        router.replace(`/fms/non-bl/entry/${saved.id}`);
      } else {
        queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", id] });
      }
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => nonBlPort.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "list"] });
      methods.reset(createEmptyNonBlFormValues());
      clearDraft(`non::${id}`);
      router.replace("/fms/non-bl/entry");
    },
  });

  function handleResetEntry() {
    methods.reset(createEmptyNonBlFormValues());
    clearDraft(`non::${id ?? "new"}`);
    detailLoadedRef.current = false;
  }

  function handleSearch() {
    if (isEdit) {
      queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", id] });
      detailLoadedRef.current = false;
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

  return (
    <FormProvider {...methods}>
    <form
      onSubmit={methods.handleSubmit(handleSubmit)}
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
          <button type="button" className="btn btn--sm btn--search" onClick={handleSearch} disabled={!isEdit}>
            <Search size={12} />Search
          </button>
          <button
            type="submit"
            className="btn btn--sm btn--transaction"
            disabled={mutation.isPending}
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
          <button type="button" className="btn btn--sm btn--transaction">
            <RefreshCw size={12} />Change BL No
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
            <DropBox variant="panel" options={workDivOptions} placeholder={workDivPlaceholder} {...register("workDiv")} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Bound</div>
          <div className="field__input">
            <DropBox variant="panel" options={boundOptions} placeholder={boundPlaceholder} {...register("bound")} />
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
    </FormProvider>
  );
}
