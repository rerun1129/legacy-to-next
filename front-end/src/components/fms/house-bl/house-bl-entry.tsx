"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { Save, Printer, Copy, Trash2, FileText, Send, Download } from "lucide-react";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { getPageTitle } from "@/lib/bl-variants";
import { MainTabSea }  from "./tabs/main-sea";
import { MainTabAir }  from "./tabs/main-air";
import { EdiTab }      from "./tabs/edi-tab";
import { OtherTab }    from "./tabs/other-tab";
import { FreightTab }  from "./tabs/freight-tab";
import { mockHouseBlPort } from "@/adapter/out/mock/house-bl";

// @hookform/resolvers 미설치로 zodResolver 없이 useForm 단독 사용. 검증은 submit 시 수동 호출.
const HOUSE_BL_SCHEMA = z.object({
  hbl:    z.string().max(35),
  mbl:    z.string().max(35),
  sType:  z.string(),
  lType:  z.string(),
  etd:    z.string().regex(/^\d{8}$/).or(z.literal("")).optional(),
  eta:    z.string().regex(/^\d{8}$/).or(z.literal("")).optional(),
  pol:    z.string().max(5).optional(),
  pod:    z.string().max(5).optional(),
  settle: z.enum(["PREPAID", "COLLECT"]),
  expImp: z.enum(["EXP", "IMP"]),
});

type FormValues = z.infer<typeof HOUSE_BL_SCHEMA>;

interface Props {
  variant: BLVariantConfig;
  id?: number;
}

const TOOLBAR_FIELDS_SEA = [
  "Shipment Type", "Settle", "HBL No", "MBL No", "Load Type", "Service Term", "B/L Type", "Master Ref",
] as const;
const TOOLBAR_FIELDS_AIR = [
  "Shipment Type", "Settle", "HAWB No", "MAWB No", "Rate Class", "Service Term", "B/L Type", "Master Ref",
] as const;

const DEFAULTS_SEA: Record<string, string> = {
  "Shipment Type": "FCL", "Settle": "PREPAID", "HBL No": "HBLKR24041956",
  "MBL No": "COSCO2404195", "Load Type": "CY/CY", "Service Term": "FCL",
  "B/L Type": "OBL", "Master Ref": "",
};
const DEFAULTS_AIR: Record<string, string> = {
  "Shipment Type": "GCR", "Settle": "PREPAID", "HAWB No": "HAWBKR24041001",
  "MAWB No": "180-12345678", "Rate Class": "GCR", "Service Term": "D2D",
  "B/L Type": "", "Master Ref": "",
};

function getToolbarFields(variant: BLVariantConfig) {
  return variant.mode === "SEA" ? TOOLBAR_FIELDS_SEA : TOOLBAR_FIELDS_AIR;
}

function getToolbarDefaults(variant: BLVariantConfig) {
  return variant.mode === "SEA" ? DEFAULTS_SEA : DEFAULTS_AIR;
}

function renderMainTab(variant: BLVariantConfig) {
  return variant.mode === "SEA"
    ? <MainTabSea variant={variant} />
    : <MainTabAir variant={variant} />;
}

/** toolbar의 라벨명 → form field 이름 매핑 */
const TOOLBAR_LABEL_TO_FIELD: Record<string, keyof FormValues> = {
  "HBL No":  "hbl",
  "HAWB No": "hbl",
  "MBL No":  "mbl",
  "MAWB No": "mbl",
  "Load Type": "lType",
  "Settle":  "settle",
};

const REQUIRED_TOOLBAR_LABELS = new Set(["HBL No", "HAWB No", "Shipment Type", "Settle"]);

