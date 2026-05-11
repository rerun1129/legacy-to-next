import type { ReactNode } from 'react';
import { create } from 'zustand';

export type ConfirmVariant = 'default' | 'destructive' | 'warning';

export interface ConfirmOptions {
  title: string;
  description?: string;
  variant?: ConfirmVariant;
  details?: Array<[string, ReactNode]>;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void | Promise<void>;
}

interface ConfirmState {
  open: boolean;
  options: ConfirmOptions | null;
  resolver: ((v: boolean) => void) | null;
  loading: boolean;
  openConfirm(opts: ConfirmOptions): Promise<boolean>;
  handleConfirm(): Promise<void>;
  handleCancel(): void;
}

function reset(set: (partial: Partial<ConfirmState>) => void) {
  set({ open: false, options: null, resolver: null, loading: false });
}

export const useConfirmStore = create<ConfirmState>((set, get) => ({
  open: false,
  options: null,
  resolver: null,
  loading: false,

  openConfirm(opts) {
    return new Promise<boolean>((resolve) => {
      set({ open: true, options: opts, resolver: resolve, loading: false });
    });
  },

  async handleConfirm() {
    const { options, resolver } = get();
    if (!options || !resolver) return;

    const { onConfirm } = options;

    if (!onConfirm) {
      resolver(true);
      reset(set);
      return;
    }

    set({ loading: true });
    try {
      await onConfirm();
      resolver(true);
      reset(set);
    } catch (e) {
      // onConfirm 실패 시 모달 유지하고 loading만 해제
      console.warn('[ConfirmModal] onConfirm threw an error:', e);
      set({ loading: false });
    }
  },

  handleCancel() {
    const { loading, resolver } = get();
    // 로딩 중 취소 무시 (await 깨짐 방지)
    if (loading) return;
    resolver?.(false);
    reset(set);
  },
}));

export const confirm = (opts: ConfirmOptions): Promise<boolean> =>
  useConfirmStore.getState().openConfirm(opts);
