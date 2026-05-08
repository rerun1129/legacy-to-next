import { render, screen, fireEvent, act } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { useForm, FormProvider } from "react-hook-form";
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

  describe("controlled (name prop + FormProvider)", () => {
    function ResetTest({
      initialValue,
      resetValue,
    }: {
      initialValue: number | undefined;
      resetValue: number | undefined;
    }) {
      const methods = useForm({ defaultValues: { qty: initialValue } });
      return (
        <FormProvider {...methods}>
          <NumberBox name="qty" decimalPlaces={3} data-testid="nb" />
          <button onClick={() => methods.reset({ qty: resetValue })}>reset</button>
        </FormProvider>
      );
    }

    it("초기값 undefined → decimalPlaces=3이면 '0.000' 표시", async () => {
      render(<ResetTest initialValue={undefined} resetValue={undefined} />);
      await act(async () => {});
      const input = screen.getByTestId("nb") as HTMLInputElement;
      expect(input.value).toBe("0.000");
    });

    it("reset 클릭 시 사용자 입력값이 지워지고 '0.000'으로 복원", async () => {
      render(<ResetTest initialValue={undefined} resetValue={undefined} />);
      await act(async () => {});
      const input = screen.getByTestId("nb") as HTMLInputElement;

      // focus 상태에서 change해야 포맷팅 없이 raw 값 유지됨
      fireEvent.focus(input);
      fireEvent.change(input, { target: { value: "123" } });
      fireEvent.blur(input);
      await act(async () => {}); // blur 후 "123.000"으로 포맷팅

      fireEvent.click(screen.getByText("reset"));
      await act(async () => {});
      expect(input.value).toBe("0.000");
    });

    it("reset 시 특정 값(5)으로 복원", async () => {
      render(<ResetTest initialValue={undefined} resetValue={5} />);
      await act(async () => {});
      const input = screen.getByTestId("nb") as HTMLInputElement;

      fireEvent.change(input, { target: { value: "100" } });
      fireEvent.click(screen.getByText("reset"));
      await act(async () => {});
      expect(input.value).toBe("5.000");
    });
  });
});
