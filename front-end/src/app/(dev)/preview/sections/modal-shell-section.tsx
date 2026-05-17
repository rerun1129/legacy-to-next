"use client";

import { useState } from "react";
import { ModalShell } from "@/components/shared/modal-shell";

export function ModalShellSection() {
  const [openDefault, setOpenDefault] = useState(false);
  const [openLg, setOpenLg] = useState(false);

  return (
    <div style={{ padding: 16 }}>
      <h2 style={{ marginTop: 0 }}>ModalShell</h2>
      <p style={{ color: "#555" }}>
        공통 모달 셸 — backdrop / 드래그 가능한 헤더 / title. children 으로 body/footer 자유 구성.
      </p>

      <section style={{ marginTop: 24 }}>
        <h3>size=&ldquo;default&rdquo;</h3>
        <button className="btn btn--sm btn--primary" onClick={() => setOpenDefault(true)}>Open Default</button>
        <ModalShell isOpen={openDefault} title="Default Modal">
          <div className="modal__body">
            <p>이 영역은 children — body 슬롯입니다.</p>
          </div>
          <div className="modal__actions">
            <button type="button" className="btn btn--sm" onClick={() => setOpenDefault(false)}>Close</button>
          </div>
        </ModalShell>
      </section>

      <section style={{ marginTop: 24 }}>
        <h3>size=&ldquo;lg&rdquo;</h3>
        <button className="btn btn--sm btn--primary" onClick={() => setOpenLg(true)}>Open Large</button>
        <ModalShell isOpen={openLg} title="Large Modal" size="lg">
          <div className="modal__body">
            <p>size=&ldquo;lg&rdquo; 옵션 — modal--lg 클래스 적용.</p>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn--sm" onClick={() => setOpenLg(false)}>Close</button>
          </div>
        </ModalShell>
      </section>
    </div>
  );
}
