"use client";

import { useState, useEffect, useRef } from "react";
import { useForm, FormProvider } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { Save, Printer, Copy, Trash2, FileText, Send, Download, RefreshCw, RotateCcw, Search } from "lucide-react";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { useDraftPersist } from "@/lib/use-draft-persist";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { getPageTitle } from "@/lib/bl-variants";
import { MainTabSea }  from "./tabs/main-sea";
import { MainTabAir }  from "./tabs/main-air";
import { EdiTab }      from "./tabs/edi-tab";
import { OtherTab }    from "./tabs/other-tab";
import { FreightTab }  from "./tabs/freight-tab";
import { houseBlPort } from "@/lib/ports";
import type { HouseBlFormValues } from "./house-bl-schema";
import { buildHouseBlRequest } from "./house-bl-submit";
import { SwitchBlModal } from "@/components/fms/switch-bl/switch-bl-modal";
import { useSearchBl } from "./use-search-bl";

// @hookform/resolvers 미설치로 zodResolver 없이 useForm 단독 사용. 검증은 submit 시 수동 호출.

const TOOLBAR_FIELDS_SEA = [
  "Shipment Type", "Settle", "HBL No", "MBL No", "Load Type", "Service Term", "B/L Type", "Master Ref",
] as const;
const TOOLBAR_FIELDS_AIR = [
  "Shipment Type", "Settle", "HAWB No", "MAWB No", "Rate Class", "Service Term", "B/L Type", "Master Ref",
] as const;
const TOOLBAR_FIELDS_TRUCK = [
  "Truck B/L No", "Settle",
] as const;
const TOOLBAR_FIELDS_NON_BL = [
  "Non B/L No", "Settle",
] as const;

const DEFAULTS_SEA: Record<string, string> = {
  "Shipment Type": "", "Settle": "", "HBL No": "",
  "MBL No": "", "Load Type": "", "Service Term": "",
  "B/L Type": "", "Master Ref": "",
};
const DEFAULTS_AIR: Record<string, string> = {
  "Shipment Type": "", "Settle": "", "HAWB No": "",
  "MAWB No": "", "Rate Class": "", "Service Term": "",
  "B/L Type": "", "Master Ref": "",
};
const DEFAULTS_TRUCK: Record<string, string> = {
  "Truck B/L No": "", "Settle": "PREPAID",
};
const DEFAULTS_NON_BL: Record<string, string> = {
  "Non B/L No": "", "Settle": "PREPAID",
};

function getToolbarFields(variant: BLVariantConfig) {
  if (variant.mode === "SEA")    return TOOLBAR_FIELDS_SEA;
  if (variant.mode === "AIR")    return TOOLBAR_FIELDS_AIR;
  if (variant.mode === "TRUCK")  return TOOLBAR_FIELDS_TRUCK;
  return TOOLBAR_FIELDS_NON_BL;
}

function getToolbarDefaults(variant: BLVariantConfig) {
  if (variant.mode === "SEA")    return DEFAULTS_SEA;
  if (variant.mode === "AIR")    return DEFAULTS_AIR;
  if (variant.mode === "TRUCK")  return DEFAULTS_TRUCK;
  return DEFAULTS_NON_BL;
}

function renderMainTab(variant: BLVariantConfig) {
  if (variant.mode === "SEA")  return <MainTabSea variant={variant} />;
  if (variant.mode === "AIR")  return <MainTabAir variant={variant} />;
  // TRUCK/NON_BL: TruckOrderPanel은 MainTabSea 안의 분기로 처리 예정
  return <MainTabSea variant={variant} />;
}

/** toolbar의 라벨명 → form field 이름 매핑 */
const TOOLBAR_LABEL_TO_FIELD: Record<string, keyof HouseBlFormValues> = {
  "HBL No":         "hbl",
  "HAWB No":        "hbl",
  "Truck B/L No":   "hbl",
  "Non B/L No":     "hbl",
  "MBL No":         "mbl",
  "MAWB No":        "mbl",
  "Load Type":      "lType",
  "Settle":         "settle",
};

const REQUIRED_TOOLBAR_LABELS = new Set(["HBL No", "HAWB No", "Truck B/L No", "Non B/L No", "Shipment Type", "Settle"]);

interface Props {
  variant: BLVariantConfig;
  id?: number;
}

