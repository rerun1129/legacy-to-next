"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessMenuPolicyPort, accessButtonPolicyPort } from "@/lib/ports";
import type { CreateMenuPolicyDto, CreateButtonPolicyDto } from "@/domain/access/policy";

const DEFAULT_MENU_POLICY: CreateMenuPolicyDto = { menuId: 0, attributeKey: "", requiredValue: "" };
const DEFAULT_BTN_POLICY: CreateButtonPolicyDto = { buttonId: 0, attributeKey: "", requiredValue: "" };

type Tab = "menu" | "button";

export function AccessPolicyListClient() {
  const qc = useQueryClient();
  const [tab, setTab] = useState<Tab>("menu");
  const [filterMenuId, setFilterMenuId] = useState<string>("");
  const [filterButtonId, setFilterButtonId] = useState<string>("");
  const [menuPolicyOpen, setMenuPolicyOpen] = useState(false);
  const [btnPolicyOpen, setBtnPolicyOpen] = useState(false);
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

  const createBtnPolicy = useMutation({
    mutationFn: (req: CreateButtonPolicyDto) => accessButtonPolicyPort.create(req),
    onSuccess: () => { toast.success("Button Policy 등록"); qc.invalidateQueries({ queryKey: ["access-button-policy"] }); setBtnPolicyOpen(false); btnForm.reset(DEFAULT_BTN_POLICY); },
  });

  const deleteBtnPolicy = useMutation({
    mutationFn: (id: number) => accessButtonPolicyPort.delete(id),
    onSuccess: () => { toast.success("삭제됨"); qc.invalidateQueries({ queryKey: ["access-button-policy"] }); },
  });

  async function handleDeleteMenu(id: number) {
    const ok = await confirm({ title: "Menu Policy 삭제", description: "삭제하시겠습니까?", variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteMenuPolicy.mutate(id);
  }

  async function handleDeleteBtn(id: number) {
    const ok = await confirm({ title: "Button Policy 삭제", description: "삭제하시겠습니까?", variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteBtnPolicy.mutate(id);
  }

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
            <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setMenuPolicyOpen(true)}>신규</Button>
          </div>
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Menu Policy</span></div>
            <div className="list-wrap">
              {menuFetching ? <div style={{ padding: 16 }}>로딩 중...</div> : (
                <table className="grid" style={{ width: "100%" }}>
                  <thead><tr><th>ID</th><th>menuId</th><th>attributeKey</th><th>requiredValue</th><th></th></tr></thead>
                  <tbody>
                    {(menuPolicies ?? []).map((r) => (
                      <tr key={r.id}>
                        <td>{r.id}</td><td>{r.menuId}</td><td>{r.attributeKey}</td><td>{r.requiredValue}</td>
                        <td><button className="btn btn--danger btn--sm" onClick={() => handleDeleteMenu(r.id)}><Trash2 size={12} /></button></td>
                      </tr>
                    ))}
                    {!menuPolicies && <tr><td colSpan={5} style={{ textAlign: "center", color: "var(--ink-3)" }}>Menu ID를 입력하세요.</td></tr>}
                  </tbody>
                </table>
              )}
            </div>
          </div>
          <ModalShell isOpen={menuPolicyOpen} title="Menu Policy 등록">
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
            <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setBtnPolicyOpen(true)}>신규</Button>
          </div>
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Button Policy</span></div>
            <div className="list-wrap">
              {btnFetching ? <div style={{ padding: 16 }}>로딩 중...</div> : (
                <table className="grid" style={{ width: "100%" }}>
                  <thead><tr><th>ID</th><th>buttonId</th><th>attributeKey</th><th>requiredValue</th><th></th></tr></thead>
                  <tbody>
                    {(btnPolicies ?? []).map((r) => (
                      <tr key={r.id}>
                        <td>{r.id}</td><td>{r.buttonId}</td><td>{r.attributeKey}</td><td>{r.requiredValue}</td>
                        <td><button className="btn btn--danger btn--sm" onClick={() => handleDeleteBtn(r.id)}><Trash2 size={12} /></button></td>
                      </tr>
                    ))}
                    {!btnPolicies && <tr><td colSpan={5} style={{ textAlign: "center", color: "var(--ink-3)" }}>Button ID를 입력하세요.</td></tr>}
                  </tbody>
                </table>
              )}
            </div>
          </div>
          <ModalShell isOpen={btnPolicyOpen} title="Button Policy 등록">
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
