"use client";

import { useState, useMemo, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { permissionPresetPort, accessAttributeValuePort } from "@/lib/ports";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { MultiSelectBox } from "@/components/shared/inputs/multi-select-box";
import type { MultiSelectBoxOption } from "@/components/shared/inputs/multi-select-box";
import type { AttributeValueRef } from "@/domain/access/permission-preset";

interface Props {
  presetId: number;
  presetCode: string;
}

export function PermissionPresetAttributeValuesSection({ presetId, presetCode }: Props) {
  // useTranslations MUST be called unconditionally before any early return
  const tSection = useTranslations("admin.permissionPreset.section");

  const qc = useQueryClient();
  const [assignedSelected, setAssignedSelected] = useState<Set<string>>(new Set());
  // MultiSelectBox는 string[] value를 사용; id를 문자열로 매핑
  const [addCandidates, setAddCandidates] = useState<string[]>([]);

  const assignedColumns = useMemo<GridColumn<AttributeValueRef>[]>(() => [
    { key: "attributeKey", label: tSection("colAttributeKey"), minWidth: 140 },
    { key: "value",        label: tSection("colValue"),        minWidth: 100 },
    { key: "label",        label: tSection("colLabel"),        minWidth: 140 },
  ], [tSection]);

  // preset 상세: attributeValues 배열 (id 포함)
  const { data: preset, isFetching: presetFetching } = useQuery({
    queryKey: ["permission-preset", "detail", presetId],
    queryFn: () => permissionPresetPort.getById(presetId),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  // 전체 attribute_value 풀 — 추가 후보 선택용
  const { data: allValues, isFetching: poolFetching } = useQuery({
    queryKey: ["access-attribute-value", "all"],
    queryFn: () => accessAttributeValuePort.listAll(),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const assignedRows = useMemo<AttributeValueRef[]>(
    () => preset?.attributeValues ?? [],
    [preset],
  );

  // 이미 보유한 id Set — 풀에서 제외하기 위해 사용
  const assignedIdSet = useMemo(
    () => new Set(assignedRows.map((r) => r.id)),
    [assignedRows],
  );

  // MultiSelectBox 옵션: 아직 할당되지 않은 attribute_value만 표시
  const poolOptions = useMemo<MultiSelectBoxOption[]>(
    () =>
      (allValues ?? [])
        .filter((av) => !assignedIdSet.has(av.id))
        .map((av) => ({
          value: String(av.id),
          label: `[${av.attributeKey}] ${av.value}${av.label ? ` (${av.label})` : ""}`,
        })),
    [allValues, assignedIdSet],
  );

  const addMutation = useMutation({
    mutationFn: (addIds: number[]) =>
      permissionPresetPort.assignAttributeValues(presetId, { addIds, removeIds: [] }),
    onSuccess: () => {
      toast.success(tSection("addSuccess"));
      qc.invalidateQueries({ queryKey: ["permission-preset", "detail", presetId] });
      qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });
      setAddCandidates([]);
    },
  });

  const removeMutation = useMutation({
    mutationFn: (removeIds: number[]) =>
      permissionPresetPort.assignAttributeValues(presetId, { addIds: [], removeIds }),
    onSuccess: () => {
      toast.success(tSection("removeSuccess"));
      qc.invalidateQueries({ queryKey: ["permission-preset", "detail", presetId] });
      qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });
      setAssignedSelected(new Set());
    },
  });

  const handleAdd = useCallback(() => {
    if (addCandidates.length === 0) return;
    const addIds = addCandidates.map(Number);
    addMutation.mutate(addIds);
  }, [addCandidates, addMutation]);

  const handleRemove = useCallback(async () => {
    if (assignedSelected.size === 0) return;
    const ok = await confirm({
      title: tSection("removeConfirmTitle"),
      description: tSection("removeConfirmDesc", { count: assignedSelected.size }),
      variant: "destructive",
      confirmText: tSection("removeConfirmOk"),
      cancelText: tSection("removeConfirmCancel"),
    });
    if (!ok) return;
    const removeIds = assignedRows
      .filter((row) => assignedSelected.has(String(row.id)))
      .map((row) => row.id);
    removeMutation.mutate(removeIds);
  }, [assignedSelected, assignedRows, removeMutation, tSection]);

  const isMutating = addMutation.isPending || removeMutation.isPending;

  return (
    <div className="panel" style={{ marginTop: 16, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tSection("title", { presetCode })}</span>
        <span className="panel__rowcount">{assignedRows.length}</span>
      </div>

      {/* 추가 영역 */}
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 12px", borderBottom: "1px solid var(--border-1)" }}>
        <MultiSelectBox
          options={poolOptions}
          value={addCandidates}
          onChange={setAddCandidates}
          placeholder={poolFetching ? tSection("loading") : tSection("addPlaceholder")}
          style={{ flex: 1 }}
          disabled={poolFetching || isMutating}
        />
        <Button
          size="sm"
          variant="modal"
          type="button"
          disabled={addCandidates.length === 0 || isMutating}
          onClick={handleAdd}
          loading={addMutation.isPending}
        >
          {tSection("addBtn")}
        </Button>
      </div>

      {/* 보유 목록 + 제거 */}
      <div style={{ display: "flex", justifyContent: "flex-end", padding: "6px 12px" }}>
        <Button
          size="sm"
          type="button"
          disabled={assignedSelected.size === 0 || isMutating}
          onClick={handleRemove}
          loading={removeMutation.isPending}
        >
          {tSection("revokeBtn")}
        </Button>
      </div>
      <div className="list-wrap" style={{ minHeight: 300 }}>
        <GridList<AttributeValueRef>
          columns={assignedColumns}
          data={assignedRows}
          gridId="preset-attr-value-assigned"
          rowKey={(row) => String(row.id)}
          selectable
          selectedKeys={assignedSelected}
          onSelectionChange={(next) => setAssignedSelected(new Set([...next].map(String)))}
          isLoading={presetFetching}
          emptyMessage={tSection("emptyAssigned")}
        />
      </div>
    </div>
  );
}
