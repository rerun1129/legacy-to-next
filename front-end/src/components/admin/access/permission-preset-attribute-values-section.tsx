"use client";

import { useState, useMemo, useCallback } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
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

const ASSIGNED_COLUMNS: GridColumn<AttributeValueRef>[] = [
  { key: "attributeKey", label: "attributeKey", minWidth: 140 },
  { key: "value", label: "value", minWidth: 100 },
  { key: "label", label: "label", minWidth: 140 },
];

export function PermissionPresetAttributeValuesSection({ presetId, presetCode }: Props) {
  const qc = useQueryClient();
  const [assignedSelected, setAssignedSelected] = useState<Set<string>>(new Set());
  // MultiSelectBox는 string[] value를 사용; id를 문자열로 매핑
  const [addCandidates, setAddCandidates] = useState<string[]>([]);

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
      toast.success("선택한 attribute_value가 추가되었습니다.");
      qc.invalidateQueries({ queryKey: ["permission-preset", "detail", presetId] });
      qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });
      setAddCandidates([]);
    },
  });

  const removeMutation = useMutation({
    mutationFn: (removeIds: number[]) =>
      permissionPresetPort.assignAttributeValues(presetId, { addIds: [], removeIds }),
    onSuccess: () => {
      toast.success("선택한 attribute_value가 제거되었습니다.");
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
      title: "attribute_value 제거",
      description: `선택한 ${assignedSelected.size}개 항목을 이 프리셋에서 제거하시겠습니까?`,
      variant: "destructive",
      confirmText: "제거",
      cancelText: "취소",
    });
    if (!ok) return;
    const removeIds = assignedRows
      .filter((row) => assignedSelected.has(String(row.id)))
      .map((row) => row.id);
    removeMutation.mutate(removeIds);
  }, [assignedSelected, assignedRows, removeMutation]);

  const isMutating = addMutation.isPending || removeMutation.isPending;

  return (
    <div className="panel" style={{ marginTop: 16, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Attribute Values — {presetCode}</span>
        <span className="panel__rowcount">{assignedRows.length}</span>
      </div>

      {/* 추가 영역 */}
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 12px", borderBottom: "1px solid var(--border-1)" }}>
        <MultiSelectBox
          options={poolOptions}
          value={addCandidates}
          onChange={setAddCandidates}
          placeholder={poolFetching ? "로딩 중…" : "추가할 attribute_value 선택"}
          style={{ flex: 1 }}
          disabled={poolFetching || isMutating}
        />
        <Button
          size="sm"
          variant="modal"
          disabled={addCandidates.length === 0 || isMutating}
          onClick={handleAdd}
          loading={addMutation.isPending}
        >
          추가
        </Button>
      </div>

      {/* 보유 목록 + 제거 */}
      <div style={{ display: "flex", justifyContent: "flex-end", padding: "6px 12px" }}>
        <Button
          size="sm"
          disabled={assignedSelected.size === 0 || isMutating}
          onClick={handleRemove}
          loading={removeMutation.isPending}
        >
          선택 제거
        </Button>
      </div>
      <div className="list-wrap" style={{ minHeight: 300 }}>
        <GridList<AttributeValueRef>
          columns={ASSIGNED_COLUMNS}
          data={assignedRows}
          gridId="preset-attr-value-assigned"
          rowKey={(row) => String(row.id)}
          selectable
          selectedKeys={assignedSelected}
          onSelectionChange={(next) => setAssignedSelected(new Set([...next].map(String)))}
          isLoading={presetFetching}
          emptyMessage="보유한 attribute_value가 없습니다."
        />
      </div>
    </div>
  );
}
