"use client";

import { forwardRef, useImperativeHandle, useMemo, useState } from "react";
import { Plus, Minus, ChevronDown, ChevronRight } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/shared/button";
import type { UseFormRegister, Control } from "react-hook-form";
import type { MenuRow } from "@/domain/access/menu";
import { ButtonRowCells } from "./button-grid-columns";
import type { ButtonFormRow, ButtonFormValues } from "./button-list-helpers";
import { ButtonTreeHeader } from "./button-tree-header";

// ─── 메뉴 트리 빌더 ──────────────────────────────────────────────────────────

interface MenuTreeNode {
  row: MenuRow;
  children: MenuTreeNode[];
}

function buildMenuTree(items: MenuRow[]): MenuTreeNode[] {
  const nodeMap = new Map<number, MenuTreeNode>();
  items.forEach((row) => nodeMap.set(row.id, { row, children: [] }));

  const roots: MenuTreeNode[] = [];
  items.forEach((row) => {
    const node = nodeMap.get(row.id)!;
    if (row.parentId === null) {
      roots.push(node);
    } else {
      const parent = nodeMap.get(row.parentId);
      if (parent) parent.children.push(node);
      else roots.push(node);
    }
  });

  const bySortOrder = (a: MenuTreeNode, b: MenuTreeNode) =>
    (a.row.sortOrder ?? 0) - (b.row.sortOrder ?? 0);
  roots.sort(bySortOrder);
  nodeMap.forEach((n) => n.children.sort(bySortOrder));
  return roots;
}

// ─── 메뉴 노드 행 (읽기전용 그룹 헤더) ───────────────────────────────────────

type MsgT = ReturnType<typeof useTranslations>;
type OptionsT = (key: string) => string;

interface MenuNodeRowProps {
  node: MenuTreeNode;
  level: number;
  expandedNodes: Set<number>;
  buttonsByMenuId: Map<number, ButtonFormRow[]>;
  indexByEntityId: Map<number, number>;
  register: UseFormRegister<ButtonFormValues>;
  control: Control<ButtonFormValues>;
  tMsg: MsgT;
  tOptions: OptionsT;
  onToggleNode: (id: number) => void;
  onAddButton: (menuId: number, moduleCode: string) => void;
  onRemoveNewRow: (entityId: number) => void;
}

