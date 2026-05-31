"use client";

import { Package, FilePlus, Search, Save, Trash2, SquarePen } from "lucide-react";
import { useTranslations } from "next-intl";
import { ActionButton } from "@/components/admin/access/action-button";

export function NonBlEntryHeader(props: {
  isEdit: boolean;
  isSavePending: boolean;
  isDeletePending: boolean;
  onNew: () => void;
  onSearch: () => void;
  onSave: () => void;
  onDelete: () => void;
  onChangeBlNo: () => void;
}) {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tt = useTranslations("fms.nonBl.entry.title");
  const ts = useTranslations("fms.nonBl.entry.status");
  const tc = useTranslations("common");

  const { isEdit, isSavePending, isDeletePending, onNew, onSearch, onSave, onDelete, onChangeBlNo } = props;

  return (
    <div className="page-head">
      <div className="page-head__title">
        <div className="page-head__title-icon"><Package size={14} /></div>
        {tt("main")}
      </div>
      <div className="page-head__meta">
        <span className={`badge ${isEdit ? "badge--saved" : "badge--draft"}`}>
          {isEdit ? ts("saved") : ts("draft")}
        </span>
      </div>
      <div className="page-head__actions">
        <ActionButton
          buttonCode="BTN_FMS_NON_BL_ENTRY_CREATE"
          className="btn btn--normal btn--sm"
          onClick={onNew}
          icon={<FilePlus size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_FMS_NON_BL_ENTRY_SEARCH_BL"
          className="btn btn--search btn--sm"
          onClick={onSearch}
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_FMS_NON_BL_ENTRY_UPDATE"
          className="btn btn--transaction btn--sm"
          disabled={isSavePending}
          onClick={onSave}
        >
          <Save size={12} style={{ marginRight: 4 }} />{isSavePending ? tc("saving") : tc("save")}
        </ActionButton>
        <ActionButton
          buttonCode="BTN_FMS_NON_BL_ENTRY_DELETE"
          className="btn btn--danger btn--sm"
          onClick={onDelete}
          disabled={!isEdit || isDeletePending}
          icon={<Trash2 size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_FMS_NON_BL_ENTRY_CHANGE_BL_NO"
          className="btn btn--modal btn--sm"
          onClick={onChangeBlNo}
          icon={<SquarePen size={12} style={{ marginRight: 4 }} />}
        />
      </div>
    </div>
  );
}
