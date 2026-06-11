"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Plus, Save, RotateCcw } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { commonCodeUseCases } from "@/application/common-code/use-cases";
import type { CommonCodeGroupRow } from "@/domain/common-code";
import {
  buildCommonCodeColumns,
  getCommonCodeRowClassName,
  type CommonCodeFormRow,
  type CommonCodeFormValues,
} from "./common-code-grid-columns";
import {
  toCommonCodeFormRow,
  COMMON_CODE_ROW_IS_EQUAL,
  COMMON_CODE_TO_CREATE,
  COMMON_CODE_TO_UPDATE,
} from "./common-code-list-helpers";

export function CommonCodeListClient() {
  const tMsg = useTranslations("admin.commonCode.msg");
  const tCols = useTranslations("admin.commonCode.cols");
  const tOptions = useTranslations("admin.commonCode.options");
  const tPanel = useTranslations("admin.commonCode.panel");
  const tGroup = useTranslations("admin.commonCode.group");

  const qc = useQueryClient();

  // ─── 그룹 목록 조회 ──────────────────────────────────────────────────────

  const { data: groups, isFetching: groupsFetching } = useQuery({
    queryKey: ["admin-common-code", "groups"],
    queryFn: () => commonCodeUseCases.listGroups(),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const [selectedGroupCode, setSelectedGroupCode] = useState<string | null>(null);

  // ─── 코드 그리드 폼 ──────────────────────────────────────────────────────

  const { control, register, getValues, reset, formState: { isDirty } } =
    useForm<CommonCodeFormValues>({ defaultValues: { rows: [] } });
  const { fields, append } = useFieldArray({ control, name: "rows" });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const pendingFocusRef = useRef<number | null>(null);

  // ─── 코드 목록 조회 ──────────────────────────────────────────────────────

  const enabled = selectedGroupCode !== null;

  const { data: codeRows, isFetching: codesFetching } = useQuery({
    queryKey: ["admin-common-code", "list", selectedGroupCode],
    queryFn: () => commonCodeUseCases.listByGroup(selectedGroupCode!),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<CommonCodeFormRow[]>(
    () => (codeRows ?? []).map(toCommonCodeFormRow),
    [codeRows],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // ─── 그룹 선택 ───────────────────────────────────────────────────────────

  function handleSelectGroup(groupCode: string) {
    if (groupCode === selectedGroupCode) return;
    setSelectedGroupCode(groupCode);
    setSelectedKeys(new Set());
  }

  // ─── Reset ───────────────────────────────────────────────────────────────

  function handleReset() {
    setSelectedGroupCode(null);
    setSelectedKeys(new Set());
    reset({ rows: [] });
    qc.invalidateQueries({ queryKey: ["admin-common-code", "groups"] });
  }

  // ─── 행 추가 ─────────────────────────────────────────────────────────────

  function handleAdd() {
    const id = -Date.now();
    append({ entityId: id, code: "", label: "", labelKo: "", sortOrder: null, active: true });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="code"]`,
      ) as HTMLElement | null;
      const input = td?.querySelector("input:not([type=hidden])") as HTMLInputElement | null;
      input?.focus();
    });
  });

  // ─── save-changes ─────────────────────────────────────────────────────────

  const invalidateCodes = () =>
    qc.invalidateQueries({ queryKey: ["admin-common-code", "list", selectedGroupCode] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: COMMON_CODE_TO_CREATE,
        toUpdate: COMMON_CODE_TO_UPDATE,
        isEqual: COMMON_CODE_ROW_IS_EQUAL,
      });
      return commonCodeUseCases.saveChanges({
        groupCode: selectedGroupCode!,
        creates: changes.creates,
        updates: changes.updates,
      });
    },
    onSuccess: (result) => {
      toast.success(
        tMsg("saveSuccess", {
          created: result.createdCount,
          updated: result.updatedCount,
          deleted: result.deletedCount,
        }),
      );
      invalidateCodes();
    },
  });

  // ─── 컬럼 ────────────────────────────────────────────────────────────────

  const columns = useMemo(
    () => buildCommonCodeColumns(register, control, tCols, tOptions),
    [register, control, tCols, tOptions],
  );

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  const isSaveDisabled =
    !isDirty || saveChangesMutation.isPending || selectedGroupCode === null;

  return (
    <>
      {/* 상단 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_COMMON_CODE_RESET"
          className="btn btn--normal btn--sm"
          onClick={handleReset}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_COMMON_CODE_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={isSaveDisabled}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 2분할 컨테이너: 그룹(좌) + 코드(우) */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "320px 1fr",
          gap: 12,
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
        }}
      >
        {/* 좌측: 그룹 목록 */}
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">{tGroup("title")}</span>
              <span className="panel__rowcount">{groups?.length ?? 0}</span>
            </div>
            <div className="list-wrap">
              <GroupListTable
                groups={groups ?? []}
                selectedGroupCode={selectedGroupCode}
                onSelectGroup={handleSelectGroup}
                isLoading={groupsFetching}
                emptyMessage={tGroup("noGroups")}
              />
            </div>
          </div>
        </div>

        {/* 우측: 코드 그리드 */}
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          {selectedGroupCode === null ? (
            <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
              <div className="panel__head">
                <div className="panel__title-accent" />
                <span className="panel__title">{tPanel("title")}</span>
              </div>
              <div
                className="list-wrap"
                style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
              >
                <span style={{ color: "var(--ink-3)" }}>{tMsg("selectGroup")}</span>
              </div>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
              {/* 코드 그리드 툴바 */}
              <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
                <Button variant="success" size="sm" iconOnly onClick={handleAdd}>
                  <Plus size={12} />
                </Button>
              </div>

              <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
                <div className="panel__head">
                  <div className="panel__title-accent" />
                  <span className="panel__title">{tPanel("title")}</span>
                  <span className="panel__rowcount">{fields.length}</span>
                </div>
                <div className="list-wrap">
                  <GridList<CommonCodeFormRow>
                    columns={columns}
                    data={fields as unknown as CommonCodeFormRow[]}
                    rowKey={(row) => row.entityId}
                    rowClassName={(row) => getCommonCodeRowClassName(row, originalRows)}
                    isLoading={codesFetching}
                    emptyMessage={tMsg("noResults")}
                    selectable
                    selectedKeys={selectedKeys}
                    onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
                  />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
}

// ─── 그룹 목록 테이블 (간단한 읽기 전용) ────────────────────────────────────

interface GroupListTableProps {
  groups: CommonCodeGroupRow[];
  selectedGroupCode: string | null;
  onSelectGroup: (groupCode: string) => void;
  isLoading: boolean;
  emptyMessage: string;
}

function GroupListTable({
  groups,
  selectedGroupCode,
  onSelectGroup,
  isLoading,
  emptyMessage,
}: GroupListTableProps) {
  if (isLoading) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1, padding: 16 }}>
        <span style={{ color: "var(--ink-3)" }}>…</span>
      </div>
    );
  }
  if (groups.length === 0) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1, padding: 16 }}>
        <span style={{ color: "var(--ink-3)" }}>{emptyMessage}</span>
      </div>
    );
  }

  return (
    <table className="grid-table" style={{ width: "100%" }}>
      <colgroup>
        <col style={{ width: 36 }} />
        <col />
        <col style={{ width: 70 }} />
      </colgroup>
      <thead>
        <tr>
          <th>#</th>
          <th>Group Code</th>
          <th>Module</th>
        </tr>
      </thead>
      <tbody>
        {groups.map((g, i) => (
          <tr
            key={g.id}
            onClick={() => onSelectGroup(g.groupCode)}
            className={g.groupCode === selectedGroupCode ? "is-selected" : undefined}
            style={{ cursor: "pointer" }}
          >
            <td className="row-num">{i + 1}</td>
            <td style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}>{g.groupCode}</td>
            <td>{g.sourceModule}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
