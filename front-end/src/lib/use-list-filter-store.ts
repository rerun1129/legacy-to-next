import { create } from "zustand";

interface ListFilterState {
  filters: Record<string, unknown>;
  getFilter: (scope: string) => unknown;
  setFilter: (scope: string, values: unknown) => void;
  clearFilter: (scope: string) => void;
}

// persist 없음 — 새로고침 시 자동 초기화, 메뉴 이동 후 복귀 시 유지
const useListFilterStore = create<ListFilterState>((set, get) => ({
  filters: {},

  getFilter(scope) {
    return get().filters[scope];
  },

  setFilter(scope, values) {
    set((state) => ({ filters: { ...state.filters, [scope]: values } }));
  },

  clearFilter(scope) {
    set((state) => {
      const next = { ...state.filters };
      delete next[scope];
      return { filters: next };
    });
  },
}));

// React 외부에서 직접 getState 접근이 필요할 때 사용
const listFilterStore = useListFilterStore;

export { useListFilterStore, listFilterStore };
