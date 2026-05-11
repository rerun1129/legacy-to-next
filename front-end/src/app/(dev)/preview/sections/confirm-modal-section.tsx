"use client";

import { useState } from "react";
import { confirm } from "@/components/confirm";

function sleep(ms: number) {
  return new Promise<void>((resolve) => setTimeout(resolve, ms));
}

type ResultLabel = "확인됨" | "취소됨" | null;

export function ConfirmModalSection() {
  const [variantResult, setVariantResult] = useState<ResultLabel>(null);
  const [detailsResult, setDetailsResult] = useState<ResultLabel>(null);
  const [asyncResult, setAsyncResult] = useState<ResultLabel>(null);

  async function handleDefaultVariant() {
    const ok = await confirm({
      title: "정말 진행하시겠습니까?",
      description: "이 작업은 되돌릴 수 없습니다.",
      variant: "default",
    });
    setVariantResult(ok ? "확인됨" : "취소됨");
  }

  async function handleDestructiveVariant() {
    const ok = await confirm({
      title: "항목을 삭제하시겠습니까?",
      description: "삭제된 데이터는 복구할 수 없습니다.",
      variant: "destructive",
      confirmText: "삭제",
    });
    setVariantResult(ok ? "확인됨" : "취소됨");
  }

  async function handleWarningVariant() {
    const ok = await confirm({
      title: "주의가 필요한 작업입니다",
      description: "계속하면 일부 데이터가 변경됩니다.",
      variant: "warning",
    });
    setVariantResult(ok ? "확인됨" : "취소됨");
  }

  async function handleWithDetails() {
    const ok = await confirm({
      title: "선택한 항목을 삭제하시겠습니까?",
      description: "아래 B/L이 영구 삭제됩니다.",
      variant: "destructive",
      confirmText: "삭제",
      details: [
        ["B/L No", "HBL-2024-001234"],
        ["POL → POD", "PUS → LAX"],
      ],
    });
    setDetailsResult(ok ? "확인됨" : "취소됨");
  }

  async function handleAsyncConfirm() {
    const ok = await confirm({
      title: "저장하시겠습니까?",
      description: "서버에 데이터를 저장합니다 (약 1.5초 소요).",
      variant: "default",
      onConfirm: async () => {
        await sleep(1500);
      },
    });
    setAsyncResult(ok ? "확인됨" : "취소됨");
  }

  async function handleAsyncThrow() {
    const ok = await confirm({
      title: "실패할 수도 있는 작업",
      description: "50% 확률로 오류가 발생합니다. 오류 시 모달이 유지됩니다.",
      variant: "warning",
      onConfirm: async () => {
        await sleep(800);
        if (Math.random() < 0.5) {
          throw new Error("랜덤 오류 발생 (데모)");
        }
      },
    });
    setAsyncResult(ok ? "확인됨" : "취소됨");
  }

  return (
    <div style={{ padding: 24, maxWidth: 960, margin: "0 auto" }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 24 }}>
        Confirm Modal Preview
      </h1>

      {/* ── 1. Variants ─────────────────────────────────── */}
      <section style={{ marginBottom: 32 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 8, color: "var(--ink-2)" }}>
          Variants
        </h2>
        <p style={{ fontSize: 11, color: "var(--ink-4)", marginBottom: 12, fontFamily: "monospace" }}>
          default / destructive / warning
        </p>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <button className="btn" onClick={handleDefaultVariant}>
            Default
          </button>
          <button className="btn btn--danger" onClick={handleDestructiveVariant}>
            Destructive
          </button>
          <button className="btn btn--normal" onClick={handleWarningVariant}>
            Warning
          </button>
        </div>
        {variantResult && (
          <p style={{ marginTop: 8, fontSize: 12, color: "var(--ink-3)" }}>
            결과: <strong>{variantResult}</strong>
          </p>
        )}
      </section>

      {/* ── 2. With details ─────────────────────────────── */}
      <section style={{ marginBottom: 32 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 8, color: "var(--ink-2)" }}>
          With details
        </h2>
        <p style={{ fontSize: 11, color: "var(--ink-4)", marginBottom: 12, fontFamily: "monospace" }}>
          destructive + details: [[key, value], ...]
        </p>
        <button className="btn btn--danger" onClick={handleWithDetails}>
          details 포함 삭제 확인
        </button>
        {detailsResult && (
          <p style={{ marginTop: 8, fontSize: 12, color: "var(--ink-3)" }}>
            결과: <strong>{detailsResult}</strong>
          </p>
        )}
      </section>

      {/* ── 3. Async onConfirm ──────────────────────────── */}
      <section style={{ marginBottom: 32 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 8, color: "var(--ink-2)" }}>
          Async onConfirm
        </h2>
        <p style={{ fontSize: 11, color: "var(--ink-4)", marginBottom: 12, fontFamily: "monospace" }}>
          onConfirm: async () =&gt; &#123; await sleep(1500) &#125; — 로딩 스핀 + 버튼 disabled 확인
        </p>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <button className="btn btn--transaction" onClick={handleAsyncConfirm}>
            Async 저장 (1.5s)
          </button>
          <button className="btn btn--normal" onClick={handleAsyncThrow}>
            Async throw 데모 (50%)
          </button>
        </div>
        {asyncResult && (
          <p style={{ marginTop: 8, fontSize: 12, color: "var(--ink-3)" }}>
            결과: <strong>{asyncResult}</strong>
          </p>
        )}
      </section>
    </div>
  );
}
