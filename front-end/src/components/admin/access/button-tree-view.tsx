"use client";

import { useMemo, useState } from "react";
import { ChevronDown, ChevronRight, Pencil, Trash2 } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import type { MenuRow } from "@/domain/access/menu";
import type { ButtonRow } from "@/domain/access/button";

interface TreeNode { row: MenuRow; children: TreeNode[]; }

function buildTree(items: MenuRow[]): TreeNode[] {
  const nodeMap = new Map<number, TreeNode>();
  items.forEach(row => nodeMap.set(row.id, { row, children: [] }));
  const roots: TreeNode[] = [];
  items.forEach(row => {
    const node = nodeMap.get(row.id)!;
    if (row.parentId === null) { roots.push(node); return; }
    const parent = nodeMap.get(row.parentId);
    if (parent) parent.children.push(node); else roots.push(node);
  });
  const bySortOrder = (a: TreeNode, b: TreeNode) => (a.row.sortOrder ?? 0) - (b.row.sortOrder ?? 0);
  roots.sort(bySortOrder);
  nodeMap.forEach(n => n.children.sort(bySortOrder));
  return roots;
}

// 해당 메뉴 + 하위 메뉴들에 소속된 모든 버튼 id 수집
function collectButtonIds(
  menuId: number,
  childMenuMap: Map<number, MenuRow[]>,
  buttonsByMenu: Map<number, ButtonRow[]>
): number[] {
  const own = (buttonsByMenu.get(menuId) ?? []).map(b => b.id);
  const childIds = (childMenuMap.get(menuId) ?? []).flatMap(c =>
    collectButtonIds(c.id, childMenuMap, buttonsByMenu)
  );
  return [...own, ...childIds];
}

const BADGE: Record<string, React.CSSProperties> = {
  CREATE: { background: "var(--accent-2)", color: "var(--accent-9)" },
  UPDATE: { background: "var(--blue-2)", color: "var(--blue-9)" },
  DELETE: { background: "var(--red-2)", color: "var(--red-9)" },
  EXPORT: { background: "var(--green-2)", color: "var(--green-9)" },
  CUSTOM: { background: "var(--surface-3)", color: "var(--ink-2)" },
};
const BADGE_BASE: React.CSSProperties = { fontSize: 11, padding: "1px 5px", borderRadius: 3 };

interface ButtonNodeProps {
  btn: ButtonRow; level: number; selectedKeys: Set<number>;
  onSelectionChange: (next: Set<number>) => void;
  onEdit: (row: ButtonRow) => void;
  onDelete: (id: number, label: string) => void;
}

function ButtonNode({ btn, level, selectedKeys, onSelectionChange, onEdit, onDelete }: ButtonNodeProps) {
  const paddingLeft = 8 + level * 16 + 8;
  const isSelected = selectedKeys.has(btn.id);
  return (
    <div
      className={`tree-row${isSelected ? " tree-row--selected" : ""}`}
      style={{ paddingLeft, background: isSelected ? undefined : "var(--surface-1)" }}
    >
      <input type="checkbox" className="chk" checked={isSelected}
        onChange={() => {
          const next = new Set(selectedKeys);
          if (next.has(btn.id)) next.delete(btn.id); else next.add(btn.id);
          onSelectionChange(next);
        }}
        onClick={(e) => e.stopPropagation()} style={{ flexShrink: 0 }}
      />
      <span className="tree-row__leaf-indent" />
      <span className="tree-row__code" style={{ fontSize: 12 }}>{btn.buttonCode}</span>
      <span className="tree-row__label" style={{ fontSize: 12 }}>{btn.label}</span>
      <span className="tree-row__badge" style={{ ...(BADGE[btn.actionType] ?? BADGE.CUSTOM), ...BADGE_BASE }}>
        {btn.actionType}
      </span>
      <span className={`tree-row__badge--${btn.active ? "active" : "inactive"}`}>
        {btn.active ? "활성" : "비활성"}
      </span>
      <div className="tree-row__actions" onClick={(e) => e.stopPropagation()}>
        <ActionButton buttonCode="BTN_ADMIN_ACCESS_BUTTON_UPDATE" className="btn btn--sm" onClick={() => onEdit(btn)}>
          <Pencil size={12} />
        </ActionButton>
        <ActionButton buttonCode="BTN_ADMIN_ACCESS_BUTTON_DELETE" className="btn btn--danger btn--sm" onClick={() => onDelete(btn.id, btn.label)}>
          <Trash2 size={12} />
        </ActionButton>
      </div>
    </div>
  );
}

interface MenuNodeProps {
  node: TreeNode; level: number;
  expandedNodes: Set<number>; selectedKeys: Set<number>;
  childMenuMap: Map<number, MenuRow[]>; buttonsByMenu: Map<number, ButtonRow[]>;
  onToggleNode: (id: number) => void;
  onSelectionChange: (next: Set<number>) => void;
  onEdit: (row: ButtonRow) => void;
  onDelete: (id: number, label: string) => void;
}

