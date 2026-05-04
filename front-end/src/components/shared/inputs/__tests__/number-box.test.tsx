import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { NumberBox } from "../number-box";

describe("NumberBox", () => {
  it("type 속성이 number", () => {
    render(<NumberBox data-testid="nb" />);
    expect(screen.getByTestId("nb")).toHaveAttribute("type", "number");
  });

  it("step 속성 기본값이 1 (정수 전용)", () => {
    render(<NumberBox data-testid="nb" />);
    expect(screen.getByTestId("nb")).toHaveAttribute("step", "1");
  });

  it("variant=cell → grid__cell-input 클래스 포함", () => {
    render(<NumberBox variant="cell" data-testid="nb" />);
    expect(screen.getByTestId("nb")).toHaveClass("grid__cell-input");
  });

  describe("handleKeyDown", () => {
    it("+ 키 입력 시 preventDefault 호출", () => {
      render(<NumberBox data-testid="nb" />);
      const input = screen.getByTestId("nb");
      const event = new KeyboardEvent("keydown", { key: "+", bubbles: true });
      const spy = vi.spyOn(event, "preventDefault");
      input.dispatchEvent(event);
      expect(spy).toHaveBeenCalled();
    });

    it("- 키 입력 시 preventDefault 호출", () => {
      render(<NumberBox data-testid="nb" />);
      const input = screen.getByTestId("nb");
      const event = new KeyboardEvent("keydown", { key: "-", bubbles: true });
      const spy = vi.spyOn(event, "preventDefault");
      input.dispatchEvent(event);
      expect(spy).toHaveBeenCalled();
    });

    it("decimalPlaces 없을 때 . 키 입력 시 preventDefault 호출", () => {
      render(<NumberBox data-testid="nb" />);
      const input = screen.getByTestId("nb");
      const event = new KeyboardEvent("keydown", { key: ".", bubbles: true });
      const spy = vi.spyOn(event, "preventDefault");
      input.dispatchEvent(event);
      expect(spy).toHaveBeenCalled();
    });

    it("decimalPlaces=2일 때 . 키 입력 허용 (preventDefault 미호출)", () => {
      render(<NumberBox data-testid="nb" decimalPlaces={2} />);
      const input = screen.getByTestId("nb");
      const event = new KeyboardEvent("keydown", { key: ".", bubbles: true });
      const spy = vi.spyOn(event, "preventDefault");
      input.dispatchEvent(event);
      expect(spy).not.toHaveBeenCalled();
    });
  });

  describe("handleBlur", () => {
    it("decimalPlaces 없을 때 '000000' blur → value가 '0'으로 정규화", () => {
      render(<NumberBox />);
      const input = screen.getByRole("spinbutton") as HTMLInputElement;
      Object.defineProperty(input, "value", { configurable: true, writable: true, value: "000000" });
      fireEvent.blur(input);
      expect(input.value).toBe("0");
    });

    it("decimalPlaces 없을 때 '007' blur → value가 '7'으로 정규화", () => {
      render(<NumberBox />);
      const input = screen.getByRole("spinbutton") as HTMLInputElement;
      Object.defineProperty(input, "value", { configurable: true, writable: true, value: "007" });
      fireEvent.blur(input);
      expect(input.value).toBe("7");
    });

    it("decimalPlaces=2일 때 '1.5' blur → value가 '1.50'으로 포맷", () => {
      render(<NumberBox decimalPlaces={2} />);
      const input = screen.getByRole("spinbutton") as HTMLInputElement;
      Object.defineProperty(input, "value", { configurable: true, writable: true, value: "1.5" });
      fireEvent.blur(input);
      expect(input.value).toBe("1.50");
    });

    it("decimalPlaces=2일 때 '' blur → value가 '0.00'으로 채움", () => {
      render(<NumberBox decimalPlaces={2} />);
      const input = screen.getByRole("spinbutton") as HTMLInputElement;
      Object.defineProperty(input, "value", { configurable: true, writable: true, value: "" });
      fireEvent.blur(input);
      expect(input.value).toBe("0.00");
    });
  });

  describe("handlePaste", () => {
    it("decimalPlaces 없을 때 '1.5' paste → preventDefault 호출", () => {
      render(<NumberBox />);
      const input = screen.getByRole("spinbutton");
      const spy = vi.spyOn(Event.prototype, "preventDefault");
      fireEvent.paste(input, { clipboardData: { getData: () => "1.5" } });
      expect(spy).toHaveBeenCalled();
      spy.mockRestore();
    });

    it("decimalPlaces 없을 때 '-5' paste → preventDefault 호출", () => {
      render(<NumberBox />);
      const input = screen.getByRole("spinbutton");
      const spy = vi.spyOn(Event.prototype, "preventDefault");
      fireEvent.paste(input, { clipboardData: { getData: () => "-5" } });
      expect(spy).toHaveBeenCalled();
      spy.mockRestore();
    });

    it("decimalPlaces 없을 때 '+5' paste → preventDefault 호출", () => {
      render(<NumberBox />);
      const input = screen.getByRole("spinbutton");
      const spy = vi.spyOn(Event.prototype, "preventDefault");
      fireEvent.paste(input, { clipboardData: { getData: () => "+5" } });
      expect(spy).toHaveBeenCalled();
      spy.mockRestore();
    });

    it("decimalPlaces 없을 때 '123' paste → 허용", () => {
      render(<NumberBox />);
      const input = screen.getByRole("spinbutton");
      const spy = vi.spyOn(Event.prototype, "preventDefault");
      fireEvent.paste(input, { clipboardData: { getData: () => "123" } });
      expect(spy).not.toHaveBeenCalled();
      spy.mockRestore();
    });

    it("decimalPlaces=2일 때 '1.5' paste → 허용", () => {
      render(<NumberBox decimalPlaces={2} />);
      const input = screen.getByRole("spinbutton");
      const spy = vi.spyOn(Event.prototype, "preventDefault");
      fireEvent.paste(input, { clipboardData: { getData: () => "1.5" } });
      expect(spy).not.toHaveBeenCalled();
      spy.mockRestore();
    });

    it("decimalPlaces=2일 때 '-1.5' paste → preventDefault 호출 (부호 차단)", () => {
      render(<NumberBox decimalPlaces={2} />);
      const input = screen.getByRole("spinbutton");
      const spy = vi.spyOn(Event.prototype, "preventDefault");
      fireEvent.paste(input, { clipboardData: { getData: () => "-1.5" } });
      expect(spy).toHaveBeenCalled();
      spy.mockRestore();
    });
  });
});
