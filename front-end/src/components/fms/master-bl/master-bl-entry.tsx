"use client";

import { useRef, useState, useEffect } from "react";
import { useBlDraftSync } from "@/lib/use-bl-draft-sync";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { useForm, FormProvider } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { TOOLBAR_TO_FIELD } from "./master-bl-schema";
import type { MasterBlFormValues } from "./master-bl-schema";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import { mapMasterBlDetailToForm } from "./map-master-bl-detail";
import { buildCreateMasterBlPayload, buildUpdateMasterBlPayload } from "./master-bl-submit";
import { Save, Copy, Trash2, Layers, RefreshCw, Search, FilePlus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { getMasterVariant, getPageTitle } from "@/lib/bl-variants";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { masterBlPort } from "@/lib/ports";
import type { ConsolidatedHouseBlSummary } from "@/domain/master-bl";
import { MasterMainTab } from "./tabs/main-tab";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";
import { ScreenGuard }   from "@/components/shared/screen-guard";

interface Props {
  variantKey: string;
  id?: number;
}

const TOOLBAR_SEA = ["Master Ref", "MBL No", "Line Bkg. No", "Load Type", "Service Term", "B/L Type", "Shipment Type"] as const;
const TOOLBAR_AIR = ["Master Ref", "MAWB No", "Shipment Type", "", "", "", ""] as const;

function getToolbarFields(mode: string) {
  return mode === "SEA" ? TOOLBAR_SEA : TOOLBAR_AIR.filter(Boolean);
}

export function MasterBLEntry({ variantKey, id }: Props) {
  const [tab, setTab] = useState("main");
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
  const router = useRouter();
  const queryClient = useQueryClient();

  const variant = getMasterVariant(variantKey);
  const isEdit = Boolean(id);
  const modeLabels = getModeLabels(variant.mode);
  const toolbarFields = getToolbarFields(variant.mode);

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    setTab(key);
  }

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["master-bl", "detail", id],
    queryFn: () => masterBlPort.getById(id!),
    enabled: isEdit,
  });

  const clearDraft = useBLDraftStore(state => state.clearDraft);

  const form = useForm<MasterBlFormValues>({
    defaultValues: {
      ...createEmptyMasterBlFormValues(),
      jobDiv: variant.mode,
      bound: variant.direction ?? "EXP",
    },
  });

  useBlDraftSync(form, `master:${variantKey}:${id ?? "new"}`);

  const detailLoadedRef = useRef<boolean>(false);

  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    form.reset(mapMasterBlDetailToForm(detail));
  }, [detail, form]);

  const mutation = useMutation<{ id: number } | void, Error, MasterBlFormValues>({
    mutationFn: (data: MasterBlFormValues) => {
      return isEdit
        ? masterBlPort.update(id!, buildUpdateMasterBlPayload(id!, data, variant))
        : masterBlPort.create(buildCreateMasterBlPayload(data, variant));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["master-bl", "list"] });
      router.push(`/fms/master-bl/${variantKey}/list`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => masterBlPort.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['master-bl', 'list'] });
      form.reset({
        ...createEmptyMasterBlFormValues(),
        jobDiv: variant.mode,
        bound: variant.direction ?? "EXP",
      });
      router.replace(`/fms/master-bl/${variantKey}/entry`);
    },
  });

  function handleSearchBl() {
    const mblValue = form.getValues('mblNo');
    if (!mblValue?.trim()) return;

    masterBlPort
      .list({
        bound: variant.direction as 'EXP' | 'IMP',
        mblNo: mblValue.trim(),
      })
      .then((rows) => {
        if (rows.length === 0) {
          alert('해당 B/L을 찾을 수 없습니다.');
          return;
        }
        queryClient.invalidateQueries({ queryKey: ['master-bl', 'detail', rows[0].id] });
        clearDraft(`master:${variantKey}:${rows[0].id}`);
        return masterBlPort.getById(rows[0].id).then((foundDetail) => {
          form.reset(mapMasterBlDetailToForm(foundDetail));
        });
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : String(err);
        alert(`B/L 조회 중 오류가 발생했습니다: ${message}`);
      });
  }

  function handleResetEntry() {
    form.reset({
      ...createEmptyMasterBlFormValues(),
      jobDiv: variant.mode,
      bound: variant.direction ?? "EXP",
    });
    clearDraft(`master:${variantKey}:${id ?? "new"}`);
    formRef.current?.reset();
  }

  function handleSave(raw: MasterBlFormValues) {
    mutation.mutate(raw);
  }

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "freight", label: "Freight" },
  ];

  const { register } = form;
  const bottomActionsLeft  = variant.bottomActions.filter(a => ["Profit/Loss", "House B/L Load"].includes(a));
  const bottomActionsRight = variant.bottomActions.filter(a => !["Profit/Loss", "House B/L Load"].includes(a));

  const isLoading = isDetailFetching || mutation.isPending || deleteMutation.isPending;
  const loadingMessage = deleteMutation.isPending ? "삭제 중..." : mutation.isPending ? "저장 중..." : "조회 중...";

  return (
    <FormProvider {...form}>
    <ScreenGuard visible={isLoading} message={loadingMessage} />
    <form ref={formRef} onSubmit={form.handleSubmit(handleSave)} style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
      {/* Page header — NOTE: No Print button per PRD §S-04 */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Layers size={14} /></div>
          {getPageTitle(variant, "Master", "Entry")}
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <button type="button" className="btn btn--sm" onClick={handleResetEntry}>
            <FilePlus size={12} />New
          </button>
          <button type="button" className="btn btn--sm" onClick={handleSearchBl}>
            <Search size={12} />Search B/L
          </button>
          <Button
            variant="danger"
            size="sm"
            onClick={() => {
              if (!isEdit) return;
              if (window.confirm('삭제하시겠습니까?')) deleteMutation.mutate();
            }}
            disabled={!isEdit || deleteMutation.isPending}
          >
            <Trash2 size={12} />Delete
          </Button>
          <button type="button" className="btn btn--sm"><Copy size={12} />Copy</button>
          <button type="button" className="btn btn--sm">
            <RefreshCw size={12} />{modeLabels.changeBLNo}
          </button>
          <button type="button" className="btn btn--sm" onClick={handleResetEntry}>
            <FilePlus size={12} />New
          </button>
          <button type="submit" className="btn btn--sm btn--primary" disabled={mutation.isPending}>
            <Save size={12} />{mutation.isPending ? "Saving..." : "Save"}
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar" style={{ gridTemplateColumns: `repeat(${variant.toolbarColumnCount}, 1fr)` }}>
        {toolbarFields.map((f) => {
          const fieldPath = TOOLBAR_TO_FIELD[f];
          return (
            <div key={f} className={`field${["MBL No","MAWB No","Master Ref"].includes(f) ? " is-required" : ""}`}>
              <div className={`field__label${["MBL No","MAWB No","Master Ref"].includes(f) ? " is-required" : ""}`}>{f}</div>
              <div className="field__input">
                {fieldPath
                  ? <input placeholder={f || ""} {...(register as (name: string) => object)(fieldPath)} />
                  : <input placeholder={f || ""} />
                }
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
        <div className="tabbar__spacer" />
        <div className="tabbar__meta">
          <span>Last saved: 10 min ago</span>
        </div>
      </div>

      {/* consolidatedHouseBls: detail fetch 시 읽기 전용 표시 전용, submit body에 미포함 */}
      {isEdit && detail?.consolidatedHouseBls && detail.consolidatedHouseBls.length > 0 && (
        <div className="consolidated-hbl-list" aria-label="Consolidated House B/L list (read-only)">
          {detail.consolidatedHouseBls.map((hbl: ConsolidatedHouseBlSummary) => (
            <span key={hbl.id} className="badge">{hbl.hblNo ?? `HBL#${hbl.id}`}</span>
          ))}
        </div>
      )}

      {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
      <div style={{ display: tab === "main"    ? "contents" : "none" }}><MasterMainTab variant={variant} form={form} active={tab === "main"} /></div>
      <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab active={tab === "freight"} /></div>
    </form>
    </FormProvider>
  );
}
