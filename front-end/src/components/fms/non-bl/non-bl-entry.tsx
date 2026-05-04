"use client";

import { useState }                                from "react";
import { useForm, FormProvider }                  from "react-hook-form";
import { Save, Trash2, Package, Printer, FilePlus } from "lucide-react";
import { FreightTab }     from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }      from "./tabs/main-non-bl";
import type { NonBlFormValues }                   from "./non-bl-schema";
import { createEmptyNonBlFormValues }             from "./non-bl-defaults";
import { useBlDraftSync }                         from "@/lib/use-bl-draft-sync";
import { useBLDraftStore }                        from "@/lib/use-bl-draft-store";

export function NonBLEntry() {
  const [tab, setTab] = useState("main");

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const methods = useForm<NonBlFormValues>({
    defaultValues: createEmptyNonBlFormValues(),
  });

  useBlDraftSync(methods, "non::new");

  const { register } = methods;

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
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm btn--success"><Printer size={12} />Print</button>
          <button className="btn btn--sm btn--primary"><Save size={12} />Save</button>
        </div>
      </div>

      <div className="toolbar" style={{ gridTemplateColumns: "repeat(6, 1fr)" }}>
        <div className="field is-required">
          <div className="field__label is-required">Non B/L No</div>
          <div className="field__input">
            <input {...register("nonBlNo")} placeholder="Auto on save" />
          </div>
        </div>
        <div className="field is-required">
          <div className="field__label is-required">Work Division</div>
          <div className="field__input">
            <select
              {...register("workDiv")}
              style={{ all: "unset", flex: 1, minWidth: 0, fontSize: "var(--fs-base)", color: "var(--ink)", cursor: "pointer" }}
            >
              <option>Sea</option>
              <option>Air</option>
              <option>Warehouse</option>
              <option>Trucking</option>
            </select>
          </div>
        </div>
        <div className="field">
          <div className="field__label">Bound</div>
          <div className="field__input">
            <input {...register("bound")} placeholder="Bound" />
          </div>
        </div>
        <div className="field">
          <div className="field__label">Ref. No.</div>
          <div className="field__input">
            <input {...register("refNo")} placeholder="Ref. No." />
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
