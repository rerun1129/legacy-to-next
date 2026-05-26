"use client";

import { useMemo, useState } from "react";
import { ChevronDown, ChevronRight, Pencil, Trash2 } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import type { MenuRow } from "@/domain/access/menu";

function collectDescendantIds(nodeId: number, childMap: Map<number, MenuRow[]>): number[] {
  const children = childMap.get(nodeId) ?? [];
  return children.flatMap(c => [c.id, ...collectDescendantIds(c.id, childMap)]);
}

interface TreeNode {
  row: MenuRow;
  children: TreeNode[];
}

function buildTree(items: MenuRow[]): { roots: TreeNode[]; } {
  const nodeMap = new Map<number, TreeNode>();
  items.forEach(row => nodeMap.set(row.id, { row, children: [] }));

  const roots: TreeNode[] = [];
  items.forEach(row => {
    const node = nodeMap.get(row.id)!;
    if (row.parentId === null) {
      roots.push(node);
    } else {
      const parent = nodeMap.get(row.parentId);
      if (parent) {
        parent.children.push(node);
      } else {
        // parentId가 있지만 부모가 같은 모듈 그룹에 없을 수 있으므로 root로 처리
        roots.push(node);
      }
    }
  });

  roots.sort((a, b) => (a.row.sortOrder ?? 0) - (b.row.sortOrder ?? 0));
  nodeMap.forEach(node => {
    node.children.sort((a, b) => (a.row.sortOrder ?? 0) - (b.row.sortOrder ?? 0));
  });

  return { roots };
}

interface TreeRowProps {
  node: TreeNode;
  level: number;
  expandedNodes: Set<number>;
  selectedKeys: Set<number>;
  childMap: Map<number, MenuRow[]>;
  onToggleNode: (id: number) => void;
  onSelectionChange: (next: Set<number>) => void;
  onEdit: (row: MenuRow) => void;
  onDelete: (id: number, label: string) => void;
  deleteIsPending: boolean;
}

