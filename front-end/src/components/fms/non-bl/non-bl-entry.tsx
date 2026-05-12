"use client";

import { FormProvider, Controller }               from "react-hook-form";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }     from "./tabs/main-non-bl";
import { TextBox, ComboBox }                      from "@/components/shared/inputs";
import { ScreenGuard }                            from "@/components/shared/screen-guard";
import { ChangeBlNoModal }                        from "./change-bl-no-modal";
import { toast }                                  from "@/lib/toast-store";
import { useNonBlEntry }                          from "./use-non-bl-entry";
import { NonBlEntryHeader }                       from "./non-bl-entry-header";

export function NonBLEntry() {
  const entry = useNonBlEntry();

  return (
    <FormProvider {...entry.methods}>
    <ScreenGuard visible={entry.isLoading} message={entry.loadingMessage} />
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
          if (!entry.isEdit) {
            toast.info("먼저 Non B/L을 조회해주세요.");
            return;
          }
          entry.methods.handleSubmit(entry.handleSubmit)();
        }}
        onDelete={entry.handleDelete}
        onChangeBlNo={entry.handleChangeBlNo}
      />

      {/* gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(6, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">Non B/L No</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...entry.register("nonBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Work Division</div>
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
          <div className="field__label is-required">Bound</div>
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
          <div className="field__label">Ref. No.</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Ref. No." {...entry.register("refNo")} />
          </div>
        </div>
      </div>

      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map(t => (
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
      <div style={{ display: entry.tab === "main"    ? "contents" : "none" }}><MainNonBL    active={entry.tab === "main"}    /></div>
      <div style={{ display: entry.tab === "freight" ? "contents" : "none" }}><FreightTab   active={entry.tab === "freight"} /></div>
    </form>
    {entry.isEdit && entry.id && (
      <ChangeBlNoModal
        houseBlId={entry.id}
        currentHblNo={entry.detail?.hblNo}
        isOpen={entry.isChangeBlNoModalOpen}
        onClose={() => entry.setIsChangeBlNoModalOpen(false)}
        onChanged={() => { entry.detailLoadedRef.current = false; }}
      />
    )}
    </FormProvider>
  );
}
