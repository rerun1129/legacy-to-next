"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore, blDraftStore } from "@/lib/use-bl-draft-store";
import { useForm, FormProvider } from "react-hook-form";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { MainTabSea } from "./tabs/main-sea";
import { MainTabAir } from "./tabs/main-air";
import { FreightTab } from "./tabs/freight-tab";
import type { HouseBlFormValues } from "./house-bl-schema";
import {
  TOOLBAR_FIELDS_SEA,
  TOOLBAR_FIELDS_AIR,
  TOOLBAR_FIELDS_TRUCK,
  TOOLBAR_FIELDS_NON_BL,
} from "./house-bl-schema";
import { createEmptyHouseBlFormValues } from "./house-bl-defaults";
import { SwitchBlModal } from "@/components/fms/switch-bl/switch-bl-modal";
import { HouseChangeBlNoModal } from "./house-change-bl-no-modal";
import { BlAttachmentModal } from "@/components/fms/shared/bl-attachment-modal";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useEntryTabStore } from "@/lib/use-entry-tab-store";
import { ScreenGuard } from "@/components/shared/screen-guard";
import { useHouseBlEntryDetailSync } from "./use-house-bl-entry-detail-sync";
import { useHouseBlEntryMutations } from "./use-house-bl-entry-mutations";
import { useHouseBlEntryHandlers } from "./use-house-bl-entry-handlers";
import { HouseBlEntryPageHead } from "./house-bl-entry-page-head";
import { useQueryClient } from "@tanstack/react-query";

function getToolbarFields(variant: BLVariantConfig): ReadonlyArray<string> {
  if (variant.mode === "SEA")   return TOOLBAR_FIELDS_SEA;
  if (variant.mode === "AIR")   return TOOLBAR_FIELDS_AIR;
  if (variant.mode === "TRUCK") return TOOLBAR_FIELDS_TRUCK;
  return TOOLBAR_FIELDS_NON_BL;
}

function renderMainTab(variant: BLVariantConfig, active: boolean, resetVersion: number, nonce: number | undefined) {
  // nonce를 key에 포함해 Copy 신호(new→new)도 리마운트 트리거
  const tabKey = `${resetVersion}:${nonce ?? 0}`;
  if (variant.mode === "SEA") return <MainTabSea key={tabKey} variant={variant} active={active} />;
  if (variant.mode === "AIR") return <MainTabAir key={tabKey} variant={variant} active={active} />;
  return <MainTabSea key={tabKey} variant={variant} active={active} />;
}

interface Props {
  variant: BLVariantConfig;
}

