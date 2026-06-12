"use client";

import { useRef, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Upload } from "lucide-react";
import { ModalShell } from "@/components/shared/modal-shell";
import { blAttachmentPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import type { AttachmentBlKind, BlAttachment } from "@/domain/attachment";
import { BlAttachmentRow } from "./bl-attachment-row";
interface BlAttachmentModalProps {
  blKind: AttachmentBlKind;
  blId: number;
  isOpen: boolean;
  onClose: () => void;
}

// ── Inner (항상 mount 상태 — ModalShell의 isOpen 가드로 보호됨) ──────────────
function BlAttachmentModalInner({ blKind, blId, onClose }: Omit<BlAttachmentModalProps, "isOpen">) {
  const t = useTranslations("fms.shared.attachment");
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [confirmingId, setConfirmingId] = useState<number | null>(null);

  const queryKey = ["bl-attachment", blKind, blId] as const;

  const { data: rows = [], isLoading } = useQuery({
    queryKey,
    queryFn: () => blAttachmentPort.list(blKind, blId),
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => blAttachmentPort.upload(blKind, blId, file),
    onSuccess: () => {
      toast.success(t("uploadSuccess"));
      // attachment 키만 무효화 — List 화면 자동 갱신 금지
      queryClient.invalidateQueries({ queryKey });
      // input value 리셋 (같은 파일 재선택 가능하도록)
      if (fileInputRef.current) fileInputRef.current.value = "";
    },
    onError: (err: Error) => {
      toast.error(err.message);
      if (fileInputRef.current) fileInputRef.current.value = "";
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => blAttachmentPort.remove(id),
    onSuccess: () => {
      toast.success(t("deleteSuccess"));
      setConfirmingId(null);
      queryClient.invalidateQueries({ queryKey });
    },
    onError: (err: Error) => {
      toast.error(err.message);
      setConfirmingId(null);
    },
  });

  // Authorization 헤더 필요 — <a href> 직링크 불가, blob URL 임시 생성 후 자동 click
  const [downloadingId, setDownloadingId] = useState<number | null>(null);
  async function handleDownload(row: BlAttachment) {
    setDownloadingId(row.id);
    try {
      const blob = await blAttachmentPort.download(row.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = row.originalFilename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : String(err));
    } finally {
      setDownloadingId(null);
    }
  }

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    uploadMutation.mutate(file);
  }

  return (
    <>
      <div className="modal__body" style={{ overflowY: "auto", minHeight: 360, maxHeight: "calc(60vh + 200px)" }}>
        {isLoading ? (
          <div style={{ padding: "16px 0", textAlign: "center", color: "var(--ink-3)" }}>
            Loading…
          </div>
        ) : rows.length === 0 ? (
          <div style={{ padding: "16px 0", textAlign: "center", color: "var(--ink-3)" }}>
            {t("empty")}
          </div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "var(--fs-sm)" }}>
            <thead>
              <tr style={{ borderBottom: "1px solid var(--border)" }}>
                <th style={{ textAlign: "left", padding: "4px 8px", color: "var(--ink-3)", fontWeight: 600 }}>{t("colFilename")}</th>
                <th style={{ textAlign: "left", padding: "4px 8px", color: "var(--ink-3)", fontWeight: 600 }}>{t("colSize")}</th>
                <th style={{ textAlign: "left", padding: "4px 8px", color: "var(--ink-3)", fontWeight: 600 }}>{t("colUploadedBy")}</th>
                <th style={{ textAlign: "left", padding: "4px 8px", color: "var(--ink-3)", fontWeight: 600 }}>{t("colUploadedAt")}</th>
                <th style={{ padding: "4px 8px" }}></th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <BlAttachmentRow
                  key={row.id}
                  row={row}
                  confirmingId={confirmingId}
                  isDeletePending={deleteMutation.isPending}
                  isDownloadPending={downloadingId === row.id}
                  onDownload={handleDownload}
                  onDeleteRequest={(id) => setConfirmingId(id)}
                  onDeleteConfirm={(id) => deleteMutation.mutate(id)}
                  onDeleteCancel={() => setConfirmingId(null)}
                />
              ))}
            </tbody>
          </table>
        )}
      </div>
      <div className="modal__actions">
        {/* 숨김 input — Upload 버튼 click()으로 트리거 */}
        <input
          ref={fileInputRef}
          type="file"
          style={{ display: "none" }}
          onChange={handleFileChange}
        />
        <button
          type="button"
          className="btn btn--primary btn--sm"
          disabled={uploadMutation.isPending}
          onClick={() => fileInputRef.current?.click()}
        >
          <Upload size={12} style={{ marginRight: 4 }} />
          {uploadMutation.isPending ? t("uploading") : t("uploadBtn")}
        </button>
        <button type="button" className="btn btn--sm" onClick={onClose}>
          Close
        </button>
      </div>
    </>
  );
}

// ── Modal 본체 (outer — isOpen 가드, mount 시 offset 0,0 reset 보장) ──────────
export function BlAttachmentModal({ isOpen, ...props }: BlAttachmentModalProps) {
  const t = useTranslations("fms.shared.attachment");
  return (
    <ModalShell isOpen={isOpen} title={t("modalTitle")} size="md" style={{ width: 710, maxWidth: 710 }}>
      <BlAttachmentModalInner {...props} />
    </ModalShell>
  );
}
