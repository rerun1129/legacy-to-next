"use client";

import { Package, FilePlus, Search, Save, Trash2, Copy, RefreshCw } from "lucide-react";

export function NonBlEntryHeader(props: {
  isEdit: boolean;
  isSavePending: boolean;
  isDeletePending: boolean;
  onNew: () => void;
  onSearch: () => void;
  onSave: () => void;
  onDelete: () => void;
  onChangeBlNo: () => void;
}): JSX.Element {
  const { isEdit, isSavePending, isDeletePending, onNew, onSearch, onSave, onDelete, onChangeBlNo } = props;

  return (
    <div className="page-head">
      <div className="page-head__title">
        <div className="page-head__title-icon"><Package size={14} /></div>
        Non B/L Entry
      </div>
      <div className="page-head__meta">
        <span className={`badge ${isEdit ? "badge--saved" : "badge--draft"}`}>
          {isEdit ? "SAVED" : "DRAFT"}
        </span>
      </div>
      <div className="page-head__actions">
        <button type="button" className="btn btn--sm" onClick={onNew}>
          <FilePlus size={12} />New
        </button>
        <button type="button" className="btn btn--sm btn--search" onClick={onSearch}>
          <Search size={12} />Search
        </button>
        <button
          type="button"
          className="btn btn--sm btn--transaction"
          disabled={isSavePending}
          onClick={onSave}
        >
          <Save size={12} />{isSavePending ? "Saving..." : "Save"}
        </button>
        <button
          type="button"
          className="btn btn--sm btn--danger"
          onClick={onDelete}
          disabled={!isEdit || isDeletePending}
        >
          <Trash2 size={12} />Delete
        </button>
        <button
          type="button"
          className="btn btn--sm btn--transaction"
        >
          <Copy size={12} />Copy
        </button>
        <button
          type="button"
          className="btn btn--sm btn--transaction"
          onClick={onChangeBlNo}
        >
          <RefreshCw size={12} />Change B/L No
        </button>
      </div>
    </div>
  );
}
