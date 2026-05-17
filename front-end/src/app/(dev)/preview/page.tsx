"use client";

import { useState } from "react";
import { InputsSection } from "./sections/inputs-section";
import { GridSection } from "./sections/grid-section";
import { ButtonsSection } from "./sections/buttons-section";
import { PaginationSection } from "./sections/pagination-section";
import { ScreenGuardSection } from "./sections/screen-guard-section";
import { ConfirmModalSection } from "./sections/confirm-modal-section";
import { ModalShellSection } from "./sections/modal-shell-section";

type SectionId = "inputs" | "grid" | "buttons" | "pagination" | "screen-guard" | "confirm-modal" | "modal-shell";

const MENU: { id: SectionId; label: string }[] = [
  { id: "inputs", label: "Inputs" },
  { id: "grid", label: "Grid" },
  { id: "buttons", label: "Buttons" },
  { id: "pagination", label: "Pagination" },
  { id: "screen-guard", label: "ScreenGuard" },
  { id: "confirm-modal", label: "Confirm Modal" },
  { id: "modal-shell", label: "Modal Shell" },
];

export default function PreviewPage() {
  const [active, setActive] = useState<SectionId>("inputs");

  return (
    <div style={{ display: "flex", height: "100vh", overflow: "hidden", fontFamily: "inherit", fontSize: 12 }}>
      {/* 사이드바 */}
      <nav style={{ width: 180, background: "#f5f5f5", borderRight: "1px solid #ddd", flexShrink: 0, display: "flex", flexDirection: "column" }}>
        <div style={{ padding: "16px", fontWeight: 700, fontSize: 13, borderBottom: "1px solid #ddd" }}>
          Dev Catalog
        </div>
        <ul style={{ listStyle: "none", margin: 0, padding: 0 }}>
          {MENU.map((item) => (
            <li key={item.id}>
              <button
                onClick={() => setActive(item.id)}
                style={{
                  width: "100%",
                  textAlign: "left",
                  padding: "8px 16px",
                  fontSize: 12,
                  border: "none",
                  cursor: "pointer",
                  background: active === item.id ? "#e8f0fe" : "transparent",
                  color: active === item.id ? "#1d4ed8" : "#333",
                  fontWeight: active === item.id ? 600 : 400,
                }}
              >
                {item.label}
              </button>
            </li>
          ))}
        </ul>
      </nav>

      {/* 컨텐츠 */}
      <main style={{ flex: 1, overflow: "auto" }}>
        {active === "inputs"       && <InputsSection />}
        {active === "grid"         && <GridSection />}
        {active === "buttons"      && <ButtonsSection />}
        {active === "pagination"   && <PaginationSection />}
        {active === "screen-guard" && <ScreenGuardSection />}
        {active === "confirm-modal" && <ConfirmModalSection />}
        {active === "modal-shell"   && <ModalShellSection />}
      </main>
    </div>
  );
}
