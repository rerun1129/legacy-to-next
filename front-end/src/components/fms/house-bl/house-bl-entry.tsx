"use client";

import { useRef, useState, useEffect } from "react";
import { useSearchParams } from "next/navigation";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { useForm, FormProvider, Controller } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Save, Printer, Trash2, FileText, RefreshCw, Search, FilePlus } from "lucide-react";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { confirm } from "@/components/confirm";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { getPageTitle } from "@/lib/bl-variants";
import { MainTabSea }  from "./tabs/main-sea";
import { MainTabAir }  from "./tabs/main-air";
import { FreightTab }  from "./tabs/freight-tab";
import { houseBlPort } from "@/lib/ports";
import type { HouseBlFormValues } from "./house-bl-schema";
import { createEmptyHouseBlFormValues } from "./house-bl-defaults";
import { buildHouseBlRequest, buildHouseBlUpdateRequest } from "./house-bl-submit";
import { mapHouseBlDetailToForm } from "./map-house-bl-detail";
import { SwitchBlModal } from "@/components/fms/switch-bl/switch-bl-modal";
import { HouseChangeBlNoModal } from "./house-change-bl-no-modal";
import { useSearchBl } from "./use-search-bl";
import { toast } from "@/lib/toast-store";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { ScreenGuard } from "@/components/shared/screen-guard";
import { Button } from "@/components/shared/button";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";

const TOOLBAR_FIELDS_SEA = [
  "Shipment Type", "HBL No", "MBL No", "Load Type", "Service Term", "B/L Type", "Master Ref",
] as const;
const TOOLBAR_FIELDS_AIR = [
  "Shipment Type", "HAWB No", "MAWB No", "Master Ref",
] as const;
const TOOLBAR_FIELDS_TRUCK = [
  "Truck B/L No",
] as const;
const TOOLBAR_FIELDS_NON_BL = [
  "Non B/L No",
] as const;

const DEFAULTS_SEA: Record<string, string> = {
  "Shipment Type": "", "HBL No": "",
  "MBL No": "", "Load Type": "", "Service Term": "",
  "B/L Type": "", "Master Ref": "",
};
const DEFAULTS_AIR: Record<string, string> = {
  "Shipment Type": "", "HAWB No": "",
  "MAWB No": "", "Master Ref": "",
};
const DEFAULTS_TRUCK: Record<string, string> = {
  "Truck B/L No": "",
};
const DEFAULTS_NON_BL: Record<string, string> = {
  "Non B/L No": "",
};

function getToolbarFields(variant: BLVariantConfig) {
  if (variant.mode === "SEA")    return TOOLBAR_FIELDS_SEA;
  if (variant.mode === "AIR")    return TOOLBAR_FIELDS_AIR;
  if (variant.mode === "TRUCK")  return TOOLBAR_FIELDS_TRUCK;
  return TOOLBAR_FIELDS_NON_BL;
}

function getToolbarDefaults(variant: BLVariantConfig) {
  if (variant.mode === "SEA")    return DEFAULTS_SEA;
  if (variant.mode === "AIR")    return DEFAULTS_AIR;
  if (variant.mode === "TRUCK")  return DEFAULTS_TRUCK;
  return DEFAULTS_NON_BL;
}

function renderMainTab(variant: BLVariantConfig, active: boolean) {
  if (variant.mode === "SEA")  return <MainTabSea variant={variant} active={active} />;
  if (variant.mode === "AIR")  return <MainTabAir variant={variant} active={active} />;
  return <MainTabSea variant={variant} active={active} />;
}

const TOOLBAR_LABEL_TO_FIELD: Record<string, string> = {
  "HBL No":         "hbl",
  "HAWB No":        "hbl",
  "Truck B/L No":   "hbl",
  "Non B/L No":     "hbl",
  "MBL No":         "mbl",
  "MAWB No":        "mbl",
  "Load Type":      "lType",
  "Shipment Type":  "sType",
  "Service Term":   "seaDetail.serviceTerm",
  "B/L Type":       "seaDetail.blType",
  "Master Ref":     "masterRefNo",
};

const REQUIRED_TOOLBAR_LABELS = new Set(["HBL No", "HAWB No", "Truck B/L No", "Non B/L No", "Shipment Type"]);

interface Props {
  variant: BLVariantConfig;
}

