"use client";

import { forwardRef, useImperativeHandle, useMemo, useState } from "react";
import { Minus, ChevronDown, ChevronRight } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/shared/button";
import type { UseFormRegister, Control } from "react-hook-form";
import { MenuRowCells } from "./menu-grid-columns";
import type { MenuFormRow, MenuFormValues } from "./menu-list-helpers";
import { MenuTreeHeader } from "./menu-tree-header";

// ─── 트리 빌더 ────────────────────────────────────────────────────────────────

interface TreeNode {
  row: MenuFormRow;
  children: TreeNode[];
}

function buildTree(items: MenuFormRow[]): TreeNode[] {
  const nodeMap = new Map<number, TreeNode>();
  items.forEach((row) => nodeMap.set(row.entityId, { row, children: [] }));

  const roots: TreeNode[] = [];
  items.forEach((row) => {
    const node = nodeMap.get(row.entityId)!;
    if (row.parentId === null) {
      roots.push(node);
    } else {
      const parent = nodeMap.get(row.parentId);
      if (parent) {
        parent.children.push(node);
      } else {
        roots.push(node);
      }
    }
  });

  roots.sort((a, b) => (a.row.sortOrder ?? 0) - (b.row.sortOrder ?? 0));
  nodeMap.forEach((node) => {
    node.children.sort((a, b) => (a.row.sortOrder ?? 0) - (b.row.sortOrder ?? 0));
  });

  return roots;
}

function collectDescendantEntityIds(
  entityId: number,
  childMap: Map<number, MenuFormRow[]>,
): number[] {
  const children = childMap.get(entityId) ?? [];
  return children.flatMap((c) => [
    c.entityId,
    ...collectDescendantEntityIds(c.entityId, childMap),
  ]);
}

// ─── 행 렌더 ──────────────────────────────────────────────────────────────────

type MsgT = ReturnType<typeof useTranslations>;
type OptionsT = (key: string) => string;

interface TreeRowProps {
  node: TreeNode;
  level: number;
  expandedNodes: Set<number>;
  selectedKeys: Set<number>;
  childMap: Map<number, MenuFormRow[]>;
  indexByEntityId: Map<number, number>;
  register: UseFormRegister<MenuFormValues>;
  control: Control<MenuFormValues>;
  moduleOptions: { value: string; label: string }[];
  tMsg: MsgT;
  tOptions: OptionsT;
  onToggleNode: (entityId: number) => void;
  onSelectionChange: (next: Set<number>) => void;
  onRemoveNewRow: (entityId: number) => void;
}

