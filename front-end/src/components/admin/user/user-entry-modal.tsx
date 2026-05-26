"use client";

import { Controller } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { useUserForm } from "./use-user-form";
import { DynamicAttributeFields } from "./dynamic-attribute-fields";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

const ROLE_OPTIONS: Array<{ value: "ADMIN" | "USER"; label: string }> = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "USER", label: "USER" },
];

const MODULE_OPTIONS = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "FMS", label: "FMS" },
] as const;

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function UserEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const {
    form,
    dynamicAttrs,
    setDynamicAttrs,
    moduleAttributes,
    handleSave,
    handleDelete,
    isReadOnly,
    isBusy,
    isDetailLoading,
    detail,
  } = useUserForm(state, onSaved);

  const { register, control, handleSubmit } = form;

  return (
    <>
      {isDetailLoading ? (
        <div className="modal__loading">Loading...</div>
      ) : (
        <form onSubmit={handleSubmit(handleSave)} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            {isReadOnly && (
              <div style={{
                padding: "8px 12px",
                background: "var(--surface-2, #fef2f2)",
                border: "1px solid var(--border, #fecaca)",
                borderRadius: 4,
                color: "var(--danger, #dc2626)",
                fontSize: 13,
              }}>
                Deleted user (deleted at: {detail?.deletedAt ?? "—"}). Read only.
              </div>
            )}
            <div className="lcn">
              <span className="lcn__label">Username</span>
              <input
                className="box-panel"
                placeholder="Username"
                readOnly={isEdit || isReadOnly}
                style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
                {...register("username")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Email</span>
              <input
                className="box-panel"
                placeholder="Email (optional)"
                readOnly={isReadOnly}
                {...register("email")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Password</span>
              <input
                type="password"
                className="box-panel"
                placeholder={isEdit ? "Enter only to change" : "Min 8 characters"}
                readOnly={isReadOnly}
                {...register("password")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Role</span>
              <Controller
                name="role"
                control={control}
                render={({ field }) => (
                  <ComboBox
                    variant="panel"
                    options={ROLE_OPTIONS}
                    value={field.value}
                    onChange={field.onChange}
                    disabled={isReadOnly}
                  />
                )}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Module</span>
              <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                {MODULE_OPTIONS.map((opt) => (
                  <Controller
                    key={opt.value}
                    name="modules"
                    control={control}
                    render={({ field }) => (
                      <label style={{ display: "flex", alignItems: "center", gap: 4 }}>
                        <input
                          type="checkbox"
                          checked={field.value.includes(opt.value)}
                          disabled={isReadOnly}
                          onChange={(e) => {
                            const next = e.target.checked
                              ? [...field.value, opt.value]
                              : field.value.filter((v: string) => v !== opt.value);
                            field.onChange(next);
                          }}
                        />
                        {opt.label}
                      </label>
                    )}
                  />
                ))}
              </div>
            </div>
            <DynamicAttributeFields
              attributes={moduleAttributes}
              values={dynamicAttrs}
              onChange={setDynamicAttrs}
              disabled={isReadOnly}
            />
            <div className="lcn">
              <span className="lcn__label">Active</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" disabled={isReadOnly} {...register("active")} />
                Active
              </label>
            </div>
          </div>
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_USER_LIST_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_USER_LIST_UPDATE" : "BTN_ADMIN_USER_LIST_CREATE"}
          className="btn btn--modal btn--sm"
          onClick={handleSubmit(handleSave)}
          disabled={isBusy || isReadOnly}
        >
          저장
        </ActionButton>
        <Button size="sm" onClick={onClose} disabled={isBusy}>
          닫기
        </Button>
      </div>
    </>
  );
}

// ─── 외부 래퍼 (isOpen 가드 — false 시 unmount로 hook·캐시 초기화) ───────────
export function UserEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "사용자 수정" : "사용자 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <UserEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
