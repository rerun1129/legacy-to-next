"use client";

import { Save, Printer, Trash2, FileText, SquarePen, Search, FilePlus, ExternalLink, Paperclip } from "lucide-react";
import { Controller, useWatch } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useTranslations } from "next-intl";
import { ActionButton } from "@/components/admin/access/action-button";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { openMasterBlEntry } from "@/lib/open-bl-entry";
import type { HouseBlFormValues } from "./house-bl-schema";
import {
  TOOLBAR_FIELD_TO_RHF,
  REQUIRED_TOOLBAR_FIELDS,
} from "./house-bl-schema";

// variant별 기본값(RHF 바인딩 없는 placeholder-only 필드용) — fieldId 키로 관리
const DEFAULTS_SEA: Record<string, string> = {
  shipmentType: "", hblNo: "", mblNo: "", loadType: "",
  serviceTerm: "", blType: "", masterRef: "",
};
const DEFAULTS_AIR: Record<string, string> = {
  shipmentType: "", hawbNo: "", mawbNo: "", masterRef: "",
};
const DEFAULTS_TRUCK: Record<string, string> = { truckBlNo: "" };
const DEFAULTS_NON_BL: Record<string, string> = { nonBlNo: "" };

// variant → title i18n 키
function getVariantTitleKey(variant: BLVariantConfig): string {
  if (variant.mode === "TRUCK")  return "truck";
  if (variant.mode === "NON_BL") return "nonBl";
  const modeKey = variant.mode === "SEA" ? "sea" : "air";
  const dirKey  = variant.direction === "EXP" ? "Exp" : "Imp";
  return `${modeKey}${dirKey}`;
}

function getToolbarDefaults(variant: BLVariantConfig): Record<string, string> {
  if (variant.mode === "SEA")   return DEFAULTS_SEA;
  if (variant.mode === "AIR")   return DEFAULTS_AIR;
  if (variant.mode === "TRUCK") return DEFAULTS_TRUCK;
  return DEFAULTS_NON_BL;
}

interface HouseBlEntryPageHeadProps {
  variant: BLVariantConfig;
  form: UseFormReturn<HouseBlFormValues>;
  toolbarFields: ReadonlyArray<string>;
  mutation: UseMutationResult<unknown, Error, HouseBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  isEdit: boolean;
  canSwitchBl: boolean;
  onResetEntry: () => void;
  onSearchBl: () => void;
  onDelete: () => Promise<void>;
  onChangeBlNo: () => void;
  onOpenSwitchBl: () => void;
  onOpenAttachments?: () => void;
}

const VARIANT_TO_MENU: Record<string, string> = {
  "sea-exp": "FMS_HOUSE_BL_SEA_EXP_ENTRY",
  "sea-imp": "FMS_HOUSE_BL_SEA_IMP_ENTRY",
  "air-exp": "FMS_HOUSE_BL_AIR_EXP_ENTRY",
  "air-imp": "FMS_HOUSE_BL_AIR_IMP_ENTRY",
  "truck":   "FMS_TRUCK_BL_ENTRY",
  "non-bl":  "FMS_NON_BL_ENTRY",
};

