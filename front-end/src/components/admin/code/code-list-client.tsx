"use client";

import { useState } from "react";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { CodeMasterListGrid } from "./code-master-list-grid";
import { CodeDetailListGrid } from "./code-detail-list-grid";
import { CodeMasterEntryModal } from "./code-master-entry-modal";
import type { CodeMasterEntryModalState } from "./code-master-entry-modal";
import { codeMasterUseCases } from "@/application/code-master/use-cases";
import { codeDetailUseCases } from "@/application/code-detail/use-cases";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";

export function CodeListClient() {
  const qc = useQueryClient();
  const [selectedMasterId, setSelectedMasterId] = useState<number | null>(null);
  const [editModalState, setEditModalState] = useState<CodeMasterEntryModalState | null>(null);
  const [masterSelectedKeys, setMasterSelectedKeys] = useState<Set<number>>(new Set());
  const [detailSelectedKeys, setDetailSelectedKeys] = useState<Set<number>>(new Set());

  const masterBulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => codeMasterUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-master", "list"] });
      setMasterSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const detailBulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => codeDetailUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-detail", "list"] });
      setDetailSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  async function handleMasterBulkDelete() {
    const ok = await confirm({
      title: "선택 삭제",
      description: `선택한 ${masterSelectedKeys.size}개 항목을 삭제하시겠습니까?`,
      variant: "destructive",
    });
    if (ok) masterBulkDeleteMutation.mutate([...masterSelectedKeys]);
  }

  async function handleDetailBulkDelete() {
    const ok = await confirm({
      title: "선택 삭제",
      description: `선택한 ${detailSelectedKeys.size}개 항목을 삭제하시겠습니까?`,
      variant: "destructive",
    });
    if (ok) detailBulkDeleteMutation.mutate([...detailSelectedKeys]);
  }

  return (
    // ResizablePanel 미사용 — 프로젝트에 shadcn ui 컴포넌트 없음, 고정+가변 그리드 레이아웃 사용
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "400px 1fr",
        gap: 12,
        flex: 1,
        minHeight: 0,
        overflow: "hidden",
      }}
    >
      <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        <CodeMasterListGrid
          selectedId={selectedMasterId}
          onSelect={(id) => { setSelectedMasterId(id); setDetailSelectedKeys(new Set()); }}
          onRowDoubleClick={(id) => setEditModalState({ mode: "edit", id })}
          selectedKeys={masterSelectedKeys}
          onSelectionChange={setMasterSelectedKeys}
          onBulkDelete={handleMasterBulkDelete}
          isBulkDeletePending={masterBulkDeleteMutation.isPending}
        />
      </div>
      <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        {/* key로 master 변경 시 컴포넌트 remount → currentPage 등 내부 state 자연 초기화. detailSelectedKeys는 onSelect에서 명시 초기화. */}
        <CodeDetailListGrid
          key={selectedMasterId ?? "none"}
          masterId={selectedMasterId}
          selectedKeys={detailSelectedKeys}
          onSelectionChange={setDetailSelectedKeys}
          onBulkDelete={handleDetailBulkDelete}
          isBulkDeletePending={detailBulkDeleteMutation.isPending}
        />
      </div>

      <CodeMasterEntryModal
        state={editModalState}
        onClose={() => setEditModalState(null)}
        onSaved={() => setEditModalState(null)}
      />
    </div>
  );
}
