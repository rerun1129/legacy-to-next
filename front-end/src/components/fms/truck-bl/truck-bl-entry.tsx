"use client";

import { useState, useEffect, useRef }                from "react";
import { useForm, FormProvider }                       from "react-hook-form";
import { useMutation, useQuery }                        from "@tanstack/react-query";
import { useRouter }                                   from "next/navigation";
import { Save, Trash2, Truck, FilePlus }               from "lucide-react";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainTruck }     from "./tabs/main-truck";
import type { HouseBlFormValues }                      from "@/components/fms/house-bl/house-bl-schema";
import { createEmptyTruckBlFormValues }                from "./truck-bl-defaults";
import { useBlDraftSync }                              from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                             from "@/lib/use-bl-draft-store";
import { truckBlPort }                                 from "@/lib/ports";

// label → RHF 필드 경로 매핑 (toolbar 5개)
const TOOLBAR_REGISTER: Record<string, keyof HouseBlFormValues> = {
  "Truck B/L No": "truckBlNo",
  "Settle":       "truckSettle",
  "Incoterms":    "incoterms",
  "Freight Term": "truckFreightTerm",
  "Status":       "truckStatus",
};

interface Props {
  id?: number;
}

export function TruckBLEntry({ id }: Props = {}) {
  const [tab, setTab] = useState("main");
  const isEdit = Boolean(id);
  // lazy initializer: 마운트 시 1회만 실행 — marker 있으면 제거 후 true 반환
  const [hydrateAllowed] = useState<boolean>(() => {
    if (typeof window === "undefined" || id == null) return false;
    const key = `truck-bl-entry:hot:${id}`;
    if (sessionStorage.getItem(key)) {
      sessionStorage.removeItem(key);
      return true;
    }
    return false;
  });
  const router = useRouter();
  const detailLoadedRef = useRef<boolean>(false);

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const form = useForm<HouseBlFormValues>({
    defaultValues: createEmptyTruckBlFormValues(),
  });

  // F5 새로고침 시 빈 폼 강제: marker 없으면 신규 모드 URL로 교체
  useEffect(() => {
    if (id == null) return;
    if (!hydrateAllowed) {
      router.replace("/fms/truck-bl/entry");
    }
  }, [id, hydrateAllowed, router]);

  useBlDraftSync(form, "truck::" + (id ?? "new"));

  // unmount 시 draft 제거 — 재진입(remount) 시 이전 값 복원 방지
  useEffect(() => {
    const draftKey = "truck::" + (id ?? "new");
    return () => {
      clearDraft(draftKey);
    };
  }, [clearDraft, id]);

  const { data: detail } = useQuery({
    queryKey: ["truck-bl", "detail", id],
    queryFn: () => truckBlPort.getById(id!),
    enabled: isEdit && hydrateAllowed,
  });

  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    form.reset({
      ...createEmptyTruckBlFormValues(),
      truckBlNo:          detail.hblNo ?? "",
      incoterms:          detail.incoterms ?? "",
      truckFreightTerm:   detail.freightTerm ?? "",
      // truckSettle은 toolbar 표시용, settlePartnerCode와 동기화
      truckSettle:        detail.settlePartnerCode ?? "",
      settlePartnerCode:  detail.settlePartnerCode ?? "",
      shipperCode:        detail.shipperCode ?? "",
      consigneeCode:      detail.consigneeCode ?? "",
      notifyCode:         detail.notifyCode ?? "",
      pol:                detail.polCode ?? "",
      pod:                detail.podCode ?? "",
      etd:                detail.etd ?? "",
      eta:                detail.eta ?? "",
      pkgQty:             detail.pkgQty != null ? String(detail.pkgQty) : "",
      pkgUnit:            detail.pkgUnit ?? "",
      grossWeightKg:      detail.grossWeightKg != null ? String(detail.grossWeightKg) : "",
      cbm:                detail.cbm != null ? String(detail.cbm) : "",
      chargeWeightKg:     detail.chargeWeightKg != null ? String(detail.chargeWeightKg) : "",
      actualCustomerCode: detail.actualCustomerCode ?? "",
      operatorCode:       detail.operatorCode ?? "",
      teamCode:           detail.teamCode ?? "",
      salesManCode:       detail.salesManCode ?? "",
      truckerCode:        detail.truckerCode ?? "",
      truckerPic:         detail.truckerPic ?? "",
      pickupDate:         detail.pickupDate ?? "",
    } satisfies Partial<HouseBlFormValues>);
  }, [detail, form]);

  // Save/Delete 구현 전: isPending 상태 관리용 — onClick은 아직 연결하지 않는다
  const mutation = useMutation({
    mutationFn: async () => { /* not yet implemented */ },
  });

  const deleteMutation = useMutation({
    mutationFn: async () => { /* not yet implemented */ },
  });

  function handleResetEntry() {
    form.reset(createEmptyTruckBlFormValues());
    clearDraft("truck::" + (id ?? "new"));
    detailLoadedRef.current = false;
    router.replace("/fms/truck-bl/entry");
  }

  return (
    <FormProvider {...form}>
    <>
      {/* Page header — NO Print button per PRD §S-06 */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Truck size={14} /></div>
          Truck B/L Entry
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
          <button
            type="button"
            className="btn btn--sm btn--danger"
            disabled={!isEdit || deleteMutation.isPending}
          >
            <Trash2 size={12} />Delete
          </button>
          <button
            type="button"
            className="btn btn--sm btn--primary"
            disabled={mutation.isPending}
          >
            <Save size={12} />{mutation.isPending ? "Saving..." : "Save"}
          </button>
        </div>
      </div>

      {/* Toolbar: Document Key fields */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(5, 1fr)" }}>
        {[
          { l: "Truck B/L No",    req: true  },
          { l: "Settle",          req: true  },
          { l: "Incoterms",       req: false },
          { l: "Freight Term",    req: false },
          { l: "Status",          req: false },
        ].map((f) => (
          <div key={f.l} className={`field${f.req ? " is-required" : ""}`}>
            <div className={`field__label${f.req ? " is-required" : ""}`}>{f.l}</div>
            <div className="field__input">
              <input
                {...form.register(TOOLBAR_REGISTER[f.l])}
                placeholder={f.l === "Truck B/L No" ? "Auto on save" : f.l}
              />
            </div>
          </div>
        ))}
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
    </>
    </FormProvider>
  );
}
