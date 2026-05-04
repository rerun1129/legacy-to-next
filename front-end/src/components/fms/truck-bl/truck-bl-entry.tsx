"use client";

import { useState } from "react";
import { useForm, FormProvider } from "react-hook-form";
import { Save, Trash2, Truck, FilePlus } from "lucide-react";
import { FreightTab } from "@/components/fms/house-bl/tabs/freight-tab";
import { MainTruck }  from "./tabs/main-truck";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { createEmptyTruckBlFormValues } from "./truck-bl-defaults";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";

// label → RHF 필드 경로 매핑 (toolbar 5개)
const TOOLBAR_REGISTER: Record<string, keyof HouseBlFormValues> = {
  "Truck B/L No": "truckBlNo",
  "Settle":       "truckSettle",
  "Incoterms":    "incoterms",
  "Freight Term": "truckFreightTerm",
  "Status":       "truckStatus",
};

export function TruckBLEntry() {
  const [tab, setTab] = useState("main");
  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const form = useForm<HouseBlFormValues>({
    defaultValues: createEmptyTruckBlFormValues(),
  });

  useBlDraftSync(form, "truck::new");

  function handleResetEntry() {
    form.reset(createEmptyTruckBlFormValues());
    clearDraft("truck::new");
  }

  return (
    <FormProvider {...form}>
    <>
      {/* Page header — NO Print button per PRD §S-06 */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Truck size={14} /></div>
          Truck B/L Entry
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <button type="button" className="btn btn--sm" onClick={handleResetEntry}>
            <FilePlus size={12} />New
          </button>
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm btn--primary">
            <Save size={12} />Save
          </button>
        </div>
      </div>

      {/* Toolbar: Document Key fields */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(5, 1fr)" }}>
        {[
          { l: "Truck B/L No",    req: true  },
          { l: "Settle",          req: true  },
          { l: "Incoterms",       req: false },
          { l: "Freight Term",    req: false },
          { l: "Status",          req: false },
        ].map((f) => (
          <div key={f.l} className={`field${f.req ? " is-required" : ""}`}>
            <div className={`field__label${f.req ? " is-required" : ""}`}>{f.l}</div>
            <div className="field__input">
              <input
                {...form.register(TOOLBAR_REGISTER[f.l])}
                placeholder={f.l === "Truck B/L No" ? "Auto on save" : f.l}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Tabbar — 2 tabs only */}
      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map((t) => (
          <button key={t.key} className={`tabbar__tab${tab === t.key ? " is-active" : ""}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
      </div>

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MainTruck   active={tab === "main"}    /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab active={tab === "freight"} /></div>
    </>
    </FormProvider>
  );
}
