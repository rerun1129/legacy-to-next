"use client";

import { Save, Trash2, Layers, SquarePen, Search, FilePlus } from "lucide-react";
import type { UseMutationResult } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { ActionButton } from "@/components/admin/access/action-button";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { MasterBlFormValues } from "./master-bl-schema";

interface MasterBlEntryPageHeadProps {
  variant: MasterVariantConfig;
  mutation: UseMutationResult<{ id: number } | void, Error, MasterBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  isEdit: boolean;
  onResetEntry: () => void;
  onSearchBl: () => void;
  onDelete: () => Promise<void>;
  onChangeBlNo: () => void;
}

const VARIANT_TO_MENU: Record<string, string> = {
  "sea-exp": "FMS_MASTER_BL_SEA_EXP_ENTRY",
  "sea-imp": "FMS_MASTER_BL_SEA_IMP_ENTRY",
  "air-exp": "FMS_MASTER_BL_AIR_EXP_ENTRY",
  "air-imp": "FMS_MASTER_BL_AIR_IMP_ENTRY",
};

// variant → title 카탈로그 키 (mode+direction 조합 → seaExp|seaImp|airExp|airImp)
function variantToTitleKey(variant: MasterVariantConfig): "seaExp" | "seaImp" | "airExp" | "airImp" {
  if (variant.mode === "SEA" && variant.direction === "EXP") return "seaExp";
  if (variant.mode === "SEA" && variant.direction === "IMP") return "seaImp";
  if (variant.mode === "AIR" && variant.direction === "EXP") return "airExp";
  return "airImp";
}

// NOTE: No Print button per PRD §S-04
export function MasterBlEntryPageHead({
  variant,
  mutation,
  deleteMutation,
  isEdit,
  onResetEntry,
  onSearchBl,
  onDelete,
  onChangeBlNo,
}: MasterBlEntryPageHeadProps) {
  const tt = useTranslations("fms.masterBl.entry.title");
  const ts = useTranslations("fms.masterBl.entry.status");
  const tc = useTranslations("common");

  const menuCode = VARIANT_TO_MENU[variant.key] ?? "FMS_MASTER_BL_SEA_EXP_ENTRY";
  return (
    <div className="page-head">
      <div className="page-head__title">
        <div className="page-head__title-icon"><Layers size={14} /></div>
        {tt(variantToTitleKey(variant))}
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
        <ActionButton
          buttonCode={`BTN_${menuCode}_CHANGE_BL_NO`}
          className="btn btn--modal btn--sm"
          onClick={onChangeBlNo}
          disabled={!isEdit}
          icon={<SquarePen size={12} style={{ marginRight: 4 }} />}
        />
      </div>
    </div>
  );
}
