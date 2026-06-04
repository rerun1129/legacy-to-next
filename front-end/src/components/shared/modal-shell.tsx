"use client";

import type { CSSProperties, ReactNode } from "react";
import { createPortal } from "react-dom";
import { useModalDrag } from "./use-modal-drag";

export interface ModalShellProps {
  isOpen: boolean;
  title: string;
  size?: "default" | "md" | "lg";
  style?: CSSProperties;
  /**
   * true면 backdrop을 document.body로 portal해 조상 transform/stacking 영향 없이
   * viewport 전체를 덮음. 기본 false — 기존 모달 동작·렌더 위치 불변(opt-in).
   */
  portal?: boolean;
  children: ReactNode;
}

// isOpen=false 시 unmount → children 의 hooks·useQuery 캐시 초기화 보장
export function ModalShell({ isOpen, title, size = "default", style, portal, children }: ModalShellProps) {
  if (!isOpen) return null;
  return <ModalShellInner title={title} size={size} style={style} portal={portal}>{children}</ModalShellInner>;
}

function ModalShellInner({ title, size, style, portal, children }: Omit<ModalShellProps, "isOpen">) {
  const { offset, onHeaderMouseDown } = useModalDrag();
  const sizeClass = size === "lg" ? "modal modal--lg" : size === "md" ? "modal modal--md" : "modal";
  const content = (
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
  if (portal && typeof document !== "undefined") {
    return createPortal(content, document.body);
  }
  return content;
}
