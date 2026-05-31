import { create } from "zustand";

export interface SavedSearchState {
  extraFilter?: unknown;
  currentPage?: number;
  pageSize?: number;
  [key: string]: unknown;
}

interface ListFilterState {
  filters: Record<string, unknown>;
  searches: Record<string, SavedSearchState>;
  // SPA 라우팅 시 화면 간 일회성 데이터 전달 슬롯 — 수신 측에서 읽은 즉시 clearInject
  injects: Record<string, unknown>;
  getFilter: (scope: string) => unknown;
  setFilter: (scope: string, values: unknown) => void;
  clearFilter: (scope: string) => void;
  getSearch: (scope: string) => SavedSearchState | undefined;
  setSearch: (scope: string, values: SavedSearchState) => void;
  setInject: (scope: string, value: unknown) => void;
  getInject: (scope: string) => unknown | undefined;
  clearInject: (scope: string) => void;
}

// persist 없음 — 새로고침 시 자동 초기화, 메뉴 이동 후 복귀 시 유지
const useListFilterStore = create<ListFilterState>((set, get) => ({
  filters: {},
  searches: {},
  injects: {},

  getFilter(scope) {
    return get().filters[scope];
  },

  setFilter(scope, values) {
    set((state) => ({ filters: { ...state.filters, [scope]: values } }));
  },

  clearFilter(scope) {
    set((state) => {
      const nextFilters = { ...state.filters };
      const nextSearches = { ...state.searches };
      delete nextFilters[scope];
      delete nextSearches[scope];
      return { filters: nextFilters, searches: nextSearches };
    });
  },

  getSearch(scope) {
    return get().searches[scope];
  },

  setSearch(scope, values) {
    set((state) => ({ searches: { ...state.searches, [scope]: values } }));
  },

  setInject(scope, value) {
    set((state) => ({ injects: { ...state.injects, [scope]: value } }));
  },

  getInject(scope) {
    return get().injects[scope];
  },

  clearInject(scope) {
    set((state) => {
      const nextInjects = { ...state.injects };
      delete nextInjects[scope];
      return { injects: nextInjects };
    });
  },
}));

// React 외부에서 직접 getState 접근이 필요할 때 사용
const listFilterStore = useListFilterStore;

export { useListFilterStore, listFilterStore };