export function HouseBLEntry({ variant }: Props) {
  const tt = useTranslations("fms.houseBl.entry.tabs");
  const [isSwitchBlModalOpen, setIsSwitchBlModalOpen] = useState(false);
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const [isAttachmentsOpen, setIsAttachmentsOpen] = useState(false);
  const [resetVersion, setResetVersion] = useState(0);
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
  const queryClient = useQueryClient();
  const id = useEntryFocusStore((s) => s.focus[entryFocusKeys.houseBl(variant.key)]);
  const isEdit = Boolean(id);
  const clearDraft = useBLDraftStore((state) => state.clearDraft);

  const form = useForm<HouseBlFormValues>({
    defaultValues: createEmptyHouseBlFormValues(),
  });

  const { didRestoreFromDraftRef } = useBlDraftSync(form, `house:${variant.key}:${id ?? "new"}`);

  const { detail, isDetailFetching, detailLoadedRef } = useHouseBlEntryDetailSync({
    id,
    isEdit,
    form,
    didRestoreFromDraftRef,
  });

  // B/L Copy 재초기화 신호 구독.
  // focus 불변(new→new)일 때 useBlDraftSync의 key 변경이 일어나지 않으므로
  // nonce 증가를 별도 트리거로 삼아 최신 :new draft로 강제 reset한다.
  const focusDomain = entryFocusKeys.houseBl(variant.key);
  const nonce = useEntryFocusStore((s) => s.resetNonce[focusDomain]);
  // 탭 상태 — 라우트 전환 후 재진입 시 마지막 탭 유지 (EntryDomain별 싱글톤 store)
  const tab = useEntryTabStore((s) => s.tabs[focusDomain] ?? "main");
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

    const draftKey = `house:${variant.key}:new`;
    const draft = blDraftStore.getState().getDraft(draftKey);
    if (draft !== undefined) {
      // detail 덮어쓰기 방지 + form reset.
      // main tab 리마운트는 renderMainTab의 key에 nonce가 포함되어 자동 처리됨.
      didRestoreFromDraftRef.current = true;
      detailLoadedRef.current = true;
      form.reset(draft as HouseBlFormValues);
    }
  // variant.key는 컴포넌트 수명 내 불변(탭 분리 구조).
  // form/didRestoreFromDraftRef/detailLoadedRef는 컴포넌트 수명 내 안정 참조(ref).
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nonce]);

  // 발행/서류삭제 성공 시 entry detail을 강제 재조회하는 콜백.
  // detailLoadedRef.current=false → invalidate 순서로 실행하면 refetch 후 reset useEffect 가드가 열림.
  const handleFreightMutated = useCallback(() => {
    detailLoadedRef.current = false;
    if (id != null) {
      queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", id] });
    }
  }, [queryClient, id, detailLoadedRef]);

  const { mutation, deleteMutation } = useHouseBlEntryMutations({
    id,
    variant,
    form,
    detailLoadedRef,
    setResetVersion,
  });

  const { handleSearchBl, handleResetEntry, handleChangeBlNo, handleSubmit, handleDelete } =
    useHouseBlEntryHandlers({
      id,
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
      clearDraft,
    });

  const toolbarFields = getToolbarFields(variant);
  const canSwitchBl = isEdit && id != null && (variant.key === "sea-exp" || variant.key === "air-exp");

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    useEntryTabStore.getState().setTab(focusDomain, key);
  }

  const tabs = [
    { key: "main",    label: tt("main")    },
    { key: "freight", label: tt("freight") },
  ];

  const tm = useTranslations("fms.houseBl.entry.msg");
  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? tm("loadingDelete") : mutation.isPending ? tm("loadingSave") : tm("loadingFetch");

  return (
    <>
      <ScreenGuard visible={isLoading} message={loadingMessage} />
      <FormProvider {...form}>
      <form ref={formRef} noValidate onSubmit={form.handleSubmit(handleSubmit)} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }} onKeyDown={(e) => {
          // textarea 줄바꿈은 보존, 그 외 Enter는 implicit form submission 차단
          if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
            e.preventDefault();
          }
        }}>
        <HouseBlEntryPageHead
          variant={variant}
          form={form}
          toolbarFields={toolbarFields}
          mutation={mutation}
          deleteMutation={deleteMutation}
          isEdit={isEdit}
          canSwitchBl={canSwitchBl}
          onResetEntry={handleResetEntry}
          onSearchBl={handleSearchBl}
          onDelete={handleDelete}
          onChangeBlNo={handleChangeBlNo}
          onOpenSwitchBl={() => setIsSwitchBlModalOpen(true)}
          onOpenAttachments={() => setIsAttachmentsOpen(true)}
        />

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
        </div>

        <div style={{ display: tab === "main"    ? "contents" : "none" }}>{renderMainTab(variant, tab === "main", resetVersion, nonce)}</div>
        <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab key={resetVersion} active={tab === "freight"} mode={variant.mode} layoutScope={`house-bl-entry.freight.${variant.key}`} blType="HOUSE" blId={id ?? null} onFreightMutated={handleFreightMutated} /></div>
      </form>
      </FormProvider>

      {canSwitchBl && (
        <SwitchBlModal
          houseBlId={id!}
          houseBlNo={form.getValues("hbl") ?? ""}
          isExp={variant.direction === "EXP"}
          isOpen={isSwitchBlModalOpen}
          onClose={() => setIsSwitchBlModalOpen(false)}
          initialFromHouseBl={{
            shipperCode:      form.getValues("shipperCode") ?? "",
            shipperAddress:   form.getValues("shipperAddress") ?? "",
            consigneeCode:    form.getValues("consigneeCode") ?? "",
            consigneeAddress: form.getValues("consigneeAddress") ?? "",
            notifyCode:       form.getValues("notifyCode") ?? "",
            notifyAddress:    form.getValues("notifyAddress") ?? "",
            marks:            form.getValues("desc.marks") ?? "",
            natureQuantity:   form.getValues("desc.description") ?? "",
            incoterms:        form.getValues("incoterms") ?? "",
            blType:           form.getValues("seaDetail.blType") ?? "",
          }}
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
            didRestoreFromDraftRef.current = false;
            clearDraft(`house:${variant.key}:${id}`);
          }}
        />
      )}
      {id != null && (
        <BlAttachmentModal
          blKind="HOUSE"
          blId={id}
          isOpen={isAttachmentsOpen}
          onClose={() => setIsAttachmentsOpen(false)}
        />
      )}
    </>
  );
}
