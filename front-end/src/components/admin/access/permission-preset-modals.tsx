"use client";

import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import type { UseFormReturn } from "react-hook-form";
import type { PermissionPresetSummary } from "@/domain/access/permission-preset";

export interface CreateFormValues {
  code: string;
  name: string;
  description: string;
  active: boolean;
}

export interface UpdateFormValues {
  name: string;
  description: string;
  active: boolean;
}

interface CreateModalProps {
  isOpen: boolean;
  form: UseFormReturn<CreateFormValues>;
  onSave: (values: CreateFormValues) => void;
  onClose: () => void;
  isPending: boolean;
}

interface UpdateModalProps {
  editTarget: PermissionPresetSummary | null;
  form: UseFormReturn<UpdateFormValues>;
  onSave: (values: UpdateFormValues) => void;
  onClose: () => void;
  isPending: boolean;
}

export function PermissionPresetCreateModal({ isOpen, form, onSave, onClose, isPending }: CreateModalProps) {
  return (
    <ModalShell isOpen={isOpen} title="Permission Preset 등록" size="md">
      <form onSubmit={form.handleSubmit(onSave)} className="modal__body">
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <div className="lcn">
            <span className="lcn__label">code</span>
            <input className="text-box text-box--panel" placeholder="PRESET_*" {...form.register("code")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">name</span>
            <input className="text-box text-box--panel" {...form.register("name")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">description</span>
            <input className="text-box text-box--panel" {...form.register("description")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">활성</span>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input type="checkbox" {...form.register("active")} />활성
            </label>
          </div>
        </div>
      </form>
      <div className="modal__footer">
        <Button variant="modal" size="sm" onClick={form.handleSubmit(onSave)} loading={isPending}>
          저장
        </Button>
        <Button size="sm" onClick={onClose}>닫기</Button>
      </div>
    </ModalShell>
  );
}

export function PermissionPresetUpdateModal({ editTarget, form, onSave, onClose, isPending }: UpdateModalProps) {
  return (
    <ModalShell isOpen={editTarget !== null} title="Permission Preset 수정" size="md">
      <form onSubmit={form.handleSubmit(onSave)} className="modal__body">
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <div className="lcn">
            <span className="lcn__label">code</span>
            <span
              className="text-box text-box--panel"
              style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}
            >
              {editTarget?.code}
            </span>
          </div>
          <div className="lcn">
            <span className="lcn__label">name</span>
            <input className="text-box text-box--panel" {...form.register("name")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">description</span>
            <input className="text-box text-box--panel" {...form.register("description")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">활성</span>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input type="checkbox" {...form.register("active")} />활성
            </label>
          </div>
        </div>
      </form>
      <div className="modal__footer">
        <Button variant="modal" size="sm" onClick={form.handleSubmit(onSave)} loading={isPending}>
          저장
        </Button>
        <Button size="sm" onClick={onClose}>닫기</Button>
      </div>
    </ModalShell>
  );
}
