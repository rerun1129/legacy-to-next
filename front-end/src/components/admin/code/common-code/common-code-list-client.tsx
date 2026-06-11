"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { RotateCcw, Search } from "lucide-react";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { ActionButton } from "@/components/admin/access/action-button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { confirm } from "@/components/confirm";
import { commonCodeUseCases } from "@/application/common-code/use-cases";
import type { CommonCodeGroupRow } from "@/domain/common-code";
import { CommonCodeGroupFilter } from "./common-code-group-filter";
import { CommonCodeGroupGrid } from "./common-code-group-grid";
import { CommonCodeCodePanel } from "./common-code-code-panel";
import {
  buildCommonCodeColumns,
  type CommonCodeFormRow,
  type CommonCodeFormValues,
} from "./common-code-grid-columns";
import {
  toCommonCodeFormRow,
  COMMON_CODE_ROW_IS_EQUAL,
  COMMON_CODE_TO_CREATE,
  COMMON_CODE_TO_UPDATE,
} from "./common-code-list-helpers";
import type { GroupFilterValues } from "./common-code-filter-types";
export type { GroupFilterValues };

const SCOPE = "/admin/code/common-code";

const DEFAULT_FILTER: GroupFilterValues = {
  groupCode: "",
  module: "ALL",
};

type CommonCodeSearchState = SavedSearchState & {
  submittedFilter: GroupFilterValues | null;
  selectedGroupCode: string | null;
};

export function CommonCodeListClient() {
  const tMsg = useTranslations("admin.commonCode.msg");
  const tCols = useTranslations("admin.commonCode.cols");
  const tOptions = useTranslations("admin.commonCode.options");
  const tPanel = useTranslations("admin.commonCode.panel");

  const qc = useQueryClient();

  // ─── 필터 폼 ─────────────────────────────────────────────────────────────

  const filterForm = useForm<GroupFilterValues>({ defaultValues: DEFAULT_FILTER });
  useListFilterSync(filterForm, SCOPE);

  const [submittedFilter, setSubmittedFilter] = useState<GroupFilterValues | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CommonCodeSearchState | undefined;
    return s?.submittedFilter ?? null;
  });
  const [selectedGroupCode, setSelectedGroupCode] = useState<string | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CommonCodeSearchState | undefined;
    return s?.selectedGroupCode ?? null;
  });

  // 검색 상태 영속화
  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, {
      submittedFilter,
      selectedGroupCode,
    });
  }, [submittedFilter, selectedGroupCode]);

  // ─── 그룹 목록 조회 ──────────────────────────────────────────────────────

  const { data: groups = [], isFetching: groupsFetching } = useQuery<CommonCodeGroupRow[]>({
    queryKey: ["admin-common-code", "groups"],
    queryFn: () => commonCodeUseCases.listGroups(),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  // ─── 코드 그리드 폼 ──────────────────────────────────────────────────────

  const { control, register, getValues, reset, formState: { isDirty } } =
    useForm<CommonCodeFormValues>({ defaultValues: { rows: [] } });
  const { fields, append } = useFieldArray({ control, name: "rows" });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const pendingFocusRef = useRef<number | null>(null);

  // ─── 코드 목록 조회 ──────────────────────────────────────────────────────

  const { data: codeRows, isFetching: codesFetching } = useQuery({
    queryKey: ["admin-common-code", "list", selectedGroupCode],
    queryFn: () => commonCodeUseCases.listByGroup(selectedGroupCode!),
    enabled: selectedGroupCode !== null,
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

  // ─── 행 추가 포커스 ──────────────────────────────────────────────────────

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

  // ─── 그룹 선택 (dirty 가드) ───────────────────────────────────────────────

  async function handleSelectGroup(groupCode: string) {
    if (groupCode === selectedGroupCode) return;
    if (isDirty) {
      const ok = await confirm({
        title: tMsg("unsavedTitle"),
        description: tMsg("unsavedDescription"),
        variant: "destructive",
      });
      if (!ok) return;
    }
    setSelectedGroupCode(groupCode);
    setSelectedKeys(new Set());
  }

  // ─── Search ───────────────────────────────────────────────────────────────

  function handleSearch() {
    filterForm.handleSubmit((values) => {
      setSubmittedFilter(values);
      // 그룹 선택 초기화 — 새 검색 결과에서 새로 선택하도록
      setSelectedGroupCode(null);
      setSelectedKeys(new Set());
      reset({ rows: [] });
    })();
  }

  // ─── Reset ───────────────────────────────────────────────────────────────
  // invalidateQueries 금지 — Reset = 비우기(listFilter store 규칙)

  function handleReset() {
    filterForm.reset(DEFAULT_FILTER);
    setSubmittedFilter(null);
    setSelectedGroupCode(null);
    setSelectedKeys(new Set());
    reset({ rows: [] });
  }

  // ─── 행 추가 ─────────────────────────────────────────────────────────────

  function handleAdd() {
    const id = -Date.now();
    append({ entityId: id, code: "", label: "", labelKo: "", sortOrder: null, active: true });
    pendingFocusRef.current = id;
  }

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

  const isSaveDisabled =
    !isDirty || saveChangesMutation.isPending || selectedGroupCode === null;

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  return (
    <>
      {/* 상단 툴바 — Reset / Search */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_COMMON_CODE_RESET"
          className="btn btn--normal btn--sm"
          onClick={handleReset}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_COMMON_CODE_SEARCH"
          className="btn btn--search btn--sm"
          onClick={handleSearch}
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 필터 카드 */}
      <CommonCodeGroupFilter form={filterForm} />

      {/* 2분할 컨테이너: 그룹(좌) + 코드(우) */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "360px 1fr",
          gap: 12,
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
          marginTop: 10,
        }}
      >
        {/* 좌측: 그룹 GridList */}
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <CommonCodeGroupGrid
            groups={groups}
            selectedGroupCode={selectedGroupCode}
            onSelectGroup={handleSelectGroup}
            isLoading={groupsFetching}
            submittedFilter={submittedFilter}
          />
        </div>

        {/* 우측: 코드 그리드 */}
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <CommonCodeCodePanel
            selectedGroupCode={selectedGroupCode}
            tPanel={(k) => tPanel(k)}
            tMsg={(k) => tMsg(k)}
            fields={fields as unknown as CommonCodeFormRow[]}
            columns={columns}
            originalRows={originalRows}
            codesFetching={codesFetching}
            selectedKeys={selectedKeys}
            onSelectionChange={setSelectedKeys}
            isSaveDisabled={isSaveDisabled}
            onSave={() => saveChangesMutation.mutate()}
            onAdd={handleAdd}
          />
        </div>
      </div>
    </>
  );
}
