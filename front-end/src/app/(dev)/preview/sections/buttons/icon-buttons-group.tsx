"use client";

import { Plus, Minus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { sectionStyle, labelStyle } from "./_shared";

export function IconButtonsGroup() {
  return (
    <section style={sectionStyle}>
      <div style={labelStyle}>C. Grid Icon Buttons — success / danger (행 추가/삭제)</div>
      <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 8 }}>
        <Button variant="success" size="sm" iconOnly onClick={() => alert("Add row")}>
          <Plus size={12} />
        </Button>
        <Button variant="danger" size="sm" iconOnly onClick={() => alert("Remove row")}>
          <Minus size={12} />
        </Button>
        <span style={{ fontSize: 10, color: "#666", marginLeft: 4 }}>
          {'<Button variant="success" size="sm" iconOnly>'}
          {"<Plus size={12} />"}
          {"</Button>"}
        </span>
      </div>
      <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 8 }}>
        <Button variant="danger" size="sm" iconOnly disabled>
          <Minus size={12} />
        </Button>
        <span style={{ fontSize: 10, color: "#666", marginLeft: 4 }}>disabled 상태</span>
      </div>
    </section>
  );
}
