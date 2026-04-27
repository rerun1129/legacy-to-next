import { render } from "@testing-library/react";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { useFieldLayout } from "@/lib/use-field-layout";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { BL_VARIANTS } from "@/lib/bl-variants";
import { PartyPanel } from "@/components/fms/house-bl/tabs/sections/party-panel";
import { SchedulePanel } from "@/components/fms/house-bl/tabs/sections/schedule-panel";
import { TradePanel } from "@/components/fms/house-bl/tabs/sections/trade-panel";
import { AirSchedulePanel } from "@/components/fms/house-bl/tabs/sections/air-schedule-panel";
import { AirTradePanel } from "@/components/fms/house-bl/tabs/sections/air-trade-panel";
import { AirCargoPanel } from "@/components/fms/house-bl/tabs/sections/air-cargo-panel";

// useCurrentUser는 zustand store — 직접 호출 시 { currentUserId } 구조분해 사용
vi.mock("@/lib/use-current-user", () => ({
  useCurrentUser: (selector?: (s: { currentUserId: string }) => unknown) => {
    const state = { currentUserId: "test-user" };
    return selector ? selector(state) : state;
  },
}));

describe("패널 위젯화 회귀 — FieldWidgetList 클래스 존재 검증", () => {
  beforeEach(() => {
    // 테스트 간 zustand store 격리
    useFieldLayout.setState({ layouts: {} });
    useWidgetLayout.setState({ layouts: {}, editMode: false, canEdit: true });
  });

  it("PartyPanel with sea-exp variant renders FieldWidgetList", () => {
    // PartyPanel의 실제 Props는 isExp: boolean
    render(<PartyPanel isExp={BL_VARIANTS["sea-exp"].direction === "EXP"} />);
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("SchedulePanel with sea-exp variant renders FieldWidgetList or FieldItemGrid", () => {
    render(<SchedulePanel variant={BL_VARIANTS["sea-exp"]} />);
    const hasWidgetList = document.querySelector(".field-widget-list") !== null;
    const hasItemGrid = document.querySelector(".field-item-grid") !== null;
    expect(hasWidgetList || hasItemGrid).toBe(true);
  });

  it("TradePanel with sea-exp variant renders FieldWidgetList", () => {
    // TradePanel은 props 없이 렌더
    render(<TradePanel />);
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("AirSchedulePanel with air-exp variant renders FieldWidgetList", () => {
    render(<AirSchedulePanel variant={BL_VARIANTS["air-exp"]} />);
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("AirTradePanel with air-exp variant renders FieldWidgetList", () => {
    render(<AirTradePanel variant={BL_VARIANTS["air-exp"]} />);
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("AirCargoPanel with air-exp variant renders FieldWidgetList", () => {
    // AirCargoPanel은 props 없이 렌더
    render(<AirCargoPanel />);
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });
});
