"use client";

import { useState, useMemo, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2, Pencil } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessAttributePort } from "@/lib/ports";
import { accessAttributeUseCases } from "@/application/access/attribute/use-cases";
import { ActionButton } from "@/components/admin/access/action-button";
import { AttributeValueSection } from "@/components/admin/access/attribute-value-section";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import type {
  CreateAttributeDefinitionDto,
  UpdateAttributeDefinitionDto,
  AttributeValueType,
  AttributeDefinitionRow,
} from "@/domain/access/attribute";

const VALUE_TYPES: AttributeValueType[] = ["STRING", "NUMBER", "BOOLEAN", "ENUM"];

const DEFAULT_FORM: CreateAttributeDefinitionDto = {
  attributeKey: "",
  name: "",
  valueType: "STRING",
  allowMulti: false,
  active: true,
};

interface UpdateFormValues {
  name: string;
  valueType: AttributeValueType;
  allowMulti: boolean;
  active: boolean;
}

const DEFAULT_UPDATE: UpdateFormValues = {
  name: "",
  valueType: "STRING",
  allowMulti: false,
  active: true,
};

const ATTRIBUTE_COLUMNS: GridColumn<AttributeDefinitionRow>[] = [
  { key: "attributeKey", label: "attributeKey", minWidth: 140 },
  { key: "name", label: "name", minWidth: 120 },
  { key: "valueType", label: "valueType", minWidth: 100 },
  { key: "allowMulti", label: "allowMulti", minWidth: 80, align: "center", render: (v) => (v ? "Y" : "N") },
  { key: "active", label: "active", minWidth: 70, align: "center", render: (v) => (v ? "활성" : "비활성") },
];

