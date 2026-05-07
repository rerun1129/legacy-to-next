"use client";

import { useState }                                from "react";
import { useForm, FormProvider }                  from "react-hook-form";
import { Save, Trash2, Package, FilePlus, Search, Copy, RefreshCw } from "lucide-react";
import { FreightTab }     from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }      from "./tabs/main-non-bl";
import type { NonBlFormValues }                   from "./non-bl-schema";
import { createEmptyNonBlFormValues }             from "./non-bl-defaults";
import { useBlDraftSync }                         from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                        from "@/lib/use-bl-draft-store";
import { TextBox, DropBox }                       from "@/components/shared/inputs";
import { useEnumOptions }                         from "@/application/enums/use-enum";

export function NonBLEntry() {
  const [tab, setTab] = useState("main");

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const methods = useForm<NonBlFormValues>({
    defaultValues: createEmptyNonBlFormValues(),
  });

  useBlDraftSync(methods, "non::new");

  const { register } = methods;

  const { options: workDivOptions, placeholder: workDivPlaceholder } = useEnumOptions("WorkDivision");
  const { options: boundOptions, placeholder: boundPlaceholder } = useEnumOptions("Bound");

  function handleResetEntry() {
    methods.reset(createEmptyNonBlFormValues());
    clearDraft("non::new");
  }

  return (
    <FormProvider {...methods}>
    <>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L Entry
        </div>
        <div className="page-head__meta"><span className="badge badge--draft">DRAFT</span></div>
        <div className="page-head__actions">
          <button type="button" className="btn btn--sm" onClick={handleResetEntry}>
            <FilePlus size={12} />New
          </button>
          <button type="button" className="btn btn--sm btn--search">
            <Search size={12} />Search
          </button>
          <button type="button" className="btn btn--sm btn--transaction">
            <Save size={12} />Save
          </button>
          <button type="button" className="btn btn--sm btn--danger">
            <Trash2 size={12} />Delete
          </button>
          <button type="button" className="btn btn--sm">
            <Copy size={12} />Copy
          </button>
          <button type="button" className="btn btn--sm btn--transaction">
            <RefreshCw size={12} />Change BL No
          </button>
        </div>
      </div>

      {/* gridTemplateColumns는 툴바 레이아웃에 필수이므로 인라인 유지 */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(6, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">Non B/L No</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Auto on save" {...register("nonBlNo")} />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Work Division</div>
          <div className="field__input">
            <DropBox variant="panel" options={workDivOptions} placeholder={workDivPlaceholder} {...register("workDiv")} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Bound</div>
          <div className="field__input">
            <DropBox variant="panel" options={boundOptions} placeholder={boundPlaceholder} {...register("bound")} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Ref. No.</div>
          <div className="field__input">
            <TextBox variant="panel" placeholder="Ref. No." {...register("refNo")} />
          </div>
        </div>
      </div>

      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map(t => (
          <button key={t.key} className={`tabbar__tab${tab === t.key ? " is-active" : ""}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
      </div>

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MainNonBL    active={tab === "main"}    /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab   active={tab === "freight"} /></div>
    </>
    </FormProvider>
  );
}
