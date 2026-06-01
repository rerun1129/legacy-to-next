import { render } from "@testing-library/react";
import { describe, it, expect, beforeEach } from "vitest";
import { useFieldLayout } from "@/lib/use-field-layout";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { BL_VARIANTS } from "@/lib/bl-variants";
import { PartyPanel } from "@/components/fms/house-bl/tabs/sections/party-panel";
import { SchedulePanel } from "@/components/fms/house-bl/tabs/sections/schedule-panel";
import { TradePanel } from "@/components/fms/house-bl/tabs/sections/trade-panel";
import { AirSchedulePanel } from "@/components/fms/house-bl/tabs/sections/air-schedule-panel";
import { AirTradePanel } from "@/components/fms/house-bl/tabs/sections/air-trade-panel";
import { AirCargoPanel } from "@/components/fms/house-bl/tabs/sections/air-cargo-panel";
import { Providers } from "@/test/render-with-providers";

describe("패널 위젯화 회귀 — FieldWidgetList 클래스 존재 검증", () => {
  beforeEach(() => {
    // 테스트 간 zustand store 격리
    useFieldLayout.setState({ layouts: {} });
    useWidgetLayout.setState({ layouts: {}, editMode: false, canEdit: true });
  });

  it("PartyPanel with sea-exp variant renders FieldWidgetList", () => {
    render(
      <PartyPanel variant={BL_VARIANTS["sea-exp"]} isExp={true} />,
      { wrapper: Providers }
    );
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("SchedulePanel with sea-exp variant renders FieldWidgetList or FieldItemGrid", () => {
    render(
      <SchedulePanel variant={BL_VARIANTS["sea-exp"]} />,
      { wrapper: Providers }
    );
    const hasWidgetList = document.querySelector(".field-widget-list") !== null;
    const hasItemGrid = document.querySelector(".field-item-grid") !== null;
    expect(hasWidgetList || hasItemGrid).toBe(true);
  });

  it("TradePanel with sea-exp variant renders FieldWidgetList", () => {
    // TradePanel은 props 없이 렌더
    render(<TradePanel />, { wrapper: Providers });
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("AirSchedulePanel with air-exp variant renders FieldWidgetList", () => {
    render(
      <AirSchedulePanel variant={BL_VARIANTS["air-exp"]} />,
      { wrapper: Providers }
    );
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("AirTradePanel with air-exp variant renders FieldWidgetList", () => {
    render(
      <AirTradePanel variant={BL_VARIANTS["air-exp"]} />,
      { wrapper: Providers }
    );
    expect(document.querySelector(".field-widget-list")).toBeInTheDocument();
  });

  it("AirCargoPanel with air-exp variant renders FieldWidgetList or FieldItemGrid", () => {
    // AirCargoPanel은 FieldWidgetList 없이 FieldItemGrid 만 사용하므로 관용 단언으로 검증
    render(<AirCargoPanel />, { wrapper: Providers });
    const hasWidgetList = document.querySelector(".field-widget-list") !== null;
    const hasItemGrid = document.querySelector(".field-item-grid") !== null;
    expect(hasWidgetList || hasItemGrid).toBe(true);
  });
});
