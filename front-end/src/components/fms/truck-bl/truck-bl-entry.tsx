"use client";

import { useCallback, useState }     from "react";
import { useTranslations }               from "next-intl";
import { FormProvider, Controller }       from "react-hook-form";
import { useQueryClient }                from "@tanstack/react-query";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainTruck }     from "./tabs/main-truck";
import { TextBox, ComboBox }              from "@/components/shared/inputs";
import { ScreenGuard }                   from "@/components/shared/screen-guard";
import { TruckBlEntryHeader }            from "./truck-bl-entry-header";
import { TruckChangeBlNoModal }          from "./truck-change-bl-no-modal";
import { BlAttachmentModal }             from "@/components/fms/shared/bl-attachment-modal";
import { useTruckBlEntry }               from "./use-truck-bl-entry";

export function TruckBLEntry() {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const [isAttachmentsOpen, setIsAttachmentsOpen] = useState(false);
  const tb  = useTranslations("fms.truckBl.entry.toolbar");
  const tts = useTranslations("fms.truckBl.entry.tabs");
  const tm  = useTranslations("fms.truckBl.entry.msg");

  const queryClient = useQueryClient();
  const entry = useTruckBlEntry();

  // 발행/서류삭제 성공 시 entry detail을 강제 재조회하는 콜백.
  // detailLoadedRef는 훅 내부 소유 — 직접 변경 대신 훅이 제공하는 콜백을 경유해 변경함(React Compiler 준수).
  const handleFreightMutated = useCallback(() => {
    entry.resetDetailLoaded();
    if (entry.id != null) {
      queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", entry.id] });
    }
  }, [entry, queryClient]);

  const loadingMessage = entry.deleteMutation.isPending
    ? tm("deleting")
    : entry.isSavePending
      ? tm("saving")
      : tm("fetching");

  const tabs = [
    { key: "main",    label: tts("main")    },
    { key: "freight", label: tts("freight") },
  ];

  return (
    <FormProvider {...entry.form}>
    <ScreenGuard visible={entry.isLoading} message={loadingMessage} />
    <form
      noValidate
      onSubmit={entry.form.handleSubmit(entry.handleSubmit)}
      onKeyDown={(e) => {
        // textarea 줄바꿈은 보존, 그 외 Enter는 implicit form submission 차단
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
      style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}
    >
      <TruckBlEntryHeader
        isEdit={entry.isEdit}
        isSavePending={entry.isSavePending}
        isDeletePending={entry.deleteMutation.isPending}
        onNew={entry.handleResetEntry}
        onSearch={entry.handleSearch}
        onSave={() => {
          entry.form.handleSubmit(entry.handleSubmit)();
        }}
        onDelete={entry.handleDelete}
        onChangeBlNo={entry.handleChangeBlNo}
        onOpenAttachments={() => setIsAttachmentsOpen(true)}
      />

      {/* Toolbar: 4필드 — gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">{tb("truckBlNo")}</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...entry.register("truckBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">{tb("bound")}</div>
          <div className="field__input">
            <Controller
              name="bound"
              control={entry.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={entry.boundOptions}
                  placeholder={entry.boundPlaceholder}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{tb("loadType")}</div>
          <div className="field__input">
            <Controller
              name="loadType"
              control={entry.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={entry.loadTypeOptions}
                  placeholder={entry.loadTypePlaceholder}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{tb("serviceTerm")}</div>
          <div className="field__input">
            <Controller
              name="serviceTerm"
              control={entry.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={entry.serviceTermOptions}
                  placeholder={entry.serviceTermPlaceholder}
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
        {tabs.map((t) => (
          <button
            key={t.key}
            type="button"
            className={`tabbar__tab${entry.tab === t.key ? " is-active" : ""}`}
            onClick={() => entry.setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
      </div>

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      {/* nonce를 key에 포함해 Copy 신호(new→new)도 리마운트 트리거 */}
      <div style={{ display: entry.tab === "main"    ? "contents" : "none" }}><MainTruck   key={`${entry.resetVersion}:${entry.nonce ?? 0}`} active={entry.tab === "main"}    /></div>
      <div style={{ display: entry.tab === "freight" ? "contents" : "none" }}><FreightTab key={`${entry.resetVersion}:${entry.nonce ?? 0}`} active={entry.tab === "freight"} layoutScope="truck-bl-entry.freight" blType="HOUSE" blId={entry.id ?? null} onFreightMutated={handleFreightMutated} /></div>
    </form>
    {entry.isEdit && entry.id && (
      <TruckChangeBlNoModal
        truckBlId={entry.id}
        currentHblNo={entry.detail?.hblNo}
        isOpen={entry.isChangeBlNoModalOpen}
        onClose={() => entry.setIsChangeBlNoModalOpen(false)}
        onChanged={entry.resetDetailLoaded}
      />
    )}
    {entry.id != null && (
      <BlAttachmentModal
        blKind="TRUCK"
        blId={entry.id}
        isOpen={isAttachmentsOpen}
        onClose={() => setIsAttachmentsOpen(false)}
      />
    )}
    </FormProvider>
  );
}
