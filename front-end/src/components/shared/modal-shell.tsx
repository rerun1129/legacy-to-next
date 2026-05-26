"use client";

import type { CSSProperties, ReactNode } from "react";
import { useModalDrag } from "./use-modal-drag";

export interface ModalShellProps {
  isOpen: boolean;
  title: string;
  size?: "default" | "md" | "lg";
  style?: CSSProperties;
  children: ReactNode;
}

// isOpen=false 시 unmount → children 의 hooks·useQuery 캐시 초기화 보장
export function ModalShell({ isOpen, title, size = "default", style, children }: ModalShellProps) {
  if (!isOpen) return null;
  return <ModalShellInner title={title} size={size} style={style}>{children}</ModalShellInner>;
}

function ModalShellInner({ title, size, style, children }: Omit<ModalShellProps, "isOpen">) {
  const { offset, onHeaderMouseDown } = useModalDrag();
  const sizeClass = size === "lg" ? "modal modal--lg" : size === "md" ? "modal modal--md" : "modal";
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className={sizeClass} style={{ transform: `translate(${offset.x}px, ${offset.y}px)`, ...style }}>
        <div
          className="modal__header"
          style={{ cursor: "move", userSelect: "none" }}
          onMouseDown={onHeaderMouseDown}
        >
          <span className="modal__title">{title}</span>
        </div>
        {children}
      </div>
    </div>
  );
}