function TreeRow({
  node,
  level,
  expandedNodes,
  selectedKeys,
  childMap,
  onToggleNode,
  onSelectionChange,
  onEdit,
  onDelete,
  deleteIsPending,
}: TreeRowProps) {
  const { row } = node;
  const isParent = row.path === null;
  const isExpanded = expandedNodes.has(row.id);
  const isSelected = selectedKeys.has(row.id);
  const paddingLeft = 8 + level * 16;

  return (
    <>
      <div
        className={`tree-row${isSelected ? " tree-row--selected" : ""}`}
        style={{ paddingLeft }}
      >
        <input
          type="checkbox"
          className="chk"
          checked={isSelected}
          onChange={() => {
            const descendantIds = collectDescendantIds(row.id, childMap);
            const allIds = [row.id, ...descendantIds];
            const next = new Set(selectedKeys);
            if (next.has(row.id)) {
              allIds.forEach(id => next.delete(id));
            } else {
              allIds.forEach(id => next.add(id));
            }
            onSelectionChange(next);
          }}
          onClick={(e) => e.stopPropagation()}
          style={{ flexShrink: 0 }}
        />
        {isParent ? (
          <button
            className="tree-row__toggle"
            onClick={() => onToggleNode(row.id)}
            aria-label={isExpanded ? "접기" : "펼치기"}
          >
            {isExpanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
          </button>
        ) : (
          <span className="tree-row__leaf-indent" />
        )}
        <span className="tree-row__code">{row.menuCode}</span>
        <span className="tree-row__label">{row.label}</span>
        {row.path && (
          <span className="tree-row__path">{row.path}</span>
        )}
        <span className={`tree-row__badge${row.active ? " tree-row__badge--active" : " tree-row__badge--inactive"}`}>
          {row.active ? "활성" : "비활성"}
        </span>
        <div className="tree-row__actions" onClick={(e) => e.stopPropagation()}>
          <ActionButton
            buttonCode="BTN_ADMIN_ACCESS_MENU_UPDATE"
            className="btn btn--sm"
            onClick={() => onEdit(row)}
          >
            <Pencil size={12} />
          </ActionButton>
          <ActionButton
            buttonCode="BTN_ADMIN_ACCESS_MENU_DELETE"
            className="btn btn--danger btn--sm"
            onClick={() => onDelete(row.id, row.label)}
            disabled={deleteIsPending}
          >
            <Trash2 size={12} />
          </ActionButton>
        </div>
      </div>
      {isParent && isExpanded && node.children.map(child => (
        <TreeRow
          key={child.row.id}
          node={child}
          level={level + 1}
          expandedNodes={expandedNodes}
          selectedKeys={selectedKeys}
          childMap={childMap}
          onToggleNode={onToggleNode}
          onSelectionChange={onSelectionChange}
          onEdit={onEdit}
          onDelete={onDelete}
          deleteIsPending={deleteIsPending}
        />
      ))}
    </>
  );
}

export interface MenuTreeViewProps {
  rows: MenuRow[];
  selectedKeys: Set<number>;
  deleteIsPending: boolean;
  onSelectionChange: (next: Set<number>) => void;
  onEdit: (row: MenuRow) => void;
  onDelete: (id: number, label: string) => void;
}

export function MenuTreeView({
  rows,
  selectedKeys,
  deleteIsPending,
  onSelectionChange,
  onEdit,
  onDelete,
}: MenuTreeViewProps) {
  // grouped를 먼저 계산해 모듈 목록 파악
  const grouped = useMemo(() => {
    const map = new Map<string, MenuRow[]>();
    rows.forEach(row => {
      const list = map.get(row.moduleCode) ?? [];
      list.push(row);
      map.set(row.moduleCode, list);
    });
    return map;
  }, [rows]);

  // 부모→자식 id 맵: 체크박스 하위 일괄 토글에 사용
  const childMap = useMemo(() => {
    const map = new Map<number, MenuRow[]>();
    rows.forEach(row => {
      if (row.parentId !== null) {
        const list = map.get(row.parentId) ?? [];
        list.push(row);
        map.set(row.parentId, list);
      }
    });
    return map;
  }, [rows]);

  // 초기값: 모든 모듈 펼침, 노드는 접힘
  const [expandedModules, setExpandedModules] = useState<Set<string>>(
    () => new Set(rows.map(r => r.moduleCode))
  );
  const [expandedNodes, setExpandedNodes] = useState<Set<number>>(new Set());

  function toggleModule(code: string) {
    setExpandedModules(prev => {
      const next = new Set(prev);
      if (next.has(code)) next.delete(code);
      else next.add(code);
      return next;
    });
  }

  function toggleNode(id: number) {
    setExpandedNodes(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  const treesByModule = useMemo(() => {
    const result = new Map<string, ReturnType<typeof buildTree>>();
    grouped.forEach((items, code) => {
      result.set(code, buildTree(items));
    });
    return result;
  }, [grouped]);

  const moduleSorted = useMemo(
    () => [...grouped.keys()].sort(),
    [grouped]
  );

  return (
    <div className="menu-tree">
      {moduleSorted.map(moduleCode => {
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
              <span className="menu-tree__module-count">{grouped.get(moduleCode)?.length ?? 0}</span>
            </div>
            {isModuleExpanded && tree.roots.map(node => (
              <TreeRow
                key={node.row.id}
                node={node}
                level={0}
                expandedNodes={expandedNodes}
                selectedKeys={selectedKeys}
                childMap={childMap}
                onToggleNode={toggleNode}
                onSelectionChange={onSelectionChange}
                onEdit={onEdit}
                onDelete={onDelete}
                deleteIsPending={deleteIsPending}
              />
            ))}
          </div>
        );
      })}
    </div>
  );
}
