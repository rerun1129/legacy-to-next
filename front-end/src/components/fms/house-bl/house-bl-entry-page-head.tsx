"use client";

import { Save, Printer, Trash2, FileText, SquarePen, Search, FilePlus } from "lucide-react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { UseMutationResult } from "@tanstack/react-query";
import { Button } from "@/components/shared/button";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { getPageTitle } from "@/lib/bl-variants";
import type { BLVariantConfig } from "@/lib/bl-variants";
import type { HouseBlFormValues } from "./house-bl-schema";
import {
  TOOLBAR_LABEL_TO_FIELD,
  REQUIRED_TOOLBAR_LABELS,
} from "./house-bl-schema";

// variant별 기본값(fieldName 없는 placeholder-only 필드용)
const DEFAULTS_SEA: Record<string, string> = {
  "Shipment Type": "", "HBL No": "", "MBL No": "", "Load Type": "",
  "Service Term": "", "B/L Type": "", "Master Ref": "",
};
const DEFAULTS_AIR: Record<string, string> = {
  "Shipment Type": "", "HAWB No": "", "MAWB No": "", "Master Ref": "",
};
const DEFAULTS_TRUCK: Record<string, string> = { "Truck B/L No": "" };
const DEFAULTS_NON_BL: Record<string, string> = { "Non B/L No": "" };

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
}

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
}: HouseBlEntryPageHeadProps) {
  const isExp = variant.direction === "EXP";
  const defaults = getToolbarDefaults(variant);

  const { options: loadTypeOptions,     placeholder: loadTypePh }     = useEnumOptions("LoadType");
  const { options: serviceTermOptions,  placeholder: serviceTermPh }  = useEnumOptions("ServiceTerm");
  const { options: blTypeOptions,       placeholder: blTypePh }       = useEnumOptions("BlType");
  const { options: shipmentTypeOptions, placeholder: shipmentTypePh } = useEnumOptions("ShipmentType");

  // LoadType / ServiceTerm / BlType / ShipmentType 은 모두 e.name() 기반 등록이므로 §6.45 재매핑 불필요
  const TOOLBAR_ENUM: Record<string, { options: typeof loadTypeOptions; placeholder: string | undefined }> = {
    "Load Type":     { options: loadTypeOptions,     placeholder: loadTypePh },
    "Service Term":  { options: serviceTermOptions,  placeholder: serviceTermPh },
    "B/L Type":      { options: blTypeOptions,       placeholder: blTypePh },
    "Shipment Type": { options: shipmentTypeOptions, placeholder: shipmentTypePh },
  };

  return (
    <>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><FileText size={14} /></div>
          {getPageTitle(variant, "House", "Entry")}
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <Button size="sm" variant="normal" leftIcon={<FilePlus size={12} />} onClick={onResetEntry}>New</Button>
          <Button size="sm" variant="search" leftIcon={<Search size={12} />} onClick={onSearchBl}>Search B/L</Button>
          <Button
            type="submit"
            size="sm"
            variant="transaction"
            leftIcon={<Save size={12} />}
            loading={mutation.isPending}
          >{mutation.isPending ? "Saving..." : "Save"}</Button>
          <Button
            size="sm"
            variant="danger"
            leftIcon={<Trash2 size={12} />}
            onClick={onDelete}
            disabled={!isEdit || deleteMutation.isPending}
          >Delete</Button>
          {isExp && variant.printDocs.length > 0 && (
            <Button size="sm" variant="normal" leftIcon={<Printer size={12} />}>Print</Button>
          )}
          {isExp && (
            <Button
              size="sm"
              variant="modal"
              leftIcon={<SquarePen size={12} />}
              disabled={!canSwitchBl}
              onClick={onOpenSwitchBl}
            >Switch B/L</Button>
          )}
          <Button
            size="sm"
            variant="modal"
            leftIcon={<SquarePen size={12} />}
            onClick={onChangeBlNo}
            disabled={!isEdit}
          >Change B/L No.</Button>
        </div>
      </div>

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
                        placeholder={f}
                      />
                    )}
                    {(form.formState.errors as Record<string, unknown>)[fieldName] && (
                      <span className="field__error">
                        {(form.formState.errors as Record<string, { message?: string }>)[fieldName]?.message}
                      </span>
                    )}
                  </>
                ) : (
                  <input defaultValue={defaults[f] ?? ""} placeholder={f} />
                )}
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
