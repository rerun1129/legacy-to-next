"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { z } from "zod";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { Save, Copy, Trash2, Layers, Send, RefreshCw, Search } from "lucide-react";
import { getMasterVariant, getPageTitle } from "@/lib/bl-variants";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { masterBlPort } from "@/lib/ports";
import type { CreateMasterBlRequest, UpdateMasterBlRequest, ConsolidatedHouseBlSummary } from "@/domain/master-bl";
import { MasterMainTab } from "./tabs/main-tab";
import { MasterEdiTab }  from "./tabs/edi-tab";
import { OtherTab }      from "@/components/fms/house-bl/tabs/other-tab";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";

interface Props {
  variantKey: string;
  id?: number;
}

const TOOLBAR_SEA = ["Master Ref", "MBL No", "Line Bkg. No", "Load Type", "Service Term", "B/L Type", "Shipment Type", "Status"] as const;
const TOOLBAR_AIR = ["Master Ref", "MAWB No", "Shipment Type", "Status", "", "", "", ""] as const;

// freightTerm 값이 null일 수 있으므로 detail reset 시 기본값 fallback 처리
const MASTER_BL_SCHEMA = z.object({
  jobDiv: z.enum(["SEA", "AIR", "TRUCK", "NON_BL"]),
  bound: z.enum(["EXP", "IMP"]),
  mblNo: z.string().max(35).optional(),
  masterRefNo: z.string().max(35).optional(),
  freightTerm: z.enum(["PREPAID", "COLLECT"]),
  shipperCode: z.string().max(20).optional(),
  consigneeCode: z.string().max(20).optional(),
  polCode: z.string().max(5).optional(),
  podCode: z.string().max(5).optional(),
  etd: z.string().regex(/^\d{8}$/).optional().or(z.literal("")),
  eta: z.string().regex(/^\d{8}$/).optional().or(z.literal("")),
  pkgQty: z.number().min(0).optional(),
  grossWeightKg: z.number().min(0).optional(),
  cbm: z.number().min(0).optional(),
  operatorCode: z.string().optional(),
});

type FormValues = z.infer<typeof MASTER_BL_SCHEMA>;

function getToolbarFields(mode: string) {
  return mode === "SEA" ? TOOLBAR_SEA : TOOLBAR_AIR.filter(Boolean);
}