export function HouseBLEntry({ variant }: Props) {
  const [tab, setTab] = useState("main");
  const [isSwitchBlModalOpen, setIsSwitchBlModalOpen] = useState(false);
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
  const searchParams = useSearchParams();
  const id = useEntryFocusStore((s) => s.focus[entryFocusKeys.houseBl(variant.key)]);
  const isEdit = Boolean(id);
  const queryClient = useQueryClient();

  const defaults = getToolbarDefaults(variant);

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const form = useForm<HouseBlFormValues>({
    defaultValues: createEmptyHouseBlFormValues(),
  });

  const { options: loadTypeOptions,     placeholder: loadTypePh }     = useEnumOptions("LoadType");
  const { options: serviceTermOptions,  placeholder: serviceTermPh }  = useEnumOptions("ServiceTerm");
  const { options: blTypeOptions,       placeholder: blTypePh }       = useEnumOptions("BlType");
  const { options: shipmentTypeOptions, placeholder: shipmentTypePh } = useEnumOptions("ShipmentType");

  // LoadType / ServiceTerm / BlType / ShipmentType 은 모두 e.name() 기반 등록이므로 §6.45 재매핑 불필요
  const TOOLBAR_ENUM: Record<string, { options: typeof loadTypeOptions; placeholder: string | undefined }> = {
    "Load Type":     { options: loadTypeOptions,     placeholder: loadTypePh },
    "Service Term":  { options: serviceTermOptions,  placeholder: serviceTermPh },
    "B/L Type":      { options: blTypeOptions,       placeholder: blTypePh },
    "Shipment Type": { options: shipmentTypeOptions, placeholder: shipmentTypePh },
  };

  const { didRestoreFromDraftRef } = useBlDraftSync(form, `house:${variant.key}:${id ?? "new"}`);

  const detailLoadedRef = useRef<boolean>(false);

  // URL ?id → store 동기: 북마크/리프레시/외부 링크로 진입 시 URL id를 store에 반영 (store가 비어있어 isEdit=false가 되는 문제 방지)
  useEffect(() => {
    const urlId = searchParams.get("id");
    if (urlId == null) return;
    const parsed = Number(urlId);
    if (!Number.isFinite(parsed) || parsed <= 0) return;
    const currentId = useEntryFocusStore.getState().focus[entryFocusKeys.houseBl(variant.key)];
    if (currentId === parsed) return; // 이미 일치하면 no-op
    useEntryFocusStore.getState().setFocus(entryFocusKeys.houseBl(variant.key), parsed);
    detailLoadedRef.current = false; // 새 id로 form.reset 재트리거
  }, [searchParams, variant.key]);

  // id 변경 시 form.reset 재트리거를 위해 ref 초기화
  useEffect(() => {
    detailLoadedRef.current = false;
  }, [id]);

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["house-bl", "detail", id],
    queryFn: () => houseBlPort.getById(id!),
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
    if (didRestoreFromDraftRef.current) return; // draft 복원 시 detail로 덮어쓰지 않음 (Truck 패턴 정합)
    // §6.48 ⑧ — BE 응답 필드 전체를 form에 반영 (3축 동기: BE response ↔ FE domain type ↔ form.reset)
    form.reset(mapHouseBlDetailToForm(detail));
  }, [detail, form, didRestoreFromDraftRef]);

  const mutation = useMutation({
    mutationFn: (data: HouseBlFormValues) => {
      return isEdit
        ? houseBlPort.update(id!, buildHouseBlUpdateRequest(data, variant))
        : houseBlPort.create(buildHouseBlRequest(data, variant));
    },
    onSuccess: (saved) => {
      if (!isEdit) {
        // create 시: saved는 HouseBlDetail (non-null). §6.29 — SEA update는 null 반환이므로
        // isEdit=false 분기에서만 saved.id에 접근해 타입 안전성 보장.
        const newId = saved?.id;
        if (newId != null) {
          // List 자동 invalidate 금지 (§6.21) — detail만 갱신
          queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", newId] });
          // hot-marker: List 화면 진입 시 하이라이트에 사용 (§6.16, Truck 정합)
          sessionStorage.setItem(`house-bl-entry:hot:${newId}`, "1");
          useEntryFocusStore.getState().setFocus(entryFocusKeys.houseBl(variant.key), newId);
          clearDraft(`house:${variant.key}:new`);
          detailLoadedRef.current = false;
        }
      } else {
        // List 자동 invalidate 금지 (§6.21) — detail만 갱신
        queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", id] });
        clearDraft(`house:${variant.key}:${id}`);
        // refetch된 detail로 form.reset 재발동 (Truck 패턴 정합)
        detailLoadedRef.current = false;
      }
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => houseBlPort.delete(id!),
    onSuccess: () => {
      // List 자동 invalidate 금지 (§6.21) — 사용자가 List에서 Search로 직접 재조회
      form.reset(createEmptyHouseBlFormValues());
      useEntryFocusStore.getState().clearFocus(entryFocusKeys.houseBl(variant.key));
    },
  });

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    setTab(key);
  }

  const toolbarFields = getToolbarFields(variant);

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "freight", label: "Freight" },
  ];

  const { handleSearchBl } = useSearchBl(form, variant, {
    id,
    onAfterFound: (_targetId, _sameAsCurrent) => {
      // useSearchBl은 외부 ref를 직접 mutate할 수 없으므로 콜백으로 위임
      detailLoadedRef.current = false;
    },
  });

  function handleResetEntry() {
    form.reset(createEmptyHouseBlFormValues());
    clearDraft(`house:${variant.key}:${id ?? "new"}`);
    formRef.current?.reset();
  }

  function handleSubmit(raw: HouseBlFormValues) {
    mutation.mutate(raw);
  }

  async function handleDelete() {
    if (!isEdit) return;
    const ok = await confirm({
      variant: 'destructive',
      title: '삭제하시겠습니까?',
      confirmText: '삭제',
      description: '삭제된 데이터는 복구할 수 없습니다.',
    });
    if (ok) deleteMutation.mutate();
  }

  const isExp = variant.direction === "EXP";
  const canSwitchBl = isEdit && id != null && variant.key.startsWith('sea-');

  function handleChangeBlNo() {
    if (!isEdit || !id) {
      toast.info("먼저 House B/L을 조회해주세요.");
      return;
    }
    setIsChangeBlNoModalOpen(true);
  }

  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : mutation.isPending ? "저장 중..." : "조회 중...";

  return (
    <>
      <ScreenGuard visible={isLoading} message={loadingMessage} />
      <FormProvider {...form}>
      <form ref={formRef} onSubmit={form.handleSubmit(handleSubmit)} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><FileText size={14} /></div>
            {getPageTitle(variant, 'House', 'Entry')}
          </div>
          <div className="page-head__meta">
            <span className="badge badge--draft">DRAFT</span>
          </div>
          <div className="page-head__actions">
            <Button size="sm" variant="normal" leftIcon={<FilePlus size={12} />} onClick={handleResetEntry}>New</Button>
            <Button size="sm" variant="search" leftIcon={<Search size={12} />} onClick={handleSearchBl}>Search B/L</Button>
            <Button
              type="submit"
              size="sm"
              variant="transaction"
              leftIcon={<Save size={12} />}
              loading={mutation.isPending}
            >{mutation.isPending ? "Saving..." : "Save"}</Button>
            <Button
              size="sm"
              variant="danger"
              leftIcon={<Trash2 size={12} />}
              onClick={handleDelete}
              disabled={!isEdit || deleteMutation.isPending}
            >Delete</Button>
            {isExp && variant.printDocs.length > 0 && (
              <Button size="sm" variant="normal" leftIcon={<Printer size={12} />}>Print</Button>
            )}
            {isExp && (
              <Button
                size="sm"
                variant="transaction"
                leftIcon={<RefreshCw size={12} />}
                disabled={!canSwitchBl}
                onClick={() => setIsSwitchBlModalOpen(true)}
              >Switch B/L</Button>
            )}
            <Button
              size="sm"
              variant="transaction"
              leftIcon={<RefreshCw size={12} />}
              onClick={handleChangeBlNo}
              disabled={!isEdit}
            >Change B/L No.</Button>
          </div>
        </div>

        <div className="toolbar">
          {toolbarFields.map((f) => {
            const fieldName = TOOLBAR_LABEL_TO_FIELD[f];
            const isRequired = REQUIRED_TOOLBAR_LABELS.has(f);
            return (
              <div key={f} className={`field${isRequired ? " is-required" : ""}`}>
                <div className={`field__label${isRequired ? " is-required" : ""}`}>{f}</div>
                <div className="field__input">
                  {fieldName ? (
                    <>
                      {TOOLBAR_ENUM[f] ? (
                        <Controller
                          name={fieldName as keyof HouseBlFormValues}
                          control={form.control}
                          render={({ field }) => (
                            <ComboBox
                              options={TOOLBAR_ENUM[f].options}
                              placeholder={TOOLBAR_ENUM[f].placeholder}
                              value={(field.value as string) ?? ""}
                              onChange={field.onChange}
                            />
                          )}
                        />
                      ) : (
                        <input
                          {...(form.register as (n: string) => object)(fieldName)}
                          placeholder={f}
                        />
                      )}
                      {(form.formState.errors as Record<string, unknown>)[fieldName] && (
                        <span className="field__error">
                          {(form.formState.errors as Record<string, { message?: string }>)[fieldName]?.message}
                        </span>
                      )}
                    </>
                  ) : (
                    <input defaultValue={defaults[f] ?? ""} placeholder={f} />
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <div className="tabbar">
          {tabs.map((t) => (
            <button
              type="button"
              key={t.key}
              className={`tabbar__tab${tab === t.key ? " is-active" : ""}`}
              onClick={() => handleTabChange(t.key)}
            >
              {t.label}
            </button>
          ))}
          <div className="tabbar__spacer" />
          <div className="tabbar__meta">
            <span>Last saved: 10 min ago</span>
          </div>
        </div>

        <div style={{ display: tab === "main"    ? "contents" : "none" }}>{renderMainTab(variant, tab === "main")}</div>
        <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab active={tab === "freight"} /></div>
      </form>
      </FormProvider>

      {canSwitchBl && (
        <SwitchBlModal
          houseBlId={id!}
          isOpen={isSwitchBlModalOpen}
          onClose={() => setIsSwitchBlModalOpen(false)}
        />
      )}
      {isEdit && id && (
        <HouseChangeBlNoModal
          houseBlId={id}
          currentHblNo={detail?.hblNo}
          isOpen={isChangeBlNoModalOpen}
          onClose={() => setIsChangeBlNoModalOpen(false)}
          onChanged={() => {
            detailLoadedRef.current = false;
            queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", id] });
          }}
        />
      )}
    </>
  );
}
