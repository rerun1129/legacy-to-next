"use client";

import { useState, useMemo, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessUserPermissionPresetPort, permissionPresetPort } from "@/lib/ports";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { MultiSelectBox } from "@/components/shared/inputs/multi-select-box";
import type { MultiSelectBoxOption } from "@/components/shared/inputs/multi-select-box";
import type { UserPermissionPresetRef } from "@/domain/access/user-permission-preset";

interface Props {
  userId: number;
  username?: string;
}

export function UserPermissionPresetsSection({ userId, username }: Props) {
  const t = useTranslations("admin.user.section");
  const qc = useQueryClient();
  const [assignedSelected, setAssignedSelected] = useState<Set<number>>(new Set());
  const [addCandidates, setAddCandidates] = useState<string[]>([]);

  const assignedColumns: GridColumn<UserPermissionPresetRef>[] = useMemo(() => [
    { key: "presetCode", label: t("colCode"), minWidth: 140 },
    { key: "presetName", label: t("colName"), minWidth: 160 },
    {
      key: "presetActive",
      label: t("colActive"),
      minWidth: 70,
      align: "center",
      render: (v) => (v ? t("statusActive") : t("statusInactive")),
    },
  ], [t]);

  // Query 1: 해당 user 에 부여된 preset 목록
  const { data: assigned = [], isFetching: assignedFetching } = useQuery({
    queryKey: ["access-user-permission-preset", "by-user", userId],
    queryFn: () => accessUserPermissionPresetPort.listByUser(userId),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  // Query 2: 전체 활성 preset 풀 (후보 선택용)
  const { data: allPresets = [], isFetching: poolFetching } = useQuery({
    queryKey: ["access-permission-preset", "list"],
    queryFn: () => permissionPresetPort.search({ activeOnly: true }),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  // 이미 부여된 presetId Set — 풀에서 제외
  const assignedPresetIdSet = useMemo(
    () => new Set(assigned.map((r) => r.presetId)),
    [assigned],
  );

  // MultiSelectBox 옵션: 아직 부여되지 않은 활성 preset만
  const poolOptions = useMemo<MultiSelectBoxOption[]>(
    () =>
      allPresets
        .filter((p) => !assignedPresetIdSet.has(p.id))
        .map((p) => ({ value: String(p.id), label: `[${p.code}] ${p.name}` })),
    [allPresets, assignedPresetIdSet],
  );

  const assignMutation = useMutation({
    mutationFn: async (presetIds: number[]) => {
      // 여러 preset을 순차적으로 부여 (순서 보장 + rate 고려)
      for (const presetId of presetIds) {
        await accessUserPermissionPresetPort.assign(userId, presetId);
      }
    },
    onSuccess: () => {
      toast.success(t("assignSuccess"));
      qc.invalidateQueries({ queryKey: ["access-user-permission-preset", "by-user", userId] });
      setAddCandidates([]);
    },
  });

  const revokeMutation = useMutation({
    mutationFn: async (ids: number[]) => {
      // 여러 항목을 순차적으로 해제
      for (const id of ids) {
        await accessUserPermissionPresetPort.revoke(id);
      }
    },
    onSuccess: () => {
      toast.success(t("revokeSuccess"));
      qc.invalidateQueries({ queryKey: ["access-user-permission-preset", "by-user", userId] });
      setAssignedSelected(new Set());
    },
  });

  const handleAdd = useCallback(() => {
    if (addCandidates.length === 0) return;
    assignMutation.mutate(addCandidates.map(Number));
  }, [addCandidates, assignMutation]);

  const handleRevoke = useCallback(async () => {
    if (assignedSelected.size === 0) return;
    const ok = await confirm({
      title: t("revokeConfirmTitle"),
      description: t("revokeConfirmDesc", { count: assignedSelected.size }),
      variant: "destructive",
      confirmText: t("revokeConfirmOk"),
      cancelText: t("revokeConfirmCancel"),
    });
    if (!ok) return;
    const revokeIds = assigned
      .filter((r) => assignedSelected.has(r.id))
      .map((r) => r.id);
    revokeMutation.mutate(revokeIds);
  }, [assignedSelected, assigned, revokeMutation, t]);

  const isMutating = assignMutation.isPending || revokeMutation.isPending;
  const sectionTitle = username ? t("titleWithUser", { username }) : t("title");

  return (
    <div className="panel" style={{ marginTop: 16, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{sectionTitle}</span>
        <span className="panel__rowcount">{assigned.length}</span>
      </div>

      {/* 추가 영역 */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 8,
          padding: "8px 12px",
          borderBottom: "1px solid var(--border-1)",
        }}
      >
        <MultiSelectBox
          options={poolOptions}
          value={addCandidates}
          onChange={setAddCandidates}
          placeholder={poolFetching ? t("loading") : t("addPlaceholder")}
          style={{ flex: 1 }}
          disabled={poolFetching || isMutating}
        />
        <Button
          size="sm"
          variant="modal"
          disabled={addCandidates.length === 0 || isMutating}
          onClick={handleAdd}
          loading={assignMutation.isPending}
        >
          {t("addBtn")}
        </Button>
      </div>

      {/* 보유 목록 + 해제 */}
      <div style={{ display: "flex", justifyContent: "flex-end", padding: "6px 12px" }}>
        <Button
          size="sm"
          disabled={assignedSelected.size === 0 || isMutating}
          onClick={handleRevoke}
          loading={revokeMutation.isPending}
        >
          {t("revokeBtn")}
        </Button>
      </div>

      <div className="list-wrap" style={{ minHeight: 300 }}>
        <GridList<UserPermissionPresetRef>
          columns={assignedColumns}
          data={assigned}
          gridId="user-permission-preset-assigned"
          rowKey={(row) => String(row.id)}
          selectable
          selectedKeys={assignedSelected}
          onSelectionChange={(next) => setAssignedSelected(new Set([...next].map(Number)))}
          isLoading={assignedFetching}
          emptyMessage={t("emptyAssigned")}
        />
      </div>
    </div>
  );
}
