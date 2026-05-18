"use client";

import { ChevronRight } from "lucide-react";
import type { SidebarMenuRow } from "@/application/access/sidebar-menu/ports";
import { resolveIcon } from "./sidebar-icon-map";

// ─── Tree builder ────────────────────────────────────────────

export interface AdminMenuTree {
  roots: SidebarMenuRow[];
  childrenOf: Map<number, SidebarMenuRow[]>;
}

export function buildAdminMenuTree(rows: SidebarMenuRow[]): AdminMenuTree {
  const childrenOf = new Map<number, SidebarMenuRow[]>();
  for (const row of rows) {
    if (row.parentId != null) {
      const list = childrenOf.get(row.parentId) ?? [];
      list.push(row);
      childrenOf.set(row.parentId, list);
    }
  }
  const roots = rows
    .filter((r) => r.parentId == null)
    .sort((a, b) => a.sortOrder - b.sortOrder);
  for (const [, v] of childrenOf) {
    v.sort((a, b) => a.sortOrder - b.sortOrder);
  }
  return { roots, childrenOf };
}

// ─── Props ───────────────────────────────────────────────────

interface SidebarAdminTreeProps {
  data: SidebarMenuRow[] | undefined;
  isLoading: boolean;
  pathname: string;
  openSections: Record<string, boolean>;
  onToggleSection: (group: string) => void;
  onNavigate: (label: string, href: string) => void;
}

// ─── Component ───────────────────────────────────────────────

export function SidebarAdminTree({
  data,
  isLoading,
  pathname,
  openSections,
  onToggleSection,
  onNavigate,
}: SidebarAdminTreeProps) {
  // USER 시나리오 등 접근 가능 메뉴가 없으면 모듈 헤더 자체 미렌더
  if (!isLoading && (!data || data.length === 0)) {
    return null;
  }

  const tree = data ? buildAdminMenuTree(data) : null;

  return (
    <div className="side-group">
      <div className="side-group__label">
        <span style={{ flex: 1 }}>Admin</span>
      </div>

      {isLoading && (
        <div
          style={{
            paddingLeft: 12,
            fontSize: "var(--fs-xs)",
            color: "var(--ink-4)",
          }}
        >
          Loading…
        </div>
      )}

      {tree &&
        tree.roots.map((root) => {
          const RootIcon = resolveIcon(root.icon);
          const children = tree.childrenOf.get(root.id) ?? [];
          const secOpen = openSections[root.menuCode] ?? false;
          const secActive =
            (root.path != null && (pathname === root.path || pathname.startsWith(root.path + "/"))) ||
            children.some(
              (c) =>
                c.path != null &&
                (pathname === c.path || pathname.startsWith(c.path + "/"))
            );

          if (children.length === 0) {
            // leaf node (no children): path가 있으면 Link, 없으면 비활성 span
            if (root.path == null) {
              return (
                <div
                  key={root.id}
                  className={`side-item${secActive ? " is-active" : ""}`}
                  style={{ paddingLeft: 12 }}
                >
                  <span className="side-item__icon"><RootIcon size={13} /></span>
                  <span style={{ flex: 1, textAlign: "left" }}>{root.label}</span>
                </div>
              );
            }
            return (
              <button
                key={root.id}
                className={`side-item${secActive ? " is-active" : ""}`}
                style={{ paddingLeft: 12 }}
                onClick={() => onNavigate(root.label, root.path!)}
              >
                <span className="side-item__icon"><RootIcon size={13} /></span>
                <span style={{ flex: 1, textAlign: "left" }}>{root.label}</span>
              </button>
            );
          }

          // section node (has children): 토글 가능
          return (
            <div key={root.id}>
              <button
                className={`side-item${secActive ? " is-active" : ""}`}
                style={{ paddingLeft: 12 }}
                onClick={() => onToggleSection(root.menuCode)}
              >
                <span className="side-item__icon"><RootIcon size={13} /></span>
                <span style={{ flex: 1, textAlign: "left" }}>{root.label}</span>
                <ChevronRight
                  size={11}
                  style={{
                    flexShrink: 0,
                    color: secActive ? "var(--accent)" : "var(--ink-4)",
                    transform: secOpen ? "rotate(90deg)" : undefined,
                    transition: "transform 160ms ease",
                    marginRight: 4,
                  }}
                />
              </button>

              {secOpen &&
                children.map((leaf) => {
                  const LeafIcon = resolveIcon(leaf.icon);
                  const active =
                    leaf.path != null &&
                    (pathname === leaf.path || pathname.startsWith(leaf.path + "/"));

                  if (leaf.path == null) {
                    return (
                      <div
                        key={leaf.id}
                        className={`side-item${active ? " is-active" : ""}`}
                        style={{ paddingLeft: 32, fontSize: "var(--fs-xs)" }}
                      >
                        <span className="side-item__icon"><LeafIcon size={11} /></span>
                        {leaf.label}
                      </div>
                    );
                  }

                  return (
                    <button
                      key={leaf.id}
                      className={`side-item${active ? " is-active" : ""}`}
                      style={{ paddingLeft: 32, fontSize: "var(--fs-xs)" }}
                      onClick={() => onNavigate(`${root.label} ${leaf.label}`, leaf.path!)}
                    >
                      <span className="side-item__icon"><LeafIcon size={11} /></span>
                      {leaf.label}
                    </button>
                  );
                })}
            </div>
          );
        })}
    </div>
  );
}
