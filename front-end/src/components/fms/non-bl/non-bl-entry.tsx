"use client";

import { useTranslations }                        from "next-intl";
import { FormProvider, Controller }               from "react-hook-form";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }     from "./tabs/main-non-bl";
import { TextBox, ComboBox }                      from "@/components/shared/inputs";
import { ScreenGuard }                            from "@/components/shared/screen-guard";
import { ChangeBlNoModal }                        from "./change-bl-no-modal";
import { useNonBlEntry }                          from "./use-non-bl-entry";
import { NonBlEntryHeader }                       from "./non-bl-entry-header";

export function NonBLEntry() {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tb  = useTranslations("fms.nonBl.entry.toolbar");
  const tts = useTranslations("fms.nonBl.entry.tabs");
  const tm  = useTranslations("fms.nonBl.entry.msg");

  const entry = useNonBlEntry();

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
    <FormProvider {...entry.methods}>
    <ScreenGuard visible={entry.isLoading} message={loadingMessage} />
    <form
      onSubmit={entry.methods.handleSubmit(entry.handleSubmit)}
      onKeyDown={(e) => {
        // textarea 줄바꿈은 보존, 그 외 Enter는 implicit form submission 차단
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
      style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}
    >
      <NonBlEntryHeader
        isEdit={entry.isEdit}
        isSavePending={entry.isSavePending}
        isDeletePending={entry.deleteMutation.isPending}
        onNew={entry.handleResetEntry}
        onSearch={entry.handleSearch}
        onSave={() => {
          entry.methods.handleSubmit(entry.handleSubmit)();
        }}
        onDelete={entry.handleDelete}
        onChangeBlNo={entry.handleChangeBlNo}
      />

      {/* Toolbar: 4필드 — gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(6, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">{tb("nonBlNo")}</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...entry.register("nonBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">{tb("workDiv")}</div>
          <div className="field__input">
            <Controller
              name="workDiv"
              control={entry.control}
              render={({ field }) => (
                <ComboBox variant="panel" options={entry.workDivOptions} placeholder={entry.workDivPlaceholder} value={field.value} onChange={field.onChange} />
              )}
            />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">{tb("bound")}</div>
          <div className="field__input">
            <Controller
              name="bound"
              control={entry.control}
              render={({ field }) => (
                <ComboBox variant="panel" options={entry.boundOptions} placeholder={entry.boundPlaceholder} value={field.value} onChange={field.onChange} />
              )}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{tb("refNo")}</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Ref. No." {...entry.register("refNo")} />
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
      <div style={{ display: entry.tab === "main"    ? "contents" : "none" }}><MainNonBL    key={`${entry.resetVersion}:${entry.nonce ?? 0}`} active={entry.tab === "main"}    /></div>
      <div style={{ display: entry.tab === "freight" ? "contents" : "none" }}><FreightTab   key={`${entry.resetVersion}:${entry.nonce ?? 0}`} active={entry.tab === "freight"} /></div>
    </form>
    {entry.isEdit && entry.id && (
      <ChangeBlNoModal
        houseBlId={entry.id}
        currentHblNo={entry.detail?.hblNo}
        isOpen={entry.isChangeBlNoModalOpen}
        onClose={() => entry.setIsChangeBlNoModalOpen(false)}
        onChanged={entry.resetDetailLoaded}
      />
    )}
    </FormProvider>
  );
}
