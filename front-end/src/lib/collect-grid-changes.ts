export interface GridChanges<C, U> {
  creates: C[];
  updates: U[];
  deleteIds: number[];
}

export function collectGridChanges<T, C, U>(
  original: T[],
  current: T[],
  options: {
    rowKey: (row: T) => number;
    toCreate: (row: T) => C;
    toUpdate: (row: T) => U;
    isEqual: (a: T, b: T) => boolean;
  }
): GridChanges<C, U> {
  const { rowKey, toCreate, toUpdate, isEqual } = options;
  const originalMap = new Map(original.map((row) => [rowKey(row), row]));
  const currentKeys = new Set(current.map((row) => rowKey(row)));

  const creates: C[] = [];
  const updates: U[] = [];
  const deleteIds: number[] = [];

  for (const row of current) {
    const key = rowKey(row);
    if (key < 0) {
      creates.push(toCreate(row));
    } else {
      const orig = originalMap.get(key);
      if (orig && !isEqual(orig, row)) {
        updates.push(toUpdate(row));
      }
    }
  }

  for (const orig of original) {
    if (!currentKeys.has(rowKey(orig))) {
      deleteIds.push(rowKey(orig));
    }
  }

  return { creates, updates, deleteIds };
}
