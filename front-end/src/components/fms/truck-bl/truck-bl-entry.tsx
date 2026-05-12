"use client";

import { FormProvider, Controller }              from "react-hook-form";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { MainTruck }     from "./tabs/main-truck";
import { TextBox, ComboBox }                     from "@/components/shared/inputs";
import { ScreenGuard }                           from "@/components/shared/screen-guard";
import { toast }                                 from "@/lib/toast-store";
import { TruckBlEntryHeader }                    from "./truck-bl-entry-header";
import { TruckChangeBlNoModal }                  from "./truck-change-bl-no-modal";
import { useTruckBlEntry }                       from "./use-truck-bl-entry";

export function TruckBLEntry() {
  const entry = useTruckBlEntry();

  return (
    <FormProvider {...entry.form}>
    <ScreenGuard visible={entry.isLoading} message={entry.loadingMessage} />
    <form
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
          if (!entry.isEdit) {
            toast.info("먼저 Truck B/L을 조회해주세요.");
            return;
          }
          entry.form.handleSubmit(entry.handleSubmit)();
        }}
        onDelete={entry.handleDelete}
        onChangeBlNo={entry.handleChangeBlNo}
      />

      {/* Toolbar: 4필드 — gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">Truck B/L No</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...entry.register("truckBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Bound</div>
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
          <div className="field__label">Load Type</div>
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
          <div className="field__label">Service Term</div>
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
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map((t) => (
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
      <div style={{ display: entry.tab === "main"    ? "contents" : "none" }}><MainTruck   active={entry.tab === "main"}    /></div>
      <div style={{ display: entry.tab === "freight" ? "contents" : "none" }}><FreightTab active={entry.tab === "freight"} /></div>
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
    </FormProvider>
  );
}