export function MasterBLEntry({ variantKey, id }: Props) {
  const [tab, setTab] = useState("main");
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

  const { data: detail } = useQuery({
    queryKey: ["master-bl", "detail", id],
    queryFn: () => masterBlPort.getById(id!),
    enabled: isEdit,
  });

  const form = useForm<FormValues>({
    defaultValues: {
      jobDiv: "SEA",
      bound: "EXP",
      freightTerm: "PREPAID",
      mblNo: "",
      masterRefNo: "",
      shipperCode: "",
      consigneeCode: "",
      polCode: "",
      podCode: "",
      etd: "",
      eta: "",
      operatorCode: "",
    },
  });

  useEffect(() => {
    if (!detail) return;
    form.reset({
      jobDiv: detail.jobDiv,
      bound: detail.bound,
      // detail.freightTerm이 null이면 기본값 유지 (DB 레거시 데이터 대응)
      freightTerm: detail.freightTerm ?? "PREPAID",
      mblNo: detail.mblNo ?? "",
      masterRefNo: detail.masterRefNo ?? "",
      shipperCode: detail.shipperCode ?? "",
      consigneeCode: detail.consigneeCode ?? "",
      polCode: detail.polCode ?? "",
      podCode: detail.podCode ?? "",
      etd: detail.etd ?? "",
      eta: detail.eta ?? "",
      pkgQty: detail.pkgQty ?? undefined,
      grossWeightKg: detail.grossWeightKg ?? undefined,
      cbm: detail.cbm ?? undefined,
      operatorCode: detail.operatorCode ?? "",
    });
  }, [detail, form]);

  const mutation = useMutation({
    mutationFn: (data: FormValues) =>
      isEdit
        ? masterBlPort.update(id!, data as UpdateMasterBlRequest)
        : masterBlPort.create(data as CreateMasterBlRequest),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["master-bl", "list"] });
      router.push(`/fms/master-bl/${variantKey}/list`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => masterBlPort.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['master-bl', 'list'] });
      form.reset();
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
        return masterBlPort.getById(rows[0].id).then((detail) => {
          form.reset({
            jobDiv: detail.jobDiv,
            bound: detail.bound,
            freightTerm: detail.freightTerm ?? 'PREPAID',
            mblNo: detail.mblNo ?? '',
            masterRefNo: detail.masterRefNo ?? '',
            shipperCode: detail.shipperCode ?? '',
            consigneeCode: detail.consigneeCode ?? '',
            polCode: detail.polCode ?? '',
            podCode: detail.podCode ?? '',
            etd: detail.etd ?? '',
            eta: detail.eta ?? '',
            pkgQty: detail.pkgQty ?? undefined,
            grossWeightKg: detail.grossWeightKg ?? undefined,
            cbm: detail.cbm ?? undefined,
            operatorCode: detail.operatorCode ?? '',
          });
        });
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : String(err);
        alert(`B/L 조회 중 오류가 발생했습니다: ${message}`);
      });
  }

  function handleSave(raw: FormValues) {
    const req: CreateMasterBlRequest = {
      jobDiv: variant.mode as 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL',
      bound: variant.direction as 'EXP' | 'IMP',
      freightTerm: 'PREPAID',
      ...raw,
    };
    mutation.mutate(req as FormValues);
  }

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "edi",     label: "EDI"     },
    { key: "other",   label: "Other"   },
    { key: "freight", label: "Freight" },
  ];

  const isExp = variant.direction === "EXP";
  const bottomActionsLeft  = variant.bottomActions.filter(a => ["Profit/Loss", "House B/L Load"].includes(a));
  const bottomActionsRight = variant.bottomActions.filter(a => !["Profit/Loss", "House B/L Load"].includes(a));

  return (
    <form onSubmit={form.handleSubmit(handleSave)}>
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
          <button type="button" className="btn btn--sm" onClick={handleSearchBl}>
            <Search size={12} />Search B/L
          </button>
          <button
            type="button"
            className="btn btn--sm btn--danger"
            onClick={() => {
              if (!isEdit) return;
              if (window.confirm('삭제하시겠습니까?')) deleteMutation.mutate();
            }}
            disabled={!isEdit || deleteMutation.isPending}
          >
            <Trash2 size={12} />Delete
          </button>
          <button type="button" className="btn btn--sm"><Copy size={12} />Copy</button>
          <button type="button" className="btn btn--sm">
            <RefreshCw size={12} />{modeLabels.changeBLNo}
          </button>
          <button type="button" className="btn btn--sm btn--info"><Send size={12} />EDI</button>
          <button type="submit" className="btn btn--sm btn--primary" disabled={mutation.isPending}>
            <Save size={12} />{mutation.isPending ? "Saving..." : "Save"}
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar" style={{ gridTemplateColumns: `repeat(${variant.toolbarColumnCount}, 1fr)` }}>
        {toolbarFields.map((f) => (
          <div key={f} className={`field${["MBL No","MAWB No","Master Ref"].includes(f) ? " is-required" : ""}`}>
            <div className={`field__label${["MBL No","MAWB No","Master Ref"].includes(f) ? " is-required" : ""}`}>{f}</div>
            <div className="field__input">
              {(f === "MBL No" || f === "MAWB No") ? (
                <input {...form.register('mblNo')} placeholder={f} />
              ) : (
                <input defaultValue={
                  f === "Master Ref" ? "MR-2026-04195" :
                  f === "Load Type" ? "FCL" :
                  f === "Service Term" ? "CY/CY" :
                  f === "B/L Type" ? "OBL" :
                  f === "Line Bkg. No" ? "BKG-COSCO-0412" :
                  f === "Shipment Type" ? "FCL" :
                  ""
                } placeholder={f || ""} />
              )}
            </div>
          </div>
        ))}
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

      {/* Tab content */}
      {tab === "main"    && <MasterMainTab variant={variant} />}
      {tab === "edi"     && <MasterEdiTab variant={variant} />}
      {tab === "other"   && <OtherTab />}
      {tab === "freight" && <FreightTab />}
    </form>
  );
}
