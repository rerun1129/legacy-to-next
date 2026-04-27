import { render } from "@testing-library/react";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { useFieldLayout } from "@/lib/use-field-layout";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { BL_VARIANTS } from "@/lib/bl-variants";
import { SchedulePanel } from "@/components/fms/house-bl/tabs/sections/schedule-panel";
import { PartyPanel } from "@/components/fms/house-bl/tabs/sections/party-panel";
import { TradePanel } from "@/components/fms/house-bl/tabs/sections/trade-panel";

// useCurrentUser는 zustand store — mock 시 selector 패턴 지원
vi.mock("@/lib/use-current-user", () => ({
  useCurrentUser: (selector?: (s: { currentUserId: string }) => unknown) => {
    const state = { currentUserId: "test-user" };
    return selector ? selector(state) : state;
  },
}));

describe("Scope 분리 회귀 — variant 별 별개 store key 생성 검증", () => {
  beforeEach(() => {
    // 테스트 간 zustand store 완전 초기화
    useFieldLayout.setState({ layouts: {} });
    useWidgetLayout.setState({ layouts: {}, editMode: false, canEdit: true });
  });

  it("SchedulePanel: sea-exp와 sea-imp는 별개 scope key를 생성한다", () => {
    // sea-exp 렌더 후 store key 확인
    const { unmount } = render(<SchedulePanel variant={BL_VARIANTS["sea-exp"]} />);
    unmount();
    const layouts1 = useFieldLayout.getState().layouts;
    const seaExpKey = Object.keys(layouts1).find(k => k.includes("sea-exp"));
    expect(seaExpKey).toBeDefined();

    // sea-imp 렌더 후 store key 확인
    render(<SchedulePanel variant={BL_VARIANTS["sea-imp"]} />);
    const layouts2 = useFieldLayout.getState().layouts;
    const seaImpKey = Object.keys(layouts2).find(k => k.includes("sea-imp"));
    expect(seaImpKey).toBeDefined();

    // 두 key는 서로 달라야 한다
    expect(seaExpKey).not.toBe(seaImpKey);
  });

  it("PartyPanel: sea-exp와 air-exp는 별개 scope key를 생성한다", () => {
    // sea-exp variant 기준 렌더 (isExp: true)
    const { unmount } = render(
      <PartyPanel variant={BL_VARIANTS["sea-exp"]} isExp={true} />
    );
    unmount();
    const layouts1 = useFieldLayout.getState().layouts;
    const seaExpKey = Object.keys(layouts1).find(k => k.includes("sea-exp"));
    expect(seaExpKey).toBeDefined();

    // air-exp variant 기준 렌더 (isExp: true)
    render(<PartyPanel variant={BL_VARIANTS["air-exp"]} isExp={true} />);
    const layouts2 = useFieldLayout.getState().layouts;
    const airExpKey = Object.keys(layouts2).find(k => k.includes("air-exp"));
    expect(airExpKey).toBeDefined();

    expect(seaExpKey).not.toBe(airExpKey);
  });

  it("TradePanel: sea-exp와 sea-imp는 별개 scope key를 생성한다", () => {
    // sea-exp variant 기준 렌더
    const { unmount } = render(<TradePanel variant={BL_VARIANTS["sea-exp"]} />);
    unmount();
    const layouts1 = useFieldLayout.getState().layouts;
    const seaExpKey = Object.keys(layouts1).find(k => k.includes("sea-exp"));
    expect(seaExpKey).toBeDefined();

    // sea-imp variant 기준 렌더
    render(<TradePanel variant={BL_VARIANTS["sea-imp"]} />);
    const layouts2 = useFieldLayout.getState().layouts;
    const seaImpKey = Object.keys(layouts2).find(k => k.includes("sea-imp"));
    expect(seaImpKey).toBeDefined();

    expect(seaExpKey).not.toBe(seaImpKey);
  });
});