export function HouseBLEntry({ variant, id }: Props) {
  const [tab, setTab] = useState("main");
  const [isSwitchBlModalOpen, setIsSwitchBlModalOpen] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);
  const { setCanEdit } = useWidgetLayout();
  const isEdit = Boolean(id);
  const queryClient = useQueryClient();
  const router = useRouter();
  const variantKey = variant.key;

  const defaults = getToolbarDefaults(variant);

  const form = useForm<HouseBlFormValues>({
    defaultValues: {
      hbl:    defaults["HBL No"] ?? defaults["HAWB No"] ?? defaults["Truck B/L No"] ?? defaults["Non B/L No"] ?? "",
      mbl:    defaults["MBL No"] ?? defaults["MAWB No"] ?? "",
      sType:  defaults["Shipment Type"] ?? "",
      lType:  defaults["Load Type"] ?? "",
      etd:    "",
      eta:    "",
      pol:    "",
      pod:    "",
      settle: "PREPAID",
      expImp: variant.direction,
      // Party
      shipperCode: "", shipperName: "", shipperAddr: "",
      consigneeCode: "", consigneeName: "", consigneeAddr: "",
      notifyCode: "", notifyName: "", notifyAddr: "",
      // Trade
      paymentType: "", paymentPlace: "",
      // Schedule (SEA)
      linerCode: "", linerName: "",
      vesselCode: "", vesselName: "", voyNo: "", onboardDate: "",
      // Air Schedule
      airlineCode: "", airlineName: "", flightNo: "", flightDate: "",
      // Marks & Description
      marksAndNumbers: "", descriptionOfGoods: "", natureOfGoods: "",
      // Freight / sub-entity rows
      itemHs:         [],
      freightSelling: [],
      freightBuying:  [],
      coLoadBls:      [],
      koreaLicenses:  [],
    },
  });

  // 수정 모드: 기존 데이터 fetch
  const { data: detail } = useQuery({
    queryKey: ["house-bl", "detail", id],
    queryFn: () => houseBlPort.getById(id!),
    enabled: isEdit,
  });

  const { clearDraft, hasDraft } = useDraftPersist(form, `draft:house-bl:${variant.key}:${id ?? "new"}`);

  // 수정 모드: draft가 없을 때만 서버 데이터로 reset (draft 우선)
  useEffect(() => {
    if (!detail || hasDraft()) return;
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
  }, [detail, form, hasDraft]);

  const mutation = useMutation({
    mutationFn: (data: HouseBlFormValues) => {
      const req = buildHouseBlRequest(data, variant);
      return isEdit ? houseBlPort.update(id!, req) : houseBlPort.create(req);
    },
    onSuccess: () => {
      clearDraft();
      queryClient.invalidateQueries({ queryKey: ["house-bl", "list"] });
      router.push(`/fms/house-bl/${variant.key}/list`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => houseBlPort.delete(id!),
    onSuccess: () => {
      clearDraft();
      queryClient.invalidateQueries({ queryKey: ['house-bl', 'list'] });
      form.reset();
      router.replace(`/fms/house-bl/${variant.key}/entry`);
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

  const { handleSearchBl } = useSearchBl(form, variant);

  function handleResetEntry() {
    form.reset();
    clearDraft();
    formRef.current?.reset();
  }

  function handleSubmit(raw: HouseBlFormValues) {
    mutation.mutate(raw);
  }

  // Switch B/L 버튼 활성 조건: 수정 모드이고 SEA variant
  const canSwitchBl = isEdit && id != null && variant.key.startsWith('sea-');

  return (
    <>
      <FormProvider {...form}>
      <form ref={formRef} onSubmit={form.handleSubmit(handleSubmit)}>
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
            <button type="button" className="btn btn--sm"><Download size={12} />Export</button>
            {variant.printDocs.length > 0 && (
              <button type="button" className="btn btn--sm btn--success"><Printer size={12} />Print</button>
            )}
            <button type="button" className="btn btn--sm btn--info"><Send size={12} />EDI</button>
            {/* Switch B/L: SEA 수정 모드에서만 활성 */}
            <button
              type="button"
              className="btn btn--sm btn--secondary"
              disabled={!canSwitchBl}
              onClick={() => setIsSwitchBlModalOpen(true)}
            >
              <RefreshCw size={12} />Switch B/L
            </button>
            <button type="button" className="btn btn--sm" onClick={handleResetEntry}>
              <RotateCcw size={12} />Reset
            </button>
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
                    // form과 미연결 필드(Shipment Type, Service Term 등)
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

        {/* Tab content — 항상 마운트, 비활성 탭은 hidden으로 숨겨 폼 상태 보존 */}
        <div style={{ display: tab === "main"    ? "contents" : "none" }}>{renderMainTab(variant)}</div>
        <div style={{ display: tab === "edi"     ? "contents" : "none" }}><EdiTab variant={variant} /></div>
        <div style={{ display: tab === "other"   ? "contents" : "none" }}><OtherTab /></div>
        <div style={{ display: tab === "freight" ? "contents" : "none" }}><FreightTab /></div>
      </form>
      </FormProvider>

      {/* Switch B/L 모달: canSwitchBl이 true일 때만 렌더 (id는 활성 조건에서 보장) */}
      {canSwitchBl && (
        <SwitchBlModal
          houseBlId={id!}
          isOpen={isSwitchBlModalOpen}
          onClose={() => setIsSwitchBlModalOpen(false)}
        />
      )}
    </>
  );
}
