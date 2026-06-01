import { render } from "@testing-library/react";
import { describe, it, expect, beforeEach } from "vitest";
import { useFieldLayout } from "@/lib/use-field-layout";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { BL_VARIANTS } from "@/lib/bl-variants";
import { SchedulePanel } from "@/components/fms/house-bl/tabs/sections/schedule-panel";
import { PartyPanel } from "@/components/fms/house-bl/tabs/sections/party-panel";
import { TradePanel } from "@/components/fms/house-bl/tabs/sections/trade-panel";
import { Providers } from "@/test/render-with-providers";

describe("Scope 분리 회귀 — variant 별 별개 store key 생성 검증", () => {
  beforeEach(() => {
    // 테스트 간 zustand store 완전 초기화
    useFieldLayout.setState({ layouts: {} });
    useWidgetLayout.setState({ layouts: {}, editMode: false, canEdit: true });
  });

  it("SchedulePanel: sea-exp와 sea-imp는 별개 scope key를 생성한다", () => {
    // sea-exp 렌더 후 store key 확인
    const { unmount } = render(
      <SchedulePanel variant={BL_VARIANTS["sea-exp"]} />,
      { wrapper: Providers }
    );
    unmount();
    const layouts1 = useFieldLayout.getState().layouts;
    const seaExpKey = Object.keys(layouts1).find(k => k.includes("sea-exp"));
    expect(seaExpKey).toBeDefined();

    // sea-exp scope 키는 "schedule-panel." 로 시작한다 (user prefix 없음)
    // FieldWidgetList 중첩 스코프가 추가 키를 만들 수 있으므로 prefix 매칭으로 검증
    expect(seaExpKey).toMatch(/^schedule-panel\.sea-exp/);

    // sea-imp 렌더 후 store key 확인
    render(
      <SchedulePanel variant={BL_VARIANTS["sea-imp"]} />,
      { wrapper: Providers }
    );
    const layouts2 = useFieldLayout.getState().layouts;
    const seaImpKey = Object.keys(layouts2).find(k => k.includes("sea-imp"));
    expect(seaImpKey).toBeDefined();

    // sea-imp scope 키도 "schedule-panel." 로 시작한다 (user prefix 없음)
    expect(seaImpKey).toMatch(/^schedule-panel\.sea-imp/);

    // 두 key는 서로 달라야 한다
    expect(seaExpKey).not.toBe(seaImpKey);
  });

  it("PartyPanel: sea-exp와 air-exp는 별개 scope key를 생성한다", () => {
    // sea-exp variant 기준 렌더 (isExp: true)
    const { unmount } = render(
      <PartyPanel variant={BL_VARIANTS["sea-exp"]} isExp={true} />,
      { wrapper: Providers }
    );
    unmount();
    const layouts1 = useFieldLayout.getState().layouts;
    const seaExpKey = Object.keys(layouts1).find(k => k.includes("sea-exp"));
    expect(seaExpKey).toBeDefined();

    // party-panel scope 키는 "party-panel." 로 시작한다 (user prefix 없음)
    expect(seaExpKey).toMatch(/^party-panel\.sea-exp/);

    // air-exp variant 기준 렌더 (isExp: true)
    render(
      <PartyPanel variant={BL_VARIANTS["air-exp"]} isExp={true} />,
      { wrapper: Providers }
    );
    const layouts2 = useFieldLayout.getState().layouts;
    const airExpKey = Object.keys(layouts2).find(k => k.includes("air-exp"));
    expect(airExpKey).toBeDefined();

    // air-exp scope 키도 "party-panel." 로 시작한다 (user prefix 없음)
    expect(airExpKey).toMatch(/^party-panel\.air-exp/);

    expect(seaExpKey).not.toBe(airExpKey);
  });

  it("TradePanel: sea-exp와 sea-imp는 별개 scope key를 생성한다", () => {
    // sea-exp variant 기준 렌더
    const { unmount } = render(
      <TradePanel variant={BL_VARIANTS["sea-exp"]} />,
      { wrapper: Providers }
    );
    unmount();
    const layouts1 = useFieldLayout.getState().layouts;
    const seaExpKey = Object.keys(layouts1).find(k => k.includes("sea-exp"));
    expect(seaExpKey).toBeDefined();

    // trade-panel scope 키는 "trade-panel." 로 시작한다 (user prefix 없음)
    expect(seaExpKey).toMatch(/^trade-panel\.sea-exp/);

    // sea-imp variant 기준 렌더
    render(
      <TradePanel variant={BL_VARIANTS["sea-imp"]} />,
      { wrapper: Providers }
    );
    const layouts2 = useFieldLayout.getState().layouts;
    const seaImpKey = Object.keys(layouts2).find(k => k.includes("sea-imp"));
    expect(seaImpKey).toBeDefined();

    // sea-imp scope 키도 "trade-panel." 로 시작한다 (user prefix 없음)
    expect(seaImpKey).toMatch(/^trade-panel\.sea-imp/);

    expect(seaExpKey).not.toBe(seaImpKey);
  });
});
