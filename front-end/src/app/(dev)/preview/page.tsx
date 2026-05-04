"use client";

import { useState } from "react";
import { InputsSection } from "./sections/inputs-section";
import { GridSection } from "./sections/grid-section";
import { ButtonsSection } from "./sections/buttons-section";

type SectionId = "inputs" | "grid" | "buttons";

const MENU: { id: SectionId; label: string }[] = [
  { id: "inputs", label: "Inputs" },
  { id: "grid", label: "Grid" },
  { id: "buttons", label: "Buttons" },
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
        {active === "inputs" && <InputsSection />}
        {active === "grid" && <GridSection />}
        {active === "buttons" && <ButtonsSection />}
      </main>
    </div>
  );
}
