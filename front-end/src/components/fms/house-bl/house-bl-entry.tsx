"use client";

import { useRef, useState } from "react";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
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
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { ScreenGuard } from "@/components/shared/screen-guard";
import { useHouseBlEntryDetailSync } from "./use-house-bl-entry-detail-sync";
import { useHouseBlEntryMutations } from "./use-house-bl-entry-mutations";
import { useHouseBlEntryHandlers } from "./use-house-bl-entry-handlers";
import { HouseBlEntryPageHead } from "./house-bl-entry-page-head";

function getToolbarFields(variant: BLVariantConfig): ReadonlyArray<string> {
  if (variant.mode === "SEA")   return TOOLBAR_FIELDS_SEA;
  if (variant.mode === "AIR")   return TOOLBAR_FIELDS_AIR;
  if (variant.mode === "TRUCK") return TOOLBAR_FIELDS_TRUCK;
  return TOOLBAR_FIELDS_NON_BL;
}

function renderMainTab(variant: BLVariantConfig, active: boolean, resetVersion: number) {
  if (variant.mode === "SEA") return <MainTabSea key={resetVersion} variant={variant} active={active} />;
  if (variant.mode === "AIR") return <MainTabAir key={resetVersion} variant={variant} active={active} />;
  return <MainTabSea key={resetVersion} variant={variant} active={active} />;
}

interface Props {
  variant: BLVariantConfig;
}

export function HouseBLEntry({ variant }: Props) {
  const [tab, setTab] = useState("main");
  const [isSwitchBlModalOpen, setIsSwitchBlModalOpen] = useState(false);
  const [isChangeBlNoModalOpen, setIsChangeBlNoModalOpen] = useState(false);
  const [resetVersion, setResetVersion] = useState(0);
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
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
  const canSwitchBl = isEdit && id != null && variant.key.startsWith("sea-");

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    setTab(key);
  }

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "freight", label: "Freight" },
  ];

  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : mutation.isPending ? "저장 중..." : "조회 중...";

  return (
    <>
      <ScreenGuard visible={isLoading} message={loadingMessage} />
      <FormProvider {...form}>
      <form ref={formRef} onSubmit={form.handleSubmit(handleSubmit)} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
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

        <div style={{ display: tab === "main"    ? "contents" : "none" }}>{renderMainTab(variant, tab === "main", resetVersion)}</div>
        <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab key={resetVersion} active={tab === "freight"} /></div>
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
    </>
  );
}