function MenuNode({ node, level, expandedNodes, selectedKeys, childMenuMap, buttonsByMenu, onToggleNode, onSelectionChange, onEdit, onDelete }: MenuNodeProps) {
  const { row } = node;
  const isExpanded = expandedNodes.has(row.id);
  const paddingLeft = 8 + level * 16;
  const menuButtonIds = collectButtonIds(row.id, childMenuMap, buttonsByMenu);
  const hasButtons = menuButtonIds.length > 0;
  const isChecked = hasButtons && menuButtonIds.every(id => selectedKeys.has(id));
  const isIndeterminate = !isChecked && menuButtonIds.some(id => selectedKeys.has(id));
  const leafButtons = row.path !== null ? (buttonsByMenu.get(row.id) ?? []) : [];

  return (
    <>
      <div className="tree-row" style={{ paddingLeft, background: "var(--surface-2)" }}>
        <input type="checkbox" className="chk" checked={isChecked}
          ref={(el) => { if (el) el.indeterminate = isIndeterminate; }}
          onChange={() => {
            const next = new Set(selectedKeys);
            if (isChecked) menuButtonIds.forEach(id => next.delete(id));
            else menuButtonIds.forEach(id => next.add(id));
            onSelectionChange(next);
          }}
          onClick={(e) => e.stopPropagation()} style={{ flexShrink: 0 }}
        />
        <button className="tree-row__toggle" onClick={() => onToggleNode(row.id)} aria-label={isExpanded ? "접기" : "펼치기"}>
          {isExpanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
        </button>
        <span className="tree-row__code">{row.menuCode}</span>
        <span className="tree-row__label">{row.label}</span>
      </div>
      {isExpanded && (
        <>
          {node.children.map(child => (
            <MenuNode key={child.row.id} node={child} level={level + 1}
              expandedNodes={expandedNodes} selectedKeys={selectedKeys}
              childMenuMap={childMenuMap} buttonsByMenu={buttonsByMenu}
              onToggleNode={onToggleNode} onSelectionChange={onSelectionChange}
              onEdit={onEdit} onDelete={onDelete}
            />
          ))}
          {leafButtons.map(btn => (
            <ButtonNode key={btn.id} btn={btn} level={level + 1}
              selectedKeys={selectedKeys} onSelectionChange={onSelectionChange}
              onEdit={onEdit} onDelete={onDelete}
            />
          ))}
        </>
      )}
    </>
  );
}

export interface ButtonTreeViewProps {
  menus: MenuRow[]; buttons: ButtonRow[];
  selectedKeys: Set<number>;
  onSelectionChange: (next: Set<number>) => void;
  onEdit: (row: ButtonRow) => void;
  onDelete: (id: number, label: string) => void;
}

export function ButtonTreeView({ menus, buttons, selectedKeys, onSelectionChange, onEdit, onDelete }: ButtonTreeViewProps) {
  const grouped = useMemo(() => {
    const map = new Map<string, MenuRow[]>();
    menus.forEach(row => { const list = map.get(row.moduleCode) ?? []; list.push(row); map.set(row.moduleCode, list); });
    return map;
  }, [menus]);

  const childMenuMap = useMemo(() => {
    const map = new Map<number, MenuRow[]>();
    menus.forEach(row => {
      if (row.parentId !== null) {
        const list = map.get(row.parentId) ?? []; list.push(row); map.set(row.parentId, list);
      }
    });
    return map;
  }, [menus]);

  const buttonsByMenu = useMemo(() => {
    const map = new Map<number, ButtonRow[]>();
    buttons.forEach(btn => { const list = map.get(btn.menuId) ?? []; list.push(btn); map.set(btn.menuId, list); });
    return map;
  }, [buttons]);

  const [expandedModules, setExpandedModules] = useState<Set<string>>(() => new Set(menus.map(r => r.moduleCode)));
  const [expandedNodes, setExpandedNodes] = useState<Set<number>>(new Set());

  const treesByModule = useMemo(() => {
    const result = new Map<string, TreeNode[]>();
    grouped.forEach((items, code) => result.set(code, buildTree(items)));
    return result;
  }, [grouped]);

  const moduleSorted = useMemo(() => [...grouped.keys()].sort(), [grouped]);

  return (
    <div className="menu-tree">
      {moduleSorted.map(moduleCode => {
        const isModuleExpanded = expandedModules.has(moduleCode);
        const roots = treesByModule.get(moduleCode);
        if (!roots) return null;
        return (
          <div key={moduleCode} className="menu-tree__module">
            <div className="menu-tree__module-header" onClick={() => setExpandedModules(prev => {
              const next = new Set(prev);
              if (next.has(moduleCode)) next.delete(moduleCode); else next.add(moduleCode);
              return next;
            })}>
              {isModuleExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
              <span>{moduleCode}</span>
              <span className="menu-tree__module-count">{grouped.get(moduleCode)?.length ?? 0}</span>
            </div>
            {isModuleExpanded && roots.map(node => (
              <MenuNode key={node.row.id} node={node} level={0}
                expandedNodes={expandedNodes} selectedKeys={selectedKeys}
                childMenuMap={childMenuMap} buttonsByMenu={buttonsByMenu}
                onToggleNode={(id) => setExpandedNodes(prev => {
                  const next = new Set(prev);
                  if (next.has(id)) next.delete(id); else next.add(id);
                  return next;
                })}
                onSelectionChange={onSelectionChange} onEdit={onEdit} onDelete={onDelete}
              />
            ))}
          </div>
        );
      })}
    </div>
  );
}
