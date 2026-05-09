"use client";

// ScreenGuard: 비동기 처리 중 Entry 화면 전체를 잠그는 풀 오버레이 로딩 컴포넌트.
// prefers-reduced-motion 을 존중하며 접근성 role="status" 를 준수한다.

interface ScreenGuardProps {
  visible?: boolean;
  size?: "sm" | "md" | "lg";
  color?: string;
  /** 3~7 범위. 범위 밖 값은 자동 클램프 */
  count?: number;
  /** undefined 이면 aria-label='Loading', 표시 텍스트='조회 중...' */
  message?: string;
}

const SIZE_MAP = {
  sm: { height: 32, width: 4, gap: 4 },
  md: { height: 48, width: 6, gap: 6 },
  lg: { height: 64, width: 8, gap: 8 },
} as const;

/**
 * prefers-reduced-motion 정적 표시 시 각 막대의 높이 비율(%).
 * 중앙 대칭 규칙: count별 고정 배열 사용.
 * count=3:[60,100,60], count=4:[60,100,100,60],
 * count=5:[40,60,100,60,40], count=6:[40,60,100,100,60,40],
 * count=7:[40,40,60,100,60,40,40]
 */
const STATIC_HEIGHTS: Record<number, number[]> = {
  3: [60, 100, 60],
  4: [60, 100, 100, 60],
  5: [40, 60, 100, 60, 40],
  6: [40, 60, 100, 100, 60, 40],
  7: [40, 40, 60, 100, 60, 40, 40],
};

export function ScreenGuard({
  visible = true,
  size = "md",
  color = "#38BDF8",
  count = 5,
  message,
}: ScreenGuardProps) {
  if (!visible) return null;

  const clampedCount = Math.min(7, Math.max(3, count));
  const { height, width, gap } = SIZE_MAP[size];
  const staticHeights = STATIC_HEIGHTS[clampedCount];
  const bars = Array.from({ length: clampedCount }, (_, i) => i);

  return (
    <div
      role="status"
      aria-label={message ?? "Loading"}
      aria-live="polite"
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0, 0, 0, 0.65)",
        zIndex: 200,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        // 클릭·포커스 가로채기로 화면 잠금
        pointerEvents: "all",
      }}
      // 배경 클릭 방지: 이벤트 흡수
      onMouseDown={(e) => e.stopPropagation()}
      onClick={(e) => e.stopPropagation()}
    >
      {/* 인디케이터 컨테이너 — 막대 전체에 약한 drop-shadow 적용 */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          height,
          gap,
          filter: "drop-shadow(0 1px 4px rgba(0,0,0,0.5))",
        }}
      >
        {bars.map((i) => (
          <span
            key={i}
            className="screen-guard__bar animate-wave-bar"
            style={{
              display: "block",
              width,
              height: "100%",
              background: color,
              borderRadius: 3,
              transformOrigin: "center",
              animationDelay: `${i * 0.12}s`,
              // CSS 변수로 정적 높이 비율 전달 — reduced-motion 시 height: var(--static-h) 로 사용
              ["--static-h" as string]: `${staticHeights[i]}%`,
            }}
          />
        ))}
      </div>

      {/* 텍스트 — 막대 색과 별개로 고정 슬레이트 100 (#F1F5F9) 사용 */}
      <p
        style={{
          marginTop: 24,
          fontSize: 24,
          fontWeight: 600,
          color: "#F1F5F9",
          letterSpacing: "0.03em",
          // 어두운 배경·밝은 배경 모두에서 텍스트 가독성 확보
          filter: "drop-shadow(0 1px 3px rgba(0,0,0,0.6))",
        }}
        aria-hidden="true"
      >
        {message ?? "조회 중..."}
      </p>
    </div>
  );
}
