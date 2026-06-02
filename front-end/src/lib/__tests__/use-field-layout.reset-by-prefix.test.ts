import { describe, it, expect, beforeEach } from 'vitest';
import { useFieldLayout } from '../use-field-layout';

beforeEach(() => {
  useFieldLayout.setState({ layouts: {} });
});

describe('useFieldLayout.resetByPrefix', () => {
  it('prefix에 해당하는 키만 제거하고 나머지는 보존한다', () => {
    useFieldLayout.setState({
      layouts: {
        'A::x': { order: [], hidden: [] },
        'A::y': { order: [], hidden: [] },
        'B::z': { order: [], hidden: [] },
      },
    });

    useFieldLayout.getState().resetByPrefix('A::');

    expect(Object.keys(useFieldLayout.getState().layouts)).toEqual(['B::z']);
  });

  it('prefix가 일치하는 키가 없으면 layouts에 변경 없음', () => {
    useFieldLayout.setState({
      layouts: {
        'B::z': { order: [], hidden: [] },
      },
    });

    useFieldLayout.getState().resetByPrefix('A::');

    expect(Object.keys(useFieldLayout.getState().layouts)).toEqual(['B::z']);
  });

  it('prefix가 모든 키와 일치하면 layouts가 빈 객체가 된다', () => {
    useFieldLayout.setState({
      layouts: {
        'A::x': { order: [], hidden: [] },
        'A::y': { order: [], hidden: [] },
      },
    });

    useFieldLayout.getState().resetByPrefix('A::');

    expect(Object.keys(useFieldLayout.getState().layouts)).toHaveLength(0);
  });
});
