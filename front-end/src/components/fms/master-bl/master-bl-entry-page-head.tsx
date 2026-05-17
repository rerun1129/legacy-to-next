"use client";

import { Save, Trash2, Layers, SquarePen, Search, FilePlus } from "lucide-react";
import type { UseMutationResult } from "@tanstack/react-query";
import { Button } from "@/components/shared/button";
import { getPageTitle } from "@/lib/bl-variants";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { MasterBlFormValues } from "./master-bl-schema";
import type { ModeLabels } from "@/lib/bl-mode-labels";

interface MasterBlEntryPageHeadProps {
  variant: MasterVariantConfig;
  mutation: UseMutationResult<{ id: number } | void, Error, MasterBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  isEdit: boolean;
  modeLabels: ModeLabels;
  onResetEntry: () => void;
  onSearchBl: () => void;
  onDelete: () => Promise<void>;
  onChangeBlNo: () => void;
}

// NOTE: No Print button per PRD §S-04
export function MasterBlEntryPageHead({
  variant,
  mutation,
  deleteMutation,
  isEdit,
  modeLabels,
  onResetEntry,
  onSearchBl,
  onDelete,
  onChangeBlNo,
}: MasterBlEntryPageHeadProps) {
  return (
    <div className="page-head">
      <div className="page-head__title">
        <div className="page-head__title-icon"><Layers size={14} /></div>
        {getPageTitle(variant, "Master", "Entry")}
      </div>
      <div className="page-head__meta">
        <span className="badge badge--draft">DRAFT</span>
      </div>
      <div className="page-head__actions">
        <Button size="sm" variant="normal" leftIcon={<FilePlus size={12} />} onClick={onResetEntry}>New</Button>
        <Button size="sm" variant="search" leftIcon={<Search size={12} />} onClick={onSearchBl}>Search</Button>
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
        <Button size="sm" variant="modal" leftIcon={<SquarePen size={12} />} onClick={onChangeBlNo} disabled={!isEdit}>{modeLabels.changeBLNo}</Button>
      </div>
    </div>
  );
}