function TreeRow({
  node,
  level,
  expandedNodes,
  selectedKeys,
  childMap,
  indexByEntityId,
  register,
  control,
  moduleOptions,
  tMsg,
  tOptions,
  onToggleNode,
  onSelectionChange,
  onRemoveNewRow,
}: TreeRowProps) {
  const { row } = node;
  const isParent = row.path === null && row.entityId > 0;
  const isNew = row.entityId < 0;
  const isExpanded = expandedNodes.has(row.entityId);
  const isSelected = selectedKeys.has(row.entityId);
  const idx = indexByEntityId.get(row.entityId) ?? -1;

  // 들여쓰기는 첫 컬럼(menuCode) 내부에 적용.
  // 토글/리프 영역을 고정 20px로, 앞에 level*16px 스페이서를 두어
  // level=0 기준으로 헤더와 정렬되며, 깊은 레벨에서도 2번째 셀부터는 고정 위치 유지.
  const indentPx = level * 16;

  function handleCheck() {
    const descendantIds = collectDescendantEntityIds(row.entityId, childMap);
    const allIds = [row.entityId, ...descendantIds];
    const next = new Set(selectedKeys);
    if (next.has(row.entityId)) {
      allIds.forEach((id) => next.delete(id));
    } else {
      allIds.forEach((id) => next.add(id));
    }
    onSelectionChange(next);
  }

  return (
    <>
      <div
        className={`tree-row${isSelected ? " tree-row--selected" : ""}${isNew ? " is-new" : ""}`}
        style={{ paddingLeft: 8, display: "flex", alignItems: "center", gap: 4, minHeight: 32 }}
      >
        {/* 체크박스: 헤더 체크박스 스페이서(28px)와 매칭 */}
        <input
          type="checkbox"
          className="chk"
          checked={isSelected}
          onChange={handleCheck}
          onClick={(e) => e.stopPropagation()}
          style={{ flexShrink: 0, width: 20 }}
        />

        {/* 들여쓰기 스페이서: level*16px, 첫 컬럼 내부 들여쓰기 구현 */}
        {indentPx > 0 && (
          <span style={{ display: "inline-block", width: indentPx, flexShrink: 0 }} />
        )}

        {/* 토글 또는 리프 인덴트: 헤더 토글 스페이서(20px)와 매칭 */}
        {isParent ? (
          <button
            className="tree-row__toggle"
            onClick={() => onToggleNode(row.entityId)}
            aria-label={isExpanded ? tMsg("collapse") : tMsg("expand")}
            type="button"
            style={{ flexShrink: 0 }}
          >
            {isExpanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
          </button>
        ) : (
          <span
            className="tree-row__leaf-indent"
            style={{ display: "inline-block", width: 20, flexShrink: 0 }}
          />
        )}

        {/* 셀 그룹: MenuRowCells(menuCode 포함 전체) */}
        {idx >= 0 && (
          <div style={{ display: "flex", alignItems: "center", gap: 4, flexWrap: "wrap" }}>
            <MenuRowCells
              row={row}
              idx={idx}
              register={register}
              control={control}
              moduleOptions={moduleOptions}
              tOptions={tOptions}
            />
          </div>
        )}

        {/* 신규 행(entityId<0)에만 제거 버튼 노출 */}
        {isNew && (
          <div onClick={(e) => e.stopPropagation()}>
            <Button
              variant="danger"
              size="sm"
              iconOnly
              type="button"
              onClick={() => onRemoveNewRow(row.entityId)}
            >
              <Minus size={12} />
            </Button>
          </div>
        )}
      </div>
      {isParent && isExpanded &&
        node.children.map((child) => (
          <TreeRow
            key={child.row.entityId}
            node={child}
            level={level + 1}
            expandedNodes={expandedNodes}
            selectedKeys={selectedKeys}
            childMap={childMap}
            indexByEntityId={indexByEntityId}
            register={register}
            control={control}
            moduleOptions={moduleOptions}
            tMsg={tMsg}
            tOptions={tOptions}
            onToggleNode={onToggleNode}
            onSelectionChange={onSelectionChange}
            onRemoveNewRow={onRemoveNewRow}
          />
        ))}
    </>
  );
}

// ─── 공개 인터페이스 ──────────────────────────────────────────────────────────

export interface MenuTreeHandle {
  expandAll(): void;
  collapseAll(): void;
}

export interface MenuTreeViewProps {
  rows: MenuFormRow[];
  indexByEntityId: Map<number, number>;
  register: UseFormRegister<MenuFormValues>;
  control: Control<MenuFormValues>;
  moduleOptions: { value: string; label: string }[];
  selectedKeys: Set<number>;
  onSelectionChange: (next: Set<number>) => void;
  onRemoveNewRow: (entityId: number) => void;
}

export const MenuTreeView = forwardRef<MenuTreeHandle, MenuTreeViewProps>(
  function MenuTreeView(
    {
      rows,
      indexByEntityId,
      register,
      control,
      moduleOptions,
      selectedKeys,
      onSelectionChange,
      onRemoveNewRow,
    },
    ref,
  ) {
    // useTranslations는 early-return 이전에 무조건 호출 (Rules of Hooks)
    const tMsg = useTranslations("admin.menu.msg");
    const tOptions = useTranslations("admin.menu.options");

    const grouped = useMemo(() => {
      const map = new Map<string, MenuFormRow[]>();
      rows.forEach((row) => {
        const list = map.get(row.moduleCode) ?? [];
        list.push(row);
        map.set(row.moduleCode, list);
      });
      return map;
    }, [rows]);

    const childMap = useMemo(() => {
      const map = new Map<number, MenuFormRow[]>();
      rows.forEach((row) => {
        if (row.parentId !== null) {
          const list = map.get(row.parentId) ?? [];
          list.push(row);
          map.set(row.parentId, list);
        }
      });
      return map;
    }, [rows]);

    const [expandedModules, setExpandedModules] = useState<Set<string>>(
      () => new Set(rows.map((r) => r.moduleCode)),
    );
    const [expandedNodes, setExpandedNodes] = useState<Set<number>>(new Set());

    const treesByModule = useMemo(() => {
      const result = new Map<string, TreeNode[]>();
      grouped.forEach((items, code) => {
        result.set(code, buildTree(items));
      });
      return result;
    }, [grouped]);

    const moduleSorted = useMemo(() => [...grouped.keys()].sort(), [grouped]);

    // expandAll: 모든 모듈 펼치기 + 모든 부모 노드(path===null && entityId>0) 펼치기
    // collapseAll: 모듈·노드 모두 접기
    useImperativeHandle(ref, () => ({
      expandAll() {
        setExpandedModules(new Set(rows.map((r) => r.moduleCode)));
        const parentIds = new Set(
          rows
            .filter((r) => r.path === null && r.entityId > 0)
            .map((r) => r.entityId),
        );
        setExpandedNodes(parentIds);
      },
      collapseAll() {
        setExpandedModules(new Set());
        setExpandedNodes(new Set());
      },
    }), [rows]);

    function toggleModule(code: string) {
      setExpandedModules((prev) => {
        const next = new Set(prev);
        if (next.has(code)) next.delete(code);
        else next.add(code);
        return next;
      });
    }

    function toggleNode(entityId: number) {
      setExpandedNodes((prev) => {
        const next = new Set(prev);
        if (next.has(entityId)) next.delete(entityId);
        else next.add(entityId);
        return next;
      });
    }

    return (
      <div className="menu-tree">
        <MenuTreeHeader />
        {moduleSorted.map((moduleCode) => {
          const isModuleExpanded = expandedModules.has(moduleCode);
          const tree = treesByModule.get(moduleCode);
          if (!tree) return null;

          return (
            <div key={moduleCode} className="menu-tree__module">
              <div
                className="menu-tree__module-header"
                onClick={() => toggleModule(moduleCode)}
              >
                {isModuleExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                <span>{moduleCode}</span>
                <span className="menu-tree__module-count">
                  {grouped.get(moduleCode)?.length ?? 0}
                </span>
              </div>
              {isModuleExpanded &&
                tree.map((node) => (
                  <TreeRow
                    key={node.row.entityId}
                    node={node}
                    level={0}
                    expandedNodes={expandedNodes}
                    selectedKeys={selectedKeys}
                    childMap={childMap}
                    indexByEntityId={indexByEntityId}
                    register={register}
                    control={control}
                    moduleOptions={moduleOptions}
                    tMsg={tMsg}
                    tOptions={tOptions}
                    onToggleNode={toggleNode}
                    onSelectionChange={onSelectionChange}
                    onRemoveNewRow={onRemoveNewRow}
                  />
                ))}
            </div>
          );
        })}
      </div>
    );
  },
);