export function HouseBLEntry({ variant, id }: Props) {
  const [tab, setTab] = useState("main");
  const { setCanEdit } = useWidgetLayout();
  const isEdit = Boolean(id);
  const queryClient = useQueryClient();
  const router = useRouter();

  const defaults = getToolbarDefaults(variant);

  // form 초기화 - defaultValues는 DEFAULTS_SEA/DEFAULTS_AIR 기반
  const form = useForm<FormValues>({
    defaultValues: {
      hbl:    defaults["HBL No"] ?? defaults["HAWB No"] ?? "",
      mbl:    defaults["MBL No"] ?? defaults["MAWB No"] ?? "",
      sType:  defaults["Shipment Type"] ?? "",
      lType:  defaults["Load Type"] ?? "",
      etd:    "",
      eta:    "",
      pol:    "",
      pod:    "",
      settle: "PREPAID",
      expImp: variant.direction,
    },
  });

  // 수정 모드: 기존 데이터 fetch
  const { data: detail } = useQuery({
    queryKey: ["house-bl", "detail", id],
    queryFn: () => mockHouseBlPort.getById(id!),
    enabled: isEdit,
  });

  // 수정 모드: 서버 데이터 로드 시 form reset
  useEffect(() => {
    if (detail) {
      form.reset({
        hbl:    detail.hblNo ?? "",
        mbl:    detail.masterBlId != null ? String(detail.masterBlId) : "",
        sType:  detail.shipmentType ?? "",
        lType:  detail.blType ?? "",
        etd:    detail.etd ?? "",
        eta:    detail.eta ?? "",
        pol:    detail.polCode ?? "",
        pod:    detail.podCode ?? "",
        settle: "PREPAID",
        expImp: detail.bound,
      });
    }
  }, [detail, form]);

  const mutation = useMutation({
    mutationFn: (data: FormValues) =>
      mockHouseBlPort.save({
        ...data,
        ...(isEdit ? { id } : {}),
        docStatus: "draft",
        regDate: "",
        vessel: "",
        voyage: "",
        shipper: "",
        consignee: "",
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["house-bl", "list"] });
      router.push(`/fms/house-bl/${variant.key}/list`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => mockHouseBlPort.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['house-bl', 'list'] });
      router.push(`/fms/house-bl/${variant.key}/list`);
    },
  });

  function handleTabChange(key: string) {
    setCanEdit(key === "main" || key === "freight");
    setTab(key);
  }

  const toolbarFields = getToolbarFields(variant);

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "edi",     label: "EDI"     },
    { key: "other",   label: "Other"   },
    { key: "freight", label: "Freight" },
  ];

  function handleSubmit(raw: FormValues) {
    // zodResolver 없이 수동 parse로 입력 경계 검증 수행
    const result = HOUSE_BL_SCHEMA.safeParse(raw);
    if (!result.success) return;
    mutation.mutate(result.data);
  }

  return (
    <form onSubmit={form.handleSubmit(handleSubmit)}>
      {/* Page header */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><FileText size={14} /></div>
          {getPageTitle(variant, 'House', 'Entry')}
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
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
          <button type="button" className="btn btn--sm"><Download size={12} />Export</button>
          {variant.printDocs.length > 0 && (
            <button type="button" className="btn btn--sm btn--success"><Printer size={12} />Print</button>
          )}
          <button type="button" className="btn btn--sm btn--info"><Send size={12} />EDI</button>
          <button
            type="submit"
            className="btn btn--sm btn--primary"
            disabled={mutation.isPending}
          >
            <Save size={12} />{mutation.isPending ? "Saving..." : "Save"}
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar">
        {toolbarFields.map((f) => {
          const fieldName = TOOLBAR_LABEL_TO_FIELD[f];
          const isRequired = REQUIRED_TOOLBAR_LABELS.has(f);
          return (
            <div key={f} className={`field${isRequired ? " is-required" : ""}`}>
              <div className={`field__label${isRequired ? " is-required" : ""}`}>{f}</div>
              <div className="field__input">
                {fieldName ? (
                  <>
                    <input
                      {...form.register(fieldName)}
                      placeholder={f}
                    />
                    {form.formState.errors[fieldName] && (
                      <span className="field__error">
                        {form.formState.errors[fieldName]?.message}
                      </span>
                    )}
                  </>
                ) : (
                  // form과 미연결 필드(Shipment Type, Service Term 등)는 기존 방식 유지
                  // TODO: 후속 작업 - 추가 필드 연결
                  <input defaultValue={defaults[f] ?? ""} placeholder={f} />
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Tabbar */}
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
        <div className="tabbar__spacer" />
        <div className="tabbar__meta">
          <span>Last saved: 10 min ago</span>
        </div>
      </div>

      {/* Tab content */}
      {tab === "main"    && renderMainTab(variant)}
      {tab === "edi"     && <EdiTab variant={variant} />}
      {tab === "other"   && <OtherTab />}
      {tab === "freight" && <FreightTab />}
    </form>
  );
}
