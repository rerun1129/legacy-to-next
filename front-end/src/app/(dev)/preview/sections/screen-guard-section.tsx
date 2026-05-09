"use client";

import { useState } from "react";
import { ScreenGuard } from "@/components/shared/screen-guard";

type DemoSize = "sm" | "md" | "lg";
type DemoCount = 3 | 5 | 7;

/** reduced-motion 을 강제 시뮬레이션하는 인라인 데모 박스 */
function StaticBarDemo({
  count,
  size,
  color,
}: {
  count: number;
  size: "sm" | "md" | "lg";
  color?: string;
}) {
  const HEIGHT_MAP = { sm: 32, md: 48, lg: 64 } as const;
  const WIDTH_MAP  = { sm: 4,  md: 6,  lg: 8  } as const;
  const GAP_MAP    = { sm: 4,  md: 6,  lg: 8  } as const;
  const STATIC_HEIGHTS: Record<number, number[]> = {
    3: [60, 100, 60],
    4: [60, 100, 100, 60],
    5: [40, 60, 100, 60, 40],
    6: [40, 60, 100, 100, 60, 40],
    7: [40, 40, 60, 100, 60, 40, 40],
  };

  const h = HEIGHT_MAP[size];
  const w = WIDTH_MAP[size];
  const g = GAP_MAP[size];
  const staticH = STATIC_HEIGHTS[count] ?? STATIC_HEIGHTS[5];
  const bars = Array.from({ length: count }, (_, i) => i);

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        height: h,
        gap: g,
      }}
    >
      {bars.map((i) => (
        <span
          key={i}
          style={{
            display: "block",
            width: w,
            height: `${staticH[i]}%`,
            background: color ?? "currentColor",
            borderRadius: 3,
          }}
        />
      ))}
    </div>
  );
}

export function ScreenGuardSection() {
  const [overlayVisible, setOverlayVisible] = useState(false);
  const [overlaySize, setOverlaySize]       = useState<DemoSize>("md");
  const [overlayMsg, setOverlayMsg]         = useState("");

  const SIZES: DemoSize[]   = ["sm", "md", "lg"];
  const COUNTS: DemoCount[] = [3, 5, 7];

  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 960, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 4 }}>ScreenGuard</h1>
      <p style={{ color: "#666", marginBottom: 24, fontSize: 12 }}>
        Entry 화면 전체를 잠그는 풀 오버레이 로딩 컴포넌트. <code>visible</code> prop으로 제어.
      </p>

      {/* ── 1. Size variants ── */}
      <section style={{ borderTop: "1px solid #e5e5e5", paddingTop: 16, marginBottom: 24 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 12 }}>1. Size (sm / md / lg)</h2>
        <div style={{ display: "flex", gap: 40, alignItems: "flex-end" }}>
          {SIZES.map((sz) => (
            <div key={sz} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
              <StaticBarDemo count={5} size={sz} color="#1f3a8a" />
              <span style={{ fontSize: 11, color: "#555" }}>{sz}</span>
            </div>
          ))}
        </div>
      </section>

      {/* ── 2. Count variants ── */}
      <section style={{ borderTop: "1px solid #e5e5e5", paddingTop: 16, marginBottom: 24 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 12 }}>2. Count (3 / 5 / 7)</h2>
        <div style={{ display: "flex", gap: 40, alignItems: "flex-end" }}>
          {COUNTS.map((c) => (
            <div key={c} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
              <StaticBarDemo count={c} size="md" color="#1f3a8a" />
              <span style={{ fontSize: 11, color: "#555" }}>count={c}</span>
            </div>
          ))}
        </div>
      </section>

      {/* ── 3. Color override ── */}
      <section style={{ borderTop: "1px solid #e5e5e5", paddingTop: 16, marginBottom: 24 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 12 }}>3. Color override (rose-600)</h2>
        <StaticBarDemo count={5} size="md" color="#e11d48" />
      </section>

      {/* ── 4. Message variants ── */}
      <section style={{ borderTop: "1px solid #e5e5e5", paddingTop: 16, marginBottom: 24 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 12 }}>4. Message variants</h2>
        <div style={{ display: "flex", gap: 32 }}>
          {["저장 중...", "제출 중...", "조회 중..."].map((msg) => (
            <div key={msg} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 6 }}>
              <StaticBarDemo count={5} size="md" color="#1f3a8a" />
              <p style={{ fontSize: 12, color: "#1f3a8a", margin: 0 }}>{msg}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── 5. Reduced-motion static 시연 ── */}
      <section style={{ borderTop: "1px solid #e5e5e5", paddingTop: 16, marginBottom: 24 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>5. Reduced-motion 정적 모드 (강제 시뮬레이션)</h2>
        <p style={{ fontSize: 11, color: "#888", marginBottom: 12 }}>
          실제 reduced-motion 환경에서는 animation: none이 적용되며, 각 막대가 중앙 대칭 높이로 정지 표시됩니다.
        </p>
        <div style={{ display: "flex", gap: 32 }}>
          {COUNTS.map((c) => (
            <div key={c} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
              <StaticBarDemo count={c} size="md" color="#1f3a8a" />
              <span style={{ fontSize: 11, color: "#555" }}>count={c} 정적</span>
            </div>
          ))}
        </div>
      </section>

      {/* ── 6. 풀 오버레이 라이브 데모 ── */}
      <section style={{ borderTop: "1px solid #e5e5e5", paddingTop: 16, marginBottom: 24 }}>
        <h2 style={{ fontSize: 13, fontWeight: 600, marginBottom: 12 }}>6. 풀 오버레이 라이브 데모</h2>
        <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 12 }}>
          <label style={{ fontSize: 12 }}>
            Size:&nbsp;
            <select
              value={overlaySize}
              onChange={(e) => setOverlaySize(e.target.value as DemoSize)}
              style={{ fontSize: 12, padding: "2px 4px" }}
            >
              {SIZES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </label>
          <label style={{ fontSize: 12 }}>
            Message:&nbsp;
            <select
              value={overlayMsg}
              onChange={(e) => setOverlayMsg(e.target.value)}
              style={{ fontSize: 12, padding: "2px 4px" }}
            >
              <option value="">기본 (조회 중...)</option>
              <option value="저장 중...">저장 중...</option>
              <option value="제출 중...">제출 중...</option>
            </select>
          </label>
          <button
            onClick={() => setOverlayVisible(true)}
            style={{
              padding: "6px 14px",
              background: "#1f3a8a",
              color: "#fff",
              border: "none",
              borderRadius: 4,
              fontSize: 12,
              cursor: "pointer",
            }}
          >
            오버레이 표시 (3초 후 자동 닫힘)
          </button>
        </div>
        {overlayVisible && (
          <ScreenGuard
            visible={overlayVisible}
            size={overlaySize}
            message={overlayMsg || undefined}
          />
        )}
        {overlayVisible && (
          /* 3초 후 자동 닫힘 — setTimeout 사이드 이펙트를 렌더 중 호출하면 안 되므로 effect 없이 버튼으로 대체 */
          <button
            onClick={() => setOverlayVisible(false)}
            style={{
              padding: "6px 14px",
              background: "#e11d48",
              color: "#fff",
              border: "none",
              borderRadius: 4,
              fontSize: 12,
              cursor: "pointer",
              position: "fixed",
              bottom: 32,
              left: "50%",
              transform: "translateX(-50%)",
              zIndex: 300,
            }}
          >
            닫기
          </button>
        )}
      </section>
    </div>
  );
}