export function AccessAttributeListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<AttributeDefinitionRow | null>(null);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<Set<string>>(new Set());
  const createForm = useForm<CreateAttributeDefinitionDto>({ defaultValues: DEFAULT_FORM });
  const editForm = useForm<UpdateFormValues>({ defaultValues: DEFAULT_UPDATE });

  const { data, isFetching } = useQuery({
    queryKey: ["access-attribute", "list"],
    queryFn: () => accessAttributePort.search(1, 100),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (req: CreateAttributeDefinitionDto) => accessAttributePort.create(req),
    onSuccess: () => {
      toast.success("속성이 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });
      setCreateOpen(false);
      createForm.reset(DEFAULT_FORM);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ attributeKey, req }: { attributeKey: string; req: UpdateAttributeDefinitionDto }) =>
      accessAttributePort.update(attributeKey, req),
    onSuccess: () => {
      toast.success("속성이 수정되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });
      setEditTarget(null);
      editForm.reset(DEFAULT_UPDATE);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (attributeKey: string) => accessAttributePort.delete(attributeKey),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });
      setSelectedKey(null);
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (keys: string[]) => accessAttributeUseCases.deleteMany(keys),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const openEdit = useCallback((row: AttributeDefinitionRow) => {
    setEditTarget(row);
    editForm.reset({
      name: row.name,
      valueType: row.valueType,
      allowMulti: row.allowMulti,
      active: row.active,
    });
  }, [editForm]);

  function handleEditSave(values: UpdateFormValues) {
    if (!editTarget) return;
    const req: UpdateAttributeDefinitionDto = {
      name: values.name.trim(),
      valueType: values.valueType,
      allowMulti: values.allowMulti,
      active: values.active,
    };
    updateMutation.mutate({ attributeKey: editTarget.attributeKey, req });
  }

  function handleRowClick(attributeKey: string, valueType: AttributeValueType) {
    if (valueType !== "ENUM") return;
    setSelectedKey((prev) => (prev === attributeKey ? null : attributeKey));
  }

  const handleDelete = useCallback(async (attributeKey: string) => {
    const ok = await confirm({
      title: "속성 삭제",
      description: `"${attributeKey}" 속성을 삭제하시겠습니까?`,
      variant: "destructive",
      confirmText: "삭제",
      cancelText: "취소",
    });
    if (!ok) return;
    deleteMutation.mutate(attributeKey);
  }, [deleteMutation]);

  async function handleBulkDelete() {
    const ok = await confirm({ title: "선택 삭제", description: `선택한 ${selectedKeys.size}개 항목을 삭제하시겠습니까?`, variant: "destructive" });
    if (ok) bulkDeleteMutation.mutate([...selectedKeys]);
  }

  const columns = useMemo<GridColumn<AttributeDefinitionRow>[]>(() => [
    ...ATTRIBUTE_COLUMNS,
    {
      key: "_actions",
      label: "",
      minWidth: 70,
      render: (_v, row) => (
        <div style={{ display: "flex", gap: 4 }} onClick={(e) => e.stopPropagation()}>
          <ActionButton
            buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_UPDATE"
            className="btn btn--sm"
            onClick={(e) => { e.stopPropagation(); openEdit(row); }}
          >
            <Pencil size={12} />
          </ActionButton>
          <ActionButton
            buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_DELETE"
            className="btn btn--danger btn--sm"
            onClick={(e) => { e.stopPropagation(); handleDelete(row.attributeKey); }}
            disabled={deleteMutation.isPending}
          >
            <Trash2 size={12} />
          </ActionButton>
        </div>
      ),
    },
  ], [deleteMutation.isPending, openEdit, handleDelete]);

  const rows = data?.content ?? [];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_DELETE"
          className="btn btn--modal btn--sm"
          disabled={selectedKeys.size === 0 || bulkDeleteMutation.isPending}
          onClick={handleBulkDelete}
        >
          선택 삭제
        </ActionButton>
        <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setCreateOpen(true)}>신규</Button>
      </div>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Attributes</span>
          <span className="panel__rowcount">{rows.length}</span>
          <ColumnVisibilityMenu<AttributeDefinitionRow> gridId="access-attribute" defaultColumns={ATTRIBUTE_COLUMNS} />
        </div>
        <div className="list-wrap">
          <GridList<AttributeDefinitionRow>
            columns={columns}
            data={rows}
            gridId="access-attribute"
            rowKey={(row) => row.attributeKey}
            onRowClick={(row) => handleRowClick(row.attributeKey, row.valueType)}
            rowClassName={(row) => {
              const cls: string[] = [];
              if (selectedKey === row.attributeKey) cls.push("is-selected");
              if (row.valueType === "ENUM") cls.push("cursor-pointer");
              return cls.length > 0 ? cls.join(" ") : undefined;
            }}
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(String)))}
            isLoading={isFetching}
            emptyMessage="데이터가 없습니다."
          />
        </div>
      </div>

      {/* ENUM 타입 행 클릭 시 attribute-value 서브 섹션 노출 */}
      {selectedKey !== null && <AttributeValueSection attributeKey={selectedKey} />}

      {/* 신규 등록 모달 */}
      <ModalShell isOpen={createOpen} title="속성 등록">
        <form onSubmit={createForm.handleSubmit((v) => createMutation.mutate(v))} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn"><span className="lcn__label">attributeKey</span><input className="text-box text-box--panel" {...createForm.register("attributeKey")} /></div>
            <div className="lcn"><span className="lcn__label">name</span><input className="text-box text-box--panel" {...createForm.register("name")} /></div>
            <div className="lcn"><span className="lcn__label">valueType</span>
              <select className="text-box text-box--panel" {...createForm.register("valueType")}>
                {VALUE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="lcn"><span className="lcn__label">allowMulti</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...createForm.register("allowMulti")} />허용</label></div>
            <div className="lcn"><span className="lcn__label">활성</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...createForm.register("active")} />활성</label></div>
          </div>
        </form>
        <div className="modal__footer">
          <Button variant="modal" size="sm" onClick={createForm.handleSubmit((v) => createMutation.mutate(v))} loading={createMutation.isPending}>저장</Button>
          <Button size="sm" onClick={() => { setCreateOpen(false); createForm.reset(DEFAULT_FORM); }}>닫기</Button>
        </div>
      </ModalShell>

      {/* 수정 모달 */}
      <ModalShell isOpen={editTarget !== null} title="속성 수정">
        <form onSubmit={editForm.handleSubmit(handleEditSave)} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn">
              <span className="lcn__label">attributeKey</span>
              <span className="text-box text-box--panel" style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}>
                {editTarget?.attributeKey}
              </span>
            </div>
            <div className="lcn"><span className="lcn__label">name</span><input className="text-box text-box--panel" {...editForm.register("name")} /></div>
            <div className="lcn"><span className="lcn__label">valueType</span>
              <select className="text-box text-box--panel" {...editForm.register("valueType")}>
                {VALUE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="lcn"><span className="lcn__label">allowMulti</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...editForm.register("allowMulti")} />허용</label></div>
            <div className="lcn"><span className="lcn__label">활성</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...editForm.register("active")} />활성</label></div>
          </div>
        </form>
        <div className="modal__footer">
          <Button variant="modal" size="sm" onClick={editForm.handleSubmit(handleEditSave)} loading={updateMutation.isPending}>저장</Button>
          <Button size="sm" onClick={() => { setEditTarget(null); editForm.reset(DEFAULT_UPDATE); }}>닫기</Button>
        </div>
      </ModalShell>
    </>
  );
}