export function HouseBlEntryPageHead({
  variant,
  form,
  toolbarFields,
  mutation,
  deleteMutation,
  isEdit,
  canSwitchBl,
  onResetEntry,
  onSearchBl,
  onDelete,
  onChangeBlNo,
  onOpenSwitchBl,
  onOpenAttachments,
}: HouseBlEntryPageHeadProps) {
  const router = useRouter();
  const isExp = variant.direction === "EXP";
  const defaults = getToolbarDefaults(variant);
  const menuCode = VARIANT_TO_MENU[variant.key] ?? "FMS_HOUSE_BL_SEA_EXP_ENTRY";

  const tb = useTranslations("fms.houseBl.entry.toolbar");
  const tt = useTranslations("fms.houseBl.entry.title");
  const ts = useTranslations("fms.houseBl.entry.status");
  const tc = useTranslations("common");
  const tq = useTranslations("shell.quickSearch");

  // masterBlId만 구독 — form.watch() 전체 구독 시 형제 입력 focus 유실 회피
  const masterBlId = useWatch({ control: form.control, name: "masterBlId" });

  const { options: loadTypeOptions,     placeholder: loadTypePh }     = useEnumOptions("LoadType");
  const { options: serviceTermOptions,  placeholder: serviceTermPh }  = useEnumOptions("ServiceTerm");
  const { options: blTypeOptions,       placeholder: blTypePh }       = useEnumOptions("BlType");
  const { options: shipmentTypeOptions, placeholder: shipmentTypePh } = useEnumOptions("ShipmentType");

  // LoadType / ServiceTerm / BlType / ShipmentType 은 모두 e.name() 기반 등록이므로 §6.45 재매핑 불필요
  // TOOLBAR_ENUM 키는 fieldId 기준
  const TOOLBAR_ENUM: Record<string, { options: typeof loadTypeOptions; placeholder: string | undefined }> = {
    loadType:    { options: loadTypeOptions,     placeholder: loadTypePh },
    serviceTerm: { options: serviceTermOptions,  placeholder: serviceTermPh },
    blType:      { options: blTypeOptions,       placeholder: blTypePh },
    shipmentType:{ options: shipmentTypeOptions, placeholder: shipmentTypePh },
  };

  return (
    <>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><FileText size={14} /></div>
          {tt(getVariantTitleKey(variant))}
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">{ts("draft")}</span>
        </div>
        <div className="page-head__actions">
          <ActionButton
            buttonCode={`BTN_${menuCode}_CREATE`}
            className="btn btn--normal btn--sm"
            onClick={onResetEntry}
            icon={<FilePlus size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode={`BTN_${menuCode}_SEARCH_BL`}
            className="btn btn--search btn--sm"
            onClick={onSearchBl}
            icon={<Search size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode={`BTN_${menuCode}_UPDATE`}
            className="btn btn--transaction btn--sm"
            type="submit"
            disabled={mutation.isPending}
          >
            <Save size={12} style={{ marginRight: 4 }} />{mutation.isPending ? tc("saving") : tc("save")}
          </ActionButton>
          <ActionButton
            buttonCode={`BTN_${menuCode}_DELETE`}
            className="btn btn--danger btn--sm"
            onClick={onDelete}
            disabled={!isEdit || deleteMutation.isPending}
            icon={<Trash2 size={12} style={{ marginRight: 4 }} />}
          />
          {isExp && variant.printDocs.length > 0 && (
            <ActionButton
              buttonCode={`BTN_${menuCode}_PRINT`}
              className="btn btn--normal btn--sm"
              icon={<Printer size={12} style={{ marginRight: 4 }} />}
            />
          )}
          {isExp && (
            <ActionButton
              buttonCode={`BTN_${menuCode}_SWITCH_BL`}
              className="btn btn--modal btn--sm"
              disabled={!canSwitchBl}
              onClick={onOpenSwitchBl}
              icon={<SquarePen size={12} style={{ marginRight: 4 }} />}
            />
          )}
          <ActionButton
            buttonCode={`BTN_${menuCode}_CHANGE_BL_NO`}
            className="btn btn--modal btn--sm"
            onClick={onChangeBlNo}
            disabled={!isEdit}
            icon={<SquarePen size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode={`BTN_${menuCode}_ATTACH`}
            className="btn btn--modal btn--sm"
            onClick={onOpenAttachments}
            disabled={!isEdit}
            icon={<Paperclip size={12} style={{ marginRight: 4 }} />}
          />
        </div>
      </div>

      <div className="toolbar">
        {toolbarFields.map((f) => {
          const fieldName = TOOLBAR_FIELD_TO_RHF[f];
          const isRequired = REQUIRED_TOOLBAR_FIELDS.has(f);
          const label = tb(f);
          return (
            <div key={f} className={`field${isRequired ? " is-required" : ""}`}>
              <div className={`field__label${isRequired ? " is-required" : ""}`}>{label}</div>
              <div className="field__input">
                {fieldName ? (
                  <>
                    {TOOLBAR_ENUM[f] ? (
                      <Controller
                        name={fieldName as keyof HouseBlFormValues}
                        control={form.control}
                        render={({ field }) => (
                          <ComboBox
                            options={TOOLBAR_ENUM[f].options}
                            placeholder={TOOLBAR_ENUM[f].placeholder}
                            value={(field.value as string) ?? ""}
                            onChange={field.onChange}
                          />
                        )}
                      />
                    ) : (
                      <input
                        {...(form.register as (n: string) => object)(fieldName)}
                        placeholder={label}
                      />
                    )}
                    {fieldName === "mbl" && String(masterBlId ?? "").trim() !== "" && (
                      <button
                        type="button"
                        className="btn--icon-sm"
                        title={tb("goToMaster")}
                        aria-label={tb("goToMaster")}
                        onClick={() => {
                          if (!variant.direction) return;
                          openMasterBlEntry(
                            { masterBlId: Number(masterBlId), jobDiv: variant.mode, bound: variant.direction },
                            router,
                            tq("noAccess"),
                          );
                        }}
                      >
                        <ExternalLink size={12} />
                      </button>
                    )}
                    {(form.formState.errors as Record<string, unknown>)[fieldName] && (
                      <span className="field__error">
                        {(form.formState.errors as Record<string, { message?: string }>)[fieldName]?.message}
                      </span>
                    )}
                  </>
                ) : (
                  <input defaultValue={defaults[f] ?? ""} placeholder={label} />
                )}
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
