import { create } from "zustand";

interface BLDraftState {
  drafts: Record<string, unknown>;
  getDraft: (key: string) => unknown;
  setDraft: (key: string, values: unknown) => void;
  clearDraft: (key: string) => void;
}

// persist 없음 — 새로고침 시 자동 초기화, 메뉴 이동 후 복귀 시 유지
const useBLDraftStore = create<BLDraftState>((set, get) => ({
  drafts: {},

  getDraft(key) {
    return get().drafts[key];
  },

  setDraft(key, values) {
    set((state) => ({
      drafts: { ...state.drafts, [key]: values },
    }));
  },

  clearDraft(key) {
    set((state) => {
      const next = { ...state.drafts };
      delete next[key];
      return { drafts: next };
    });
  },
}));

// React 외부에서 직접 getState 접근이 필요할 때 사용
const blDraftStore = useBLDraftStore;

export { useBLDraftStore, blDraftStore };
