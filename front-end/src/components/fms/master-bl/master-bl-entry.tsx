"use client";

import { useRef, useState, useEffect } from "react";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { useForm, FormProvider, Controller } from "react-hook-form";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { confirm } from "@/components/confirm";
import { TOOLBAR_TO_FIELD } from "./master-bl-schema";
import type { MasterBlFormValues } from "./master-bl-schema";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import { mapMasterBlDetailToForm } from "./map-master-bl-detail";
import { Save, Trash2, Layers, RefreshCw, Search, FilePlus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ComboBox, TextBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { getMasterVariant, getPageTitle } from "@/lib/bl-variants";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { masterBlPort } from "@/lib/ports";
import type { ConsolidatedHouseBlSummary } from "@/domain/master-bl";
import { useMasterBlEntryMutations } from "./use-master-bl-entry-mutations";
import { MasterMainTab } from "./tabs/main-tab";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { ScreenGuard }   from "@/components/shared/screen-guard";

interface Props {
  variantKey: string;
}

const TOOLBAR_SEA = ["Master Ref", "MBL No", "Line Bkg. No", "Load Type", "Service Term", "B/L Type", "Shipment Type"] as const;
const TOOLBAR_AIR = ["Master Ref", "MAWB No", "Shipment Type", "", "", "", ""] as const;

// ComboBox로 렌더링할 enum 라벨 목록
const TOOLBAR_ENUM_LABELS = new Set(["Load Type", "Service Term", "B/L Type", "Shipment Type"]);

function getToolbarFields(mode: string) {
  return mode === "SEA" ? TOOLBAR_SEA : TOOLBAR_AIR.filter(Boolean);
}

export function MasterBLEntry({ variantKey }: Props) {
  const [tab, setTab] = useState("main");
  const [resetVersion, setResetVersion] = useState(0);
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
  const queryClient = useQueryClient();

  const variant = getMasterVariant(variantKey);
  const id = useEntryFocusStore((s) => s.focus[entryFocusKeys.masterBl(variantKey)]);
  const isEdit = Boolean(id);
  const modeLabels = getModeLabels(variant.mode);
  const toolbarFields = getToolbarFields(variant.mode);

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    setTab(key);
  }

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["master-bl", "detail", id],
    queryFn: () => masterBlPort.getById(id!),
    enabled: isEdit,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const form = useForm<MasterBlFormValues>({
    defaultValues: {
      ...createEmptyMasterBlFormValues(),
      jobDiv: variant.mode,
      bound: variant.direction ?? "EXP",
    },
  });

  // §6.49 ⑰ — enum 옵션은 useEnumOptions로 동적 fetch (BE SSOT)
  const { options: loadTypeOptions,     placeholder: loadTypePh }     = useEnumOptions("LoadType");
  const { options: serviceTermOptions,  placeholder: serviceTermPh }  = useEnumOptions("ServiceTerm");
  const { options: blTypeOptions,       placeholder: blTypePh }       = useEnumOptions("BlType");
  const { options: shipmentTypeOptions, placeholder: shipmentTypePh } = useEnumOptions("ShipmentType");

  const TOOLBAR_ENUM: Record<string, { options: typeof loadTypeOptions; placeholder: string | undefined }> = {
    "Load Type":     { options: loadTypeOptions,     placeholder: loadTypePh },
    "Service Term":  { options: serviceTermOptions,  placeholder: serviceTermPh },
    "B/L Type":      { options: blTypeOptions,       placeholder: blTypePh },
    "Shipment Type": { options: shipmentTypeOptions, placeholder: shipmentTypePh },
  };

  // §6.49 ⑨ — didRestoreFromDraftRef 수신하여 form.reset 시 draft 복원 시 덮어쓰기 방지
  const { didRestoreFromDraftRef } = useBlDraftSync(form, `master:${variantKey}:${id ?? "new"}`);

  const detailLoadedRef = useRef<boolean>(false);

  // id 변경 시 form.reset 재트리거를 위해 ref 초기화
  useEffect(() => {
    detailLoadedRef.current = false;
  }, [id]);

  // §6.49 ⑨ — draft 복원 시 detail로 덮어쓰지 않음 (House 패턴 정합)
  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (didRestoreFromDraftRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    form.reset(mapMasterBlDetailToForm(detail));
  }, [detail, form, didRestoreFromDraftRef]);

  const { mutation, deleteMutation } = useMasterBlEntryMutations({
    id,
    variantKey,
    variant,
    form,
    detailLoadedRef,
    setResetVersion,
  });

  function handleSearchBl() {
    const mblValue = form.getValues('mblNo')?.trim();
    if (!mblValue) return;

    masterBlPort
      .findByMblNo(mblValue)
      .then((ids) => {
        if (ids.length === 0) {
          alert('해당 B/L을 찾을 수 없습니다.');
          return;
        }
        if (ids.length > 1) {
          alert('동일 MBL No. 다건 발견 — List 화면에서 선택해주세요.');
          return;
        }
        const targetId = ids[0];
        if (targetId === id) {
          // 동일 id 재조회: detail cache invalidate 후 useEffect가 form.reset을 다시 실행
          queryClient.invalidateQueries({ queryKey: ['master-bl', 'detail', id] });
          clearDraft(`master:${variantKey}:${id}`);
          detailLoadedRef.current = false;
        } else {
          // 다른 id: focus 변경 → useQuery 자동 트리거 → useEffect에서 form.reset
          queryClient.invalidateQueries({ queryKey: ['master-bl', 'detail', targetId] });
          clearDraft(`master:${variantKey}:${targetId}`);
          sessionStorage.setItem(`master-bl-entry:hot:${targetId}`, "1");
          useEntryFocusStore.getState().setFocus(entryFocusKeys.masterBl(variantKey), targetId);
        }
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : String(err);
        alert(`B/L 조회 중 오류가 발생했습니다: ${message}`);
      });
  }

  function handleResetEntry() {
    form.reset({
      ...createEmptyMasterBlFormValues(),
      jobDiv: variant.mode,
      bound: variant.direction ?? "EXP",
    });
    clearDraft(`master:${variantKey}:${id ?? "new"}`);
    clearDraft(`master:${variantKey}:new`);
    detailLoadedRef.current = false;
    useEntryFocusStore.getState().clearFocus(entryFocusKeys.masterBl(variantKey));
    formRef.current?.reset();
    setResetVersion(v => v + 1);
  }

  // Save confirm 모달 (House 패턴 정합 — Non B/L 16dbc0b 패턴)
  async function handleSave(raw: MasterBlFormValues) {
    const ok = await confirm({
      title: "저장하시겠습니까?",
      variant: "default",
    });
    if (!ok) return;
    mutation.mutate(raw);
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

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "freight", label: "Freight" },
  ];

  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : mutation.isPending ? "저장 중..." : "조회 중...";

  return (
    <FormProvider {...form}>
    <ScreenGuard visible={isLoading} message={loadingMessage} />
    <form ref={formRef} onSubmit={form.handleSubmit(handleSave)} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
      {/* Page header — NOTE: No Print button per PRD §S-04 */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Layers size={14} /></div>
          {getPageTitle(variant, "Master", "Entry")}
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <Button size="sm" variant="normal" leftIcon={<FilePlus size={12} />} onClick={handleResetEntry}>New</Button>
          <Button size="sm" variant="search" leftIcon={<Search size={12} />} onClick={handleSearchBl}>Search</Button>
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
          <Button size="sm" variant="transaction" leftIcon={<RefreshCw size={12} />}>{modeLabels.changeBLNo}</Button>
        </div>
      </div>

      {/* Toolbar — SEA 7 필드: TextBox(3) + ComboBox+useEnumOptions(4 enum) */}
      <div className="toolbar" style={{ gridTemplateColumns: `repeat(${variant.toolbarColumnCount}, 1fr)` }}>
        {toolbarFields.map((f) => {
          const fieldPath = TOOLBAR_TO_FIELD[f];
          const isRequired = ["MBL No", "MAWB No", "Master Ref"].includes(f);
          return (
            <div key={f} className={`field${isRequired ? " is-required" : ""}`}>
              <div className={`field__label${isRequired ? " is-required" : ""}`}>{f}</div>
              <div className="field__input">
                {fieldPath ? (
                  TOOLBAR_ENUM_LABELS.has(f) ? (
                    <Controller
                      name={fieldPath as keyof MasterBlFormValues}
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
                    <TextBox
                      placeholder={f}
                      {...(form.register as (n: string) => object)(fieldPath)}
                    />
                  )
                ) : (
                  <input placeholder={f || ""} />
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Tabbar */}
      <div className="tabbar">
        {tabs.map((t) => (
          <button key={t.key} type="button" className={`tabbar__tab${tab === t.key ? " is-active" : ""}`} onClick={() => handleTabChange(t.key)}>
            {t.label}
          </button>
        ))}
      </div>

      {/* consolidatedHouseBls: detail fetch 시 읽기 전용 표시 전용, submit body에 미포함 */}
      {isEdit && detail?.consolidatedHouseBls && detail.consolidatedHouseBls.length > 0 && (
        <div className="consolidated-hbl-list" aria-label="Consolidated House B/L list (read-only)">
          {detail.consolidatedHouseBls.map((hbl: ConsolidatedHouseBlSummary) => (
            <span key={hbl.houseBlId} className="badge">{hbl.hblNo ?? `HBL#${hbl.houseBlId}`}</span>
          ))}
        </div>
      )}

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MasterMainTab key={resetVersion} variant={variant} form={form} active={tab === "main"} /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab key={resetVersion} active={tab === "freight"} /></div>
    </form>
    </FormProvider>
  );
}
