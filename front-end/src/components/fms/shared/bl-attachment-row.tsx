"use client";

import { Trash2, Download } from "lucide-react";
import { useTranslations } from "next-intl";
import type { BlAttachment } from "@/domain/attachment";

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

// ISO-8601 datetime을 "YYYY-MM-DD HH:mm" 형식으로 표시
function formatDatetime(iso: string): string {
  if (!iso) return "";
  const t = iso.replace("T", " ");
  // "YYYY-MM-DD HH:mm:ss.xxx" 에서 앞 16자만 취함
  return t.slice(0, 16);
}

interface BlAttachmentRowProps {
  row: BlAttachment;
  confirmingId: number | null;
  isDeletePending: boolean;
  isDownloadPending: boolean;
  onDownload: (row: BlAttachment) => void;
  onDeleteRequest: (id: number) => void;
  onDeleteConfirm: (id: number) => void;
  onDeleteCancel: () => void;
}

export function BlAttachmentRow({
  row,
  confirmingId,
  isDeletePending,
  isDownloadPending,
  onDownload,
  onDeleteRequest,
  onDeleteConfirm,
  onDeleteCancel,
}: BlAttachmentRowProps) {
  const t = useTranslations("fms.shared.attachment");
  const isConfirming = confirmingId === row.id;

  return (
    <tr>
      <td style={{ color: "var(--ink)" }}>{row.originalFilename}</td>
      <td style={{ color: "var(--ink-3)", whiteSpace: "nowrap" }}>{formatFileSize(row.fileSize)}</td>
      <td style={{ color: "var(--ink-3)" }}>{row.uploadedBy}</td>
      <td style={{ color: "var(--ink-3)", whiteSpace: "nowrap" }}>{formatDatetime(row.createdAt)}</td>
      <td style={{ whiteSpace: "nowrap" }}>
        {isConfirming ? (
          <span style={{ display: "inline-flex", gap: 4, alignItems: "center" }}>
            <button
              type="button"
              className="btn btn--danger btn--xs"
              disabled={isDeletePending}
              onClick={() => onDeleteConfirm(row.id)}
            >
              {t("confirmDelete")}
            </button>
            <button
              type="button"
              className="btn btn--xs"
              onClick={onDeleteCancel}
            >
              {t("cancelDelete")}
            </button>
          </span>
        ) : (
          <span style={{ display: "inline-flex", gap: 4, alignItems: "center" }}>
            <button
              type="button"
              className="btn btn--xs"
              disabled={isDownloadPending}
              onClick={() => onDownload(row)}
              title={t("downloadBtn")}
            >
              <Download size={11} />
            </button>
            <button
              type="button"
              className="btn btn--danger btn--xs"
              onClick={() => onDeleteRequest(row.id)}
              title={t("deleteBtn")}
            >
              <Trash2 size={11} />
            </button>
          </span>
        )}
      </td>
    </tr>
  );
}
