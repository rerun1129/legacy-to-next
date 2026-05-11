import { describe, it, expect, beforeEach } from 'vitest';
import { listFilterStore } from '../use-list-filter-store';

const SCOPE_A = '/fms/non-bl/list';
const SCOPE_B = '/fms/house-bl/list';

beforeEach(() => {
  // 각 테스트 전 inject 슬롯 초기화
  listFilterStore.getState().clearInject(SCOPE_A);
  listFilterStore.getState().clearInject(SCOPE_B);
});

describe('listFilterStore inject 슬롯', () => {
  it('setInject 후 getInject 시 저장한 값 반환', () => {
    listFilterStore.getState().setInject(SCOPE_A, { nonBlNo: 'TESTNO' });

    const result = listFilterStore.getState().getInject(SCOPE_A);

    expect(result).toEqual({ nonBlNo: 'TESTNO' });
  });

  it('clearInject 후 getInject 시 undefined 반환', () => {
    listFilterStore.getState().setInject(SCOPE_A, { nonBlNo: 'TESTNO' });
    listFilterStore.getState().clearInject(SCOPE_A);

    const result = listFilterStore.getState().getInject(SCOPE_A);

    expect(result).toBeUndefined();
  });

  it('scope별로 독립적으로 저장 및 조회', () => {
    listFilterStore.getState().setInject(SCOPE_A, { nonBlNo: 'NO_A' });
    listFilterStore.getState().setInject(SCOPE_B, { nonBlNo: 'NO_B' });

    expect(listFilterStore.getState().getInject(SCOPE_A)).toEqual({ nonBlNo: 'NO_A' });
    expect(listFilterStore.getState().getInject(SCOPE_B)).toEqual({ nonBlNo: 'NO_B' });
  });

  it('SCOPE_A clearInject가 SCOPE_B inject에 영향 없음', () => {
    listFilterStore.getState().setInject(SCOPE_A, { nonBlNo: 'NO_A' });
    listFilterStore.getState().setInject(SCOPE_B, { nonBlNo: 'NO_B' });

    listFilterStore.getState().clearInject(SCOPE_A);

    expect(listFilterStore.getState().getInject(SCOPE_A)).toBeUndefined();
    expect(listFilterStore.getState().getInject(SCOPE_B)).toEqual({ nonBlNo: 'NO_B' });
  });

  it('존재하지 않는 scope getInject 시 undefined 반환', () => {
    const result = listFilterStore.getState().getInject('/fms/not-exist');

    expect(result).toBeUndefined();
  });

  it('inject 슬롯과 search 슬롯은 독립 — inject가 search에 영향 없음', () => {
    listFilterStore.getState().setSearch(SCOPE_A, {
      extraFilter: { nonBlNo: 'PREV' },
      currentPage: 2,
      showAll: false,
    });

    listFilterStore.getState().setInject(SCOPE_A, { nonBlNo: 'INJECT' });
    listFilterStore.getState().clearInject(SCOPE_A);

    const search = listFilterStore.getState().getSearch(SCOPE_A);
    expect(search?.currentPage).toBe(2);
    expect(search?.showAll).toBe(false);
  });
});
