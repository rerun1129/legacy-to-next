"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import type { ModuleRow, UpdateModuleDto } from "@/domain/access/module";

interface Props {
  target: ModuleRow | null;
  isPending: boolean;
  onSave: (moduleCode: string, req: UpdateModuleDto) => void;
  onClose: () => void;
}

interface FormValues {
  name: string;
  description: string;
  sortOrder: string;
  active: boolean;
}

const DEFAULT: FormValues = {
  name: "",
  description: "",
  sortOrder: "",
  active: true,
};

function parseNullableStr(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function parseNullableNum(v: string): number | null {
  if (!v.trim()) return null;
  const n = Number(v);
  return isNaN(n) ? null : n;
}

export function ModuleUpdateModal({ target, isPending, onSave, onClose }: Props) {
  const form = useForm<FormValues>({ defaultValues: DEFAULT });

  useEffect(() => {
    if (target) {
      form.reset({
        name: target.name,
        description: target.description ?? "",
        sortOrder: target.sortOrder != null ? String(target.sortOrder) : "",
        active: target.active,
      });
    } else {
      form.reset(DEFAULT);
    }
  }, [target, form]);

  function handleSave(values: FormValues) {
    if (!target) return;
    const req: UpdateModuleDto = {
      name: values.name.trim(),
      description: parseNullableStr(values.description),
      sortOrder: parseNullableNum(values.sortOrder),
      active: values.active,
    };
    onSave(target.moduleCode, req);
  }

  return (
    <ModalShell isOpen={target !== null} title="모듈 수정" size="md">
      <form onSubmit={form.handleSubmit(handleSave)} className="modal__body">
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <div className="lcn">
            <span className="lcn__label">moduleCode</span>
            <span
              className="text-box text-box--panel"
              style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}
            >
              {target?.moduleCode}
            </span>
          </div>
          <div className="lcn">
            <span className="lcn__label">name</span>
            <input className="text-box text-box--panel" {...form.register("name")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">description</span>
            <input className="text-box text-box--panel" placeholder="선택" {...form.register("description")} />
          </div>
          <div className="lcn">
            <span className="lcn__label">sortOrder</span>
            <input
              type="number"
              className="text-box text-box--panel"
              placeholder="선택"
              {...form.register("sortOrder")}
            />
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
        <Button
          variant="modal"
          size="sm"
          onClick={form.handleSubmit(handleSave)}
          loading={isPending}
        >
          저장
        </Button>
        <Button size="sm" onClick={onClose}>
          닫기
        </Button>
      </div>
    </ModalShell>
  );
}
