"use client";

import { useState, useRef, useEffect } from "react";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore, blDraftStore } from "@/lib/use-bl-draft-store";
import { useForm, FormProvider, Controller } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { useTranslations } from "next-intl";
import { TOOLBAR_FIELD_TO_RHF } from "./master-bl-schema";
import type { MasterBlFormValues } from "./master-bl-schema";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import { ComboBox, TextBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { getMasterVariant } from "@/lib/bl-variants";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useEntryTabStore } from "@/lib/use-entry-tab-store";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { useMasterBlEntryMutations } from "./use-master-bl-entry-mutations";
import { useMasterBlEntryDetailSync } from "./use-master-bl-entry-detail-sync";
import { useMasterBlEntryHandlers } from "./use-master-bl-entry-handlers";
import { MasterBlEntryPageHead } from "./master-bl-entry-page-head";
import { MasterMainTab } from "./tabs/main-tab";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { ScreenGuard }   from "@/components/shared/screen-guard";
import { MasterChangeBlNoModal } from "./master-change-bl-no-modal";

interface Props {
  variantKey: string;
}

const TOOLBAR_SEA = ["masterRef", "mblNo", "lineBkgNo", "loadType", "serviceTerm", "blType", "shipmentType"] as const;
const TOOLBAR_AIR = ["masterRef", "mawbNo", "shipmentType"] as const;

// ComboBox로 렌더링할 enum fieldId 목록
const TOOLBAR_ENUM_FIELDS = new Set(["loadType", "serviceTerm", "blType", "shipmentType"]);

// required fieldId 목록
const REQUIRED = new Set(["masterRef", "mblNo", "mawbNo"]);

function getToolbarFields(mode: string) {
  return mode === "SEA" ? TOOLBAR_SEA : TOOLBAR_AIR;
}