function MenuNodeRow({
  node,
  level,
  expandedNodes,
  buttonsByMenuId,
  indexByEntityId,
  register,
  control,
  tMsg,
  tOptions,
  onToggleNode,
  onAddButton,
  onRemoveNewRow,
}: MenuNodeRowProps) {
  const { row } = node;
  const isExpanded = expandedNodes.has(row.id);
  const indentPx = level * 16;
  const ownButtons = buttonsByMenuId.get(row.id) ?? [];

  return (
    <>
      {/* 메뉴 노드 헤더 행 (읽기전용) */}
      <div
        className="tree-row"
        style={{
          paddingLeft: 8 + indentPx,
          background: "var(--surface-2)",
          display: "flex",
          alignItems: "center",
          gap: 4,
          minHeight: 32,
        }}
      >
        {/* 체크박스 스페이서 — 버튼 행 체크박스 위치와 정렬 */}
        <span style={{ display: "inline-block", width: 20, flexShrink: 0 }} />

        {/* 토글 버튼 */}
        <button
          className="tree-row__toggle"
          onClick={() => onToggleNode(row.id)}
          aria-label={isExpanded ? tMsg("collapse") : tMsg("expand")}
          type="button"
          style={{ flexShrink: 0 }}
        >
          {isExpanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
        </button>

        {/* 메뉴 코드 + 라벨 (읽기전용, DB 데이터이므로 번역 불필요) */}
        <span
          style={{
            fontFamily: "var(--font-mono)",
            fontWeight: 600,
            fontSize: 12,
            color: "var(--ink-2)",
            marginRight: 8,
          }}
        >
          {row.menuCode}
        </span>
        <span style={{ fontSize: 12, color: "var(--ink-3)" }}>{row.label}</span>

        {/* 이 메뉴 아래 버튼 추가 "+" */}
        <div style={{ marginLeft: "auto" }} onClick={(e) => e.stopPropagation()}>
          <Button
            variant="success"
            size="sm"
            iconOnly
            type="button"
            onClick={() => onAddButton(row.id, row.moduleCode)}
            aria-label={tMsg("addButtonUnder", { menuCode: row.menuCode })}
          >
            <Plus size={10} />
          </Button>
        </div>
      </div>

      {/* 펼쳐진 경우: 자식 메뉴 노드 + 이 메뉴에 속한 버튼 행 */}
      {isExpanded && (
        <>
          {node.children.map((child) => (
            <MenuNodeRow
              key={child.row.id}
              node={child}
              level={level + 1}
              expandedNodes={expandedNodes}
              buttonsByMenuId={buttonsByMenuId}
              indexByEntityId={indexByEntityId}
              register={register}
              control={control}
              tMsg={tMsg}
              tOptions={tOptions}
              onToggleNode={onToggleNode}
              onAddButton={onAddButton}
              onRemoveNewRow={onRemoveNewRow}
            />
          ))}
          {ownButtons.map((btnRow) => (
            <ButtonLeafRow
              key={btnRow.entityId}
              row={btnRow}
              level={level + 1}
              indexByEntityId={indexByEntityId}
              register={register}
              control={control}
              tOptions={tOptions}
              onRemoveNewRow={onRemoveNewRow}
            />
          ))}
        </>
      )}
    </>
  );
}

// ─── 버튼 leaf 행 (인라인 편집 대상) ─────────────────────────────────────────

interface ButtonLeafRowProps {
  row: ButtonFormRow;
  level: number;
  indexByEntityId: Map<number, number>;
  register: UseFormRegister<ButtonFormValues>;
  control: Control<ButtonFormValues>;
  tOptions: OptionsT;
  onRemoveNewRow: (entityId: number) => void;
}

function ButtonLeafRow({
  row,
  level,
  indexByEntityId,
  register,
  control,
  tOptions,
  onRemoveNewRow,
}: ButtonLeafRowProps) {
  const isNew = row.entityId < 0;
  const idx = indexByEntityId.get(row.entityId) ?? -1;
  const indentPx = level * 16;

  return (
    <div
      className={`tree-row${isNew ? " is-new" : ""}`}
      style={{
        paddingLeft: 8 + indentPx,
        display: "flex",
        alignItems: "center",
        gap: 4,
        minHeight: 32,
      }}
    >
      {/* 체크박스 스페이서 — 현재 버튼 행은 체크박스 없음(개별 선택 불필요, active 콤보로 관리) */}
      <span style={{ display: "inline-block", width: 20, flexShrink: 0 }} />

      {/* leaf indent 스페이서 — 헤더 TOGGLE_W(20px)와 맞춤 */}
      <span
        className="tree-row__leaf-indent"
        style={{ display: "inline-block", width: 20, flexShrink: 0 }}
      />

      {/* 셀 그룹 */}
      {idx >= 0 && (
        <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
          <ButtonRowCells row={row} idx={idx} register={register} control={control} tOptions={tOptions} />
        </div>
      )}

      {/* 신규 행(entityId<0)에만 제거 버튼 */}
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
  );
}

// ─── 공개 인터페이스 ──────────────────────────────────────────────────────────

export interface ButtonTreeHandle {
  expandAll(): void;
  collapseAll(): void;
}

export interface ButtonTreeViewProps {
  menus: MenuRow[];
  rows: ButtonFormRow[];
  indexByEntityId: Map<number, number>;
  register: UseFormRegister<ButtonFormValues>;
  control: Control<ButtonFormValues>;
  onAddButton: (menuId: number, moduleCode: string) => void;
  onRemoveNewRow: (entityId: number) => void;
}

export const ButtonTreeView = forwardRef<ButtonTreeHandle, ButtonTreeViewProps>(
  function ButtonTreeView(
    { menus, rows, indexByEntityId, register, control, onAddButton, onRemoveNewRow },
    ref,
  ) {
    // useTranslations는 early-return 이전에 무조건 호출 (Rules of Hooks)
    const tMsg = useTranslations("admin.button.msg");
    const tOptions = useTranslations("admin.button.options");

    // 메뉴를 moduleCode로 그룹핑
    const menusByModule = useMemo(() => {
      const map = new Map<string, MenuRow[]>();
      menus.forEach((menu) => {
        const list = map.get(menu.moduleCode) ?? [];
        list.push(menu);
        map.set(menu.moduleCode, list);
      });
      return map;
    }, [menus]);

    // 버튼을 menuId로 인덱싱 (ButtonFormRow)
    const buttonsByMenuId = useMemo(() => {
      const map = new Map<number, ButtonFormRow[]>();
      rows.forEach((row) => {
        const list = map.get(row.menuId) ?? [];
        list.push(row);
        map.set(row.menuId, list);
      });
      return map;
    }, [rows]);

    // 메뉴 트리 by module
    const treesByModule = useMemo(() => {
      const result = new Map<string, MenuTreeNode[]>();
      menusByModule.forEach((items, code) => {
        result.set(code, buildMenuTree(items));
      });
      return result;
    }, [menusByModule]);

    const moduleSorted = useMemo(() => [...menusByModule.keys()].sort(), [menusByModule]);

    const [expandedModules, setExpandedModules] = useState<Set<string>>(
      () => new Set(menus.map((m) => m.moduleCode)),
    );
    const [expandedNodes, setExpandedNodes] = useState<Set<number>>(new Set());

    // expandAll: 모든 모듈 펼치기 + 모든 메뉴 노드 펼치기
    // collapseAll: 모듈·노드 모두 접기
    useImperativeHandle(
      ref,
      () => ({
        expandAll() {
          setExpandedModules(new Set(menus.map((m) => m.moduleCode)));
          setExpandedNodes(new Set(menus.map((m) => m.id)));
        },
        collapseAll() {
          setExpandedModules(new Set());
          setExpandedNodes(new Set());
        },
      }),
      [menus],
    );

    function toggleModule(code: string) {
      setExpandedModules((prev) => {
        const next = new Set(prev);
        if (next.has(code)) next.delete(code);
        else next.add(code);
        return next;
      });
    }

    function toggleNode(id: number) {
      setExpandedNodes((prev) => {
        const next = new Set(prev);
        if (next.has(id)) next.delete(id);
        else next.add(id);
        return next;
      });
    }

    return (
      <div className="menu-tree">
        <ButtonTreeHeader />
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
                  {menusByModule.get(moduleCode)?.length ?? 0}
                </span>
              </div>
              {isModuleExpanded &&
                tree.map((node) => (
                  <MenuNodeRow
                    key={node.row.id}
                    node={node}
                    level={0}
                    expandedNodes={expandedNodes}
                    buttonsByMenuId={buttonsByMenuId}
                    indexByEntityId={indexByEntityId}
                    register={register}
                    control={control}
                    tMsg={tMsg}
                    tOptions={tOptions}
                    onToggleNode={toggleNode}
                    onAddButton={onAddButton}
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
