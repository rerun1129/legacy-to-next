"use client";

import { useState, useMemo, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessMenuPolicyPort, accessButtonPolicyPort } from "@/lib/ports";
import { accessMenuPolicyUseCases } from "@/application/access/menu-policy/use-cases";
import { accessButtonPolicyUseCases } from "@/application/access/button-policy/use-cases";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import type { CreateMenuPolicyDto, CreateButtonPolicyDto } from "@/domain/access/policy";

const DEFAULT_MENU_POLICY: CreateMenuPolicyDto = { menuId: 0, attributeKey: "", requiredValue: "" };
const DEFAULT_BTN_POLICY: CreateButtonPolicyDto = { buttonId: 0, attributeKey: "", requiredValue: "" };

type Tab = "menu" | "button";

type MenuPolicyRow = { id: number; menuId: number; attributeKey: string; requiredValue: string };
type BtnPolicyRow = { id: number; buttonId: number; attributeKey: string; requiredValue: string };

const MENU_POLICY_COLUMNS: GridColumn<MenuPolicyRow>[] = [
  { key: "id", label: "ID", minWidth: 60 },
  { key: "menuId", label: "menuId", minWidth: 80 },
  { key: "attributeKey", label: "attributeKey", minWidth: 140 },
  { key: "requiredValue", label: "requiredValue", minWidth: 140 },
];

const BTN_POLICY_COLUMNS: GridColumn<BtnPolicyRow>[] = [
  { key: "id", label: "ID", minWidth: 60 },
  { key: "buttonId", label: "buttonId", minWidth: 80 },
  { key: "attributeKey", label: "attributeKey", minWidth: 140 },
  { key: "requiredValue", label: "requiredValue", minWidth: 140 },
];