export function MasterBLEntry({ variantKey }: Props) {
  const [resetVersion, setResetVersion] = useState(0);
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
  const queryClient = useQueryClient();

  const tb  = useTranslations("fms.masterBl.entry.toolbar");
  const tts = useTranslations("fms.masterBl.entry.tabs");
  const tm  = useTranslations("fms.masterBl.entry.msg");

  const variant = getMasterVariant(variantKey);
  const id = useEntryFocusStore((s) => s.focus[entryFocusKeys.masterBl(variantKey)]);
  const isEdit = Boolean(id);
  const modeLabels = getModeLabels(variant.mode);
  const toolbarFields = getToolbarFields(variant.mode);
  const focusDomain = entryFocusKeys.masterBl(variantKey);
  // 탭 상태 — 라우트 전환 후 재진입 시 마지막 탭 유지 (EntryDomain별 싱글톤 store)
  const tab = useEntryTabStore((s) => s.tabs[focusDomain] ?? "main");

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    useEntryTabStore.getState().setTab(focusDomain, key);
  }

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
    loadType:     { options: loadTypeOptions,     placeholder: loadTypePh },
    serviceTerm:  { options: serviceTermOptions,  placeholder: serviceTermPh },
    blType:       { options: blTypeOptions,       placeholder: blTypePh },
    shipmentType: { options: shipmentTypeOptions, placeholder: shipmentTypePh },
  };

  // §6.49 ⑨ — didRestoreFromDraftRef 수신하여 form.reset 시 draft 복원 시 덮어쓰기 방지
  const { didRestoreFromDraftRef } = useBlDraftSync(form, `master:${variantKey}:${id ?? "new"}`);

  const { detail, isDetailFetching, detailLoadedRef } = useMasterBlEntryDetailSync({
    id,
    isEdit,
    form,
    didRestoreFromDraftRef,
  });

  // B/L Copy 재초기화 신호 구독.
  // focus 불변(new→new)일 때 useBlDraftSync의 key 변경이 일어나지 않으므로
  // nonce 증가를 별도 트리거로 삼아 최신 :new draft로 강제 reset한다.
  const nonce = useEntryFocusStore((s) => s.resetNonce[focusDomain]);
  const prevNonceRef = useRef<number | undefined>(undefined);

  useEffect(() => {
    // 초기 마운트(prevNonceRef가 아직 세팅되지 않은 시점)는 무시 — useBlDraftSync가 처리
    if (prevNonceRef.current === undefined) {
      prevNonceRef.current = nonce;
      return;
    }
    // nonce가 실제로 증가했을 때만 발동
    if (nonce === prevNonceRef.current) return;
    prevNonceRef.current = nonce;

    const draftKey = `master:${variantKey}:new`;
    const draft = blDraftStore.getState().getDraft(draftKey);
    if (draft !== undefined) {
      // detail 덮어쓰기 방지 + form reset.
      // main tab 리마운트는 key에 nonce가 포함되어 자동 처리됨.
      didRestoreFromDraftRef.current = true;
      detailLoadedRef.current = true;
      form.reset(draft as MasterBlFormValues);
    }
  // variantKey는 컴포넌트 수명 내 불변(탭 분리 구조).
  // form/didRestoreFromDraftRef/detailLoadedRef는 컴포넌트 수명 내 안정 참조(ref).
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nonce]);

  const { mutation, deleteMutation } = useMasterBlEntryMutations({
    id,
    variantKey,
    variant,
    form,
    detailLoadedRef,
    setResetVersion,
  });

  const { handleSearchBl, handleResetEntry, handleChangeBlNo, handleSave, handleDelete } =
    useMasterBlEntryHandlers({
      id,
      variantKey,
      variant,
      form,
      formRef,
      detailLoadedRef,
      didRestoreFromDraftRef,
      isEdit,
      mutation,
      deleteMutation,
      setResetVersion,
      setIsChangeBlNoModalOpen,
      queryClient,
      clearDraft,
    });

  const tabs = [
    { key: "main",    label: tts("main")    },
    { key: "freight", label: tts("freight") },
  ];

  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? tm("deleting") : mutation.isPending ? tm("saving") : tm("fetching");

  // nonce를 key에 포함해 Copy 신호(new→new)도 main tab 리마운트 트리거
  const mainTabKey = `${resetVersion}:${nonce ?? 0}`;

  return (
    <FormProvider {...form}>
    <ScreenGuard visible={isLoading} message={loadingMessage} />
    <form ref={formRef} onSubmit={form.handleSubmit(handleSave)} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
      <MasterBlEntryPageHead
        variant={variant}
        mutation={mutation}
        deleteMutation={deleteMutation}
        isEdit={isEdit}
        onResetEntry={handleResetEntry}
        onSearchBl={handleSearchBl}
        onDelete={handleDelete}
        onChangeBlNo={handleChangeBlNo}
      />

      {/* Toolbar — SEA 7 필드: TextBox(3) + ComboBox+useEnumOptions(4 enum) */}
      <div className="toolbar" style={{ gridTemplateColumns: `repeat(${variant.toolbarColumnCount}, 1fr)` }}>
        {toolbarFields.map((f) => {
          const fieldPath = TOOLBAR_FIELD_TO_RHF[f];
          const isRequired = REQUIRED.has(f);
          const label = tb(f);
          return (
            <div key={f} className={`field${isRequired ? " is-required" : ""}`}>
              <div className={`field__label${isRequired ? " is-required" : ""}`}>{label}</div>
              <div className="field__input">
                {fieldPath ? (
                  TOOLBAR_ENUM_FIELDS.has(f) ? (
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
                    // register(uncontrolled)는 nested path reset 시 input.ref 갱신 미보장 — Controller로 통일
                    <Controller
                      name={fieldPath as keyof MasterBlFormValues}
                      control={form.control}
                      render={({ field }) => (
                        <TextBox
                          placeholder={label}
                          value={(field.value as string | undefined) ?? ""}
                          onChange={field.onChange}
                          onBlur={field.onBlur}
                        />
                      )}
                    />
                  )
                ) : null}
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

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MasterMainTab key={mainTabKey} variant={variant} form={form} active={tab === "main"} /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab key={resetVersion} active={tab === "freight"} mode={variant.mode === "SEA" || variant.mode === "AIR" ? variant.mode : undefined} layoutScope={`master-bl-entry.freight.${variant.key}`} /></div>
    </form>
    {isEdit && id && (
      <MasterChangeBlNoModal
        masterBlId={id}
        currentMblNo={detail?.mblNo ?? ""}
        currentMasterRefNo={detail?.masterRefNo ?? ""}
        blNoLabel={modeLabels.blNo}
        isOpen={isChangeBlNoModalOpen}
        onClose={() => setIsChangeBlNoModalOpen(false)}
        onChanged={() => {
          // §6.67 draft 가드 3단계: ref 해제 → draft 복원 차단 → draft 삭제 순서 보존
          detailLoadedRef.current = false;
          didRestoreFromDraftRef.current = false;
          clearDraft(`master:${variantKey}:${id}`);
        }}
      />
    )}
    </FormProvider>
  );
}
