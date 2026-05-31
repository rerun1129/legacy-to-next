"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { RotateCcw, Search, Plus, Minus, Save } from "lucide-react";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE } from "@/lib/grid-pagination";
import { ActionButton } from "@/components/admin/access/action-button";
import { SubscriberListFilter } from "./subscriber-list-filter";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { Button } from "@/components/shared/button";
import type { SubscriberFilter } from "@/domain/subscriber";
import { subscriberUseCases } from "@/application/subscriber/use-cases";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { toast } from "@/lib/toast-store";
import {
  buildSubscriberColumns,
  getSubscriberRowClassName,
  type SubscriberFormRow,
  type FormValues,
} from "./subscriber-grid-columns";
import {
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
} from "./subscriber-list-helpers";
import { SubscriberSubscriptionSection } from "./subscriber-subscription-section";
import { useSubscriberGridPaste } from "./use-subscriber-grid-paste";

const DEFAULT_FILTER: SubscriberFilter = {
  subscriberCode: "",
  name: "",
  scope: "ALL",
};

const SCOPE = "/admin/subscriber/list";

type SubscriberSearchState = SavedSearchState & { extraFilter: SubscriberFilter | null };

export function SubscriberListClient() {
  const tMsg = useTranslations("admin.subscriber.msg");
  const tPanel = useTranslations("admin.subscriber.panel");
  const tCols = useTranslations("admin.subscriber.cols");
  const tOptions = useTranslations("admin.subscriber.options");

  const filterForm = useForm<SubscriberFilter>({ defaultValues: DEFAULT_FILTER });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<SubscriberFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as SubscriberSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });

  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, { extraFilter, currentPage });
  }, [extraFilter, currentPage]);

  const { control, register, getValues, setValue, reset, formState: { isDirty } } = useForm<FormValues>({
    defaultValues: { rows: [] },
  });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const { data, isFetching } = useQuery({
    queryKey: ["admin-subscriber", "list", extraFilter, currentPage],
    queryFn: () => subscriberUseCases.search(extraFilter!, currentPage, DEFAULT_PAGE_SIZE),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<SubscriberFormRow[]>(
    () => (data?.content ?? []).map(toFormRow),
    [data],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  useSubscriberGridPaste(getValues, setValue);

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const [subscriptionTargetId, setSubscriptionTargetId] = useState<number | null>(null);

  const handleCodeDoubleClick = useCallback((entityId: number) => {
    // 신규 행(entityId < 0)은 아직 저장되지 않았으므로 구독 섹션 노출 대상 제외
    if (entityId < 0) return;
    setSubscriptionTargetId(entityId);
  }, []);

  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    const id = -Date.now();
    append({
      entityId: id,
      subscriberCode: "",
      name: "",
      nameEn: "",
      businessNo: "",
      representative: "",
      phone: "",
      email: "",
      memo: "",
      active: true,
    });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="subscriberCode"]`,
      ) as HTMLElement | null;
      const input = td?.querySelector("input:not([type=hidden])") as HTMLInputElement | null;
      input?.focus();
    });
  });

  function handleRemove() {
    if (selectedKeys.size === 0) return;
    const rows = getValues("rows");
    const indices = rows
      .map((r, i) => (selectedKeys.has(r.entityId) ? i : -1))
      .filter((i) => i !== -1)
      .sort((a, b) => b - a);
    for (const idx of indices) remove(idx);
    setSelectedKeys(new Set());
  }

  const invalidateList = () => qc.invalidateQueries({ queryKey: ["admin-subscriber", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: TO_CREATE,
        toUpdate: TO_UPDATE,
        isEqual: ROW_IS_EQUAL,
      });
      return subscriberUseCases.saveChanges({
        creates: changes.creates,
        updates: changes.updates,
        deleteIds: changes.deleteIds,
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
      setSubscriptionTargetId(null);
      invalidateList();
    },
  });

  const columns = useMemo(
    () => buildSubscriberColumns(register, control, tCols, tOptions, handleCodeDoubleClick),
    [register, control, tCols, tOptions, handleCodeDoubleClick],
  );

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_SUBSCRIBER_LIST_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            filterForm.reset(DEFAULT_FILTER);
            invalidateList();
            setExtraFilter(null);
            setCurrentPage(1);
            setSubscriptionTargetId(null);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_SUBSCRIBER_LIST_SEARCH"
          className="btn btn--search btn--sm"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              invalidateList();
              setExtraFilter(values);
              setCurrentPage(1);
              setSubscriptionTargetId(null);
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_SUBSCRIBER_LIST_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      <SubscriberListFilter form={filterForm} />

      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
          <span className="panel__rowcount">{fields.length}</span>
          <div className="panel__actions">
            <Button variant="success" size="sm" iconOnly onClick={handleAdd}>
              <Plus size={12} />
            </Button>
            <Button
              variant="danger"
              size="sm"
              iconOnly
              onClick={handleRemove}
              disabled={selectedKeys.size === 0}
            >
              <Minus size={12} />
            </Button>
          </div>
        </div>

        <div className="list-wrap">
          <GridList<SubscriberFormRow>
            columns={columns}
            data={fields as unknown as SubscriberFormRow[]}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getSubscriberRowClassName(row, originalRows)}
            isLoading={isFetching}
            emptyMessage={
              extraFilter === null
                ? tMsg("enterCriteria")
                : tMsg("noResults")
            }
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
          />
        </div>

        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
          disabled={isFetching}
        />
      </div>

      {subscriptionTargetId !== null && (() => {
        const row = data?.content.find((r) => r.id === subscriptionTargetId);
        return (
          <SubscriberSubscriptionSection
            key={subscriptionTargetId}
            subscriberId={subscriptionTargetId}
            subscriberCode={row?.subscriberCode}
          />
        );
      })()}
    </>
  );
}