export function AccessPolicyListClient() {
  const qc = useQueryClient();
  const [tab, setTab] = useState<Tab>("menu");
  const [filterMenuId, setFilterMenuId] = useState<string>("");
  const [filterButtonId, setFilterButtonId] = useState<string>("");
  const [menuPolicyOpen, setMenuPolicyOpen] = useState(false);
  const [btnPolicyOpen, setBtnPolicyOpen] = useState(false);
  const [menuPolicySelectedKeys, setMenuPolicySelectedKeys] = useState<Set<number>>(new Set());
  const [btnPolicySelectedKeys, setBtnPolicySelectedKeys] = useState<Set<number>>(new Set());
  const menuForm = useForm<CreateMenuPolicyDto>({ defaultValues: DEFAULT_MENU_POLICY });
  const btnForm = useForm<CreateButtonPolicyDto>({ defaultValues: DEFAULT_BTN_POLICY });

  const menuId = Number(filterMenuId) || 0;
  const buttonId = Number(filterButtonId) || 0;

  const { data: menuPolicies, isFetching: menuFetching } = useQuery({
    queryKey: ["access-menu-policy", menuId],
    queryFn: () => accessMenuPolicyPort.listByMenu(menuId),
    enabled: menuId > 0,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const { data: btnPolicies, isFetching: btnFetching } = useQuery({
    queryKey: ["access-button-policy", buttonId],
    queryFn: () => accessButtonPolicyPort.listByButton(buttonId),
    enabled: buttonId > 0,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMenuPolicy = useMutation({
    mutationFn: (req: CreateMenuPolicyDto) => accessMenuPolicyPort.create(req),
    onSuccess: () => { toast.success("Menu Policy 등록"); qc.invalidateQueries({ queryKey: ["access-menu-policy"] }); setMenuPolicyOpen(false); menuForm.reset(DEFAULT_MENU_POLICY); },
  });

  const deleteMenuPolicy = useMutation({
    mutationFn: (id: number) => accessMenuPolicyPort.delete(id),
    onSuccess: () => { toast.success("삭제됨"); qc.invalidateQueries({ queryKey: ["access-menu-policy"] }); },
  });

  const bulkDeleteMenuPolicy = useMutation({
    mutationFn: (ids: number[]) => accessMenuPolicyUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["access-menu-policy"] });
      setMenuPolicySelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const createBtnPolicy = useMutation({
    mutationFn: (req: CreateButtonPolicyDto) => accessButtonPolicyPort.create(req),
    onSuccess: () => { toast.success("Button Policy 등록"); qc.invalidateQueries({ queryKey: ["access-button-policy"] }); setBtnPolicyOpen(false); btnForm.reset(DEFAULT_BTN_POLICY); },
  });

  const deleteBtnPolicy = useMutation({
    mutationFn: (id: number) => accessButtonPolicyPort.delete(id),
    onSuccess: () => { toast.success("삭제됨"); qc.invalidateQueries({ queryKey: ["access-button-policy"] }); },
  });

  const bulkDeleteBtnPolicy = useMutation({
    mutationFn: (ids: number[]) => accessButtonPolicyUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["access-button-policy"] });
      setBtnPolicySelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const handleDeleteMenu = useCallback(async (id: number) => {
    const ok = await confirm({ title: "Menu Policy 삭제", description: "삭제하시겠습니까?", variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteMenuPolicy.mutate(id);
  }, [deleteMenuPolicy]);

  async function handleBulkDeleteMenu() {
    const ok = await confirm({ title: "선택 삭제", description: `선택한 ${menuPolicySelectedKeys.size}개 항목을 삭제하시겠습니까?`, variant: "destructive" });
    if (ok) bulkDeleteMenuPolicy.mutate([...menuPolicySelectedKeys]);
  }

  const handleDeleteBtn = useCallback(async (id: number) => {
    const ok = await confirm({ title: "Button Policy 삭제", description: "삭제하시겠습니까?", variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteBtnPolicy.mutate(id);
  }, [deleteBtnPolicy]);

  async function handleBulkDeleteBtn() {
    const ok = await confirm({ title: "선택 삭제", description: `선택한 ${btnPolicySelectedKeys.size}개 항목을 삭제하시겠습니까?`, variant: "destructive" });
    if (ok) bulkDeleteBtnPolicy.mutate([...btnPolicySelectedKeys]);
  }

  const menuPolicyColumns = useMemo<GridColumn<MenuPolicyRow>[]>(() => [
    ...MENU_POLICY_COLUMNS,
    {
      key: "_actions",
      label: "",
      minWidth: 60,
      render: (_v, row) => (
        <div onClick={(e) => e.stopPropagation()}>
          <button className="btn btn--danger btn--sm" onClick={() => handleDeleteMenu(row.id)}><Trash2 size={12} /></button>
        </div>
      ),
    },
  ], [handleDeleteMenu]);

  const btnPolicyColumns = useMemo<GridColumn<BtnPolicyRow>[]>(() => [
    ...BTN_POLICY_COLUMNS,
    {
      key: "_actions",
      label: "",
      minWidth: 60,
      render: (_v, row) => (
        <div onClick={(e) => e.stopPropagation()}>
          <button className="btn btn--danger btn--sm" onClick={() => handleDeleteBtn(row.id)}><Trash2 size={12} /></button>
        </div>
      ),
    },
  ], [handleDeleteBtn]);

  return (
    <>
      <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
        <button className={`btn btn--sm${tab === "menu" ? " btn--modal" : ""}`} onClick={() => setTab("menu")}>Menu Policy</button>
        <button className={`btn btn--sm${tab === "button" ? " btn--modal" : ""}`} onClick={() => setTab("button")}>Button Policy</button>
      </div>

      {tab === "menu" && (
        <>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <div className="lcn">
              <span className="lcn__label">Menu ID</span>
              <input className="text-box text-box--panel" placeholder="Menu ID 입력 후 조회" value={filterMenuId} onChange={(e) => setFilterMenuId(e.target.value)} />
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <ActionButton
                buttonCode="BTN_ADMIN_ACCESS_POLICY_DELETE"
                className="btn btn--modal btn--sm"
                disabled={menuPolicySelectedKeys.size === 0 || bulkDeleteMenuPolicy.isPending}
                onClick={handleBulkDeleteMenu}
              >
                선택 삭제
              </ActionButton>
              <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setMenuPolicyOpen(true)}>신규</Button>
            </div>
          </div>
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Menu Policy</span></div>
            <div className="list-wrap">
              <GridList<MenuPolicyRow>
                columns={menuPolicyColumns}
                data={menuPolicies ?? []}
                gridId="access-menu-policy"
                rowKey={(r) => r.id}
                selectable
                selectedKeys={menuPolicySelectedKeys}
                onSelectionChange={(next) => setMenuPolicySelectedKeys(new Set([...next].map(Number)))}
                isLoading={menuFetching}
                emptyMessage={menuId > 0 ? "데이터가 없습니다." : "Menu ID를 입력하세요."}
              />
            </div>
          </div>
          <ModalShell isOpen={menuPolicyOpen} title="Menu Policy 등록" size="md">
            <form onSubmit={menuForm.handleSubmit((v) => createMenuPolicy.mutate(v))} className="modal__body">
              <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                <div className="lcn"><span className="lcn__label">menuId</span><input type="number" className="text-box text-box--panel" {...menuForm.register("menuId", { valueAsNumber: true })} /></div>
                <div className="lcn"><span className="lcn__label">attributeKey</span><input className="text-box text-box--panel" {...menuForm.register("attributeKey")} /></div>
                <div className="lcn"><span className="lcn__label">requiredValue</span><input className="text-box text-box--panel" {...menuForm.register("requiredValue")} /></div>
              </div>
            </form>
            <div className="modal__footer">
              <Button variant="modal" size="sm" onClick={menuForm.handleSubmit((v) => createMenuPolicy.mutate(v))} loading={createMenuPolicy.isPending}>저장</Button>
              <Button size="sm" onClick={() => { setMenuPolicyOpen(false); menuForm.reset(DEFAULT_MENU_POLICY); }}>닫기</Button>
            </div>
          </ModalShell>
        </>
      )}

      {tab === "button" && (
        <>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <div className="lcn">
              <span className="lcn__label">Button ID</span>
              <input className="text-box text-box--panel" placeholder="Button ID 입력 후 조회" value={filterButtonId} onChange={(e) => setFilterButtonId(e.target.value)} />
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <ActionButton
                buttonCode="BTN_ADMIN_ACCESS_POLICY_DELETE"
                className="btn btn--modal btn--sm"
                disabled={btnPolicySelectedKeys.size === 0 || bulkDeleteBtnPolicy.isPending}
                onClick={handleBulkDeleteBtn}
              >
                선택 삭제
              </ActionButton>
              <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setBtnPolicyOpen(true)}>신규</Button>
            </div>
          </div>
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Button Policy</span></div>
            <div className="list-wrap">
              <GridList<BtnPolicyRow>
                columns={btnPolicyColumns}
                data={btnPolicies ?? []}
                gridId="access-btn-policy"
                rowKey={(r) => r.id}
                selectable
                selectedKeys={btnPolicySelectedKeys}
                onSelectionChange={(next) => setBtnPolicySelectedKeys(new Set([...next].map(Number)))}
                isLoading={btnFetching}
                emptyMessage={buttonId > 0 ? "데이터가 없습니다." : "Button ID를 입력하세요."}
              />
            </div>
          </div>
          <ModalShell isOpen={btnPolicyOpen} title="Button Policy 등록" size="md">
            <form onSubmit={btnForm.handleSubmit((v) => createBtnPolicy.mutate(v))} className="modal__body">
              <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                <div className="lcn"><span className="lcn__label">buttonId</span><input type="number" className="text-box text-box--panel" {...btnForm.register("buttonId", { valueAsNumber: true })} /></div>
                <div className="lcn"><span className="lcn__label">attributeKey</span><input className="text-box text-box--panel" {...btnForm.register("attributeKey")} /></div>
                <div className="lcn"><span className="lcn__label">requiredValue</span><input className="text-box text-box--panel" {...btnForm.register("requiredValue")} /></div>
              </div>
            </form>
            <div className="modal__footer">
              <Button variant="modal" size="sm" onClick={btnForm.handleSubmit((v) => createBtnPolicy.mutate(v))} loading={createBtnPolicy.isPending}>저장</Button>
              <Button size="sm" onClick={() => { setBtnPolicyOpen(false); btnForm.reset(DEFAULT_BTN_POLICY); }}>닫기</Button>
            </div>
          </ModalShell>
        </>
      )}
    </>
  );
}
