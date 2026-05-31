"use client";

import { useEffect, useMemo, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Plus, Minus } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { Button } from "@/components/shared/button";
import { GridList } from "@/components/shared/grid-list";
import { Save } from "lucide-react";
import { subscriptionUseCases } from "@/application/subscription/use-cases";
import { accessAttributeValueUseCases } from "@/application/access/attribute-value/use-cases";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { toast } from "@/lib/toast-store";
import type { SubscriptionItem } from "@/domain/subscription";
import {
  buildSubscriptionColumns,
  type SubscriptionFormRow,
  type SubscriptionFormValues,
} from "./subscription-grid-columns";

interface Props {
  subscriberId: number;
  subscriberCode?: string;
}

function toFormRow(item: SubscriptionItem): SubscriptionFormRow {
  return {
    entityId: item.id,
    moduleCode: item.moduleCode,
    startDate: item.startDate,
    endDate: item.endDate,
    active: item.active,
  };
}

const ROW_IS_EQUAL = (a: SubscriptionFormRow, b: SubscriptionFormRow) =>
  a.moduleCode === b.moduleCode &&
  a.startDate === b.startDate &&
  a.endDate === b.endDate &&
  a.active === b.active;

const TO_CREATE = (row: SubscriptionFormRow) => ({
  moduleCode: row.moduleCode,
  startDate: row.startDate,
  endDate: row.endDate,
  active: row.active,
});

const TO_UPDATE = (row: SubscriptionFormRow) => ({
  id: row.entityId,
  startDate: row.startDate,
  endDate: row.endDate,
  active: row.active,
});

export function SubscriberSubscriptionSection({ subscriberId, subscriberCode }: Props) {
  const tSection = useTranslations("admin.subscriber.section");
  const tOptions = useTranslations("admin.subscriber.options");

  const qc = useQueryClient();

  const { control, register, getValues, reset, formState: { isDirty } } = useForm<SubscriptionFormValues>({
    defaultValues: { rows: [] },
  });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  // staleTime/gcTime Infinity — permission-preset section 패턴 준수
  const { data: items = [], isFetching } = useQuery({
    queryKey: ["admin-subscription", "by-subscriber", subscriberId],
    queryFn: () => subscriptionUseCases.listBySubscriber(subscriberId),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const { data: moduleValues = [] } = useQuery({
    queryKey: ["admin-access-attribute-value", "module"],
    queryFn: () => accessAttributeValueUseCases.listByKey("module"),
    staleTime: Infinity,
  });

  const moduleOptions = useMemo(
    () => moduleValues.map((v) => ({ value: v.value, label: v.label ?? v.value })),
    [moduleValues],
  );

  const originalRows = useMemo<SubscriptionFormRow[]>(
    () => items.map(toFormRow),
    [items],
  );

  // items가 변경되면 폼 동기화
  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: TO_CREATE,
        toUpdate: TO_UPDATE,
        isEqual: ROW_IS_EQUAL,
      });
      return subscriptionUseCases.saveChanges(subscriberId, {
        creates: changes.creates,
        updates: changes.updates,
        deleteIds: changes.deleteIds,
      });
    },
    onSuccess: (result) => {
      toast.success(
        tSection("saveSuccess", {
          created: result.createdCount,
          updated: result.updatedCount,
          deleted: result.deletedCount,
        }),
      );
      qc.invalidateQueries({ queryKey: ["admin-subscription", "by-subscriber", subscriberId] });
    },
  });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());

  function handleAdd() {
    append({
      entityId: -Date.now(),
      moduleCode: moduleOptions[0]?.value ?? "",
      startDate: "",
      endDate: "",
      active: true,
    });
  }

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

  const columns = useMemo(
    () => buildSubscriptionColumns(register, control, moduleOptions, tSection, tOptions),
    [register, control, moduleOptions, tSection, tOptions],
  );

  const title = subscriberCode
    ? tSection("title", { subscriberCode })
    : tSection("title", { subscriberCode: String(subscriberId) });

  return (
    <div className="panel" style={{ marginTop: 16, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{title}</span>
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

      <div style={{ display: "flex", justifyContent: "flex-end", padding: "6px 12px" }}>
        <ActionButton
          buttonCode="BTN_ADMIN_SUBSCRIBER_SUBSCRIPTION_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      <div className="list-wrap">
        <GridList<SubscriptionFormRow>
          columns={columns}
          data={fields as unknown as SubscriptionFormRow[]}
          rowKey={(row) => row.entityId}
          isLoading={isFetching}
          emptyMessage={tSection("empty")}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
        />
      </div>
    </div>
  );
}
