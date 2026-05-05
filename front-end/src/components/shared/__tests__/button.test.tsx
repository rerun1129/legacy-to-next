import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { createRef } from "react";
import { Button } from "../button";

describe("Button", () => {
  it('variant="transaction" → className에 btn + btn--transaction 포함', () => {
    render(<Button variant="transaction" data-testid="btn">Label</Button>);
    const btn = screen.getByTestId("btn");
    expect(btn).toHaveClass("btn");
    expect(btn).toHaveClass("btn--transaction");
  });

  it('size="sm" → className에 btn--sm 포함', () => {
    render(<Button size="sm" data-testid="btn">Label</Button>);
    expect(screen.getByTestId("btn")).toHaveClass("btn--sm");
  });

  it("iconOnly → className에 btn--icon 포함", () => {
    render(<Button iconOnly data-testid="btn">+</Button>);
    expect(screen.getByTestId("btn")).toHaveClass("btn--icon");
  });

  it("loading → is-busy 클래스 + disabled 속성 + aria-busy + onClick 미호출", () => {
    const handleClick = vi.fn();
    render(
      <Button loading onClick={handleClick} data-testid="btn">
        Label
      </Button>
    );
    const btn = screen.getByTestId("btn");
    expect(btn).toHaveClass("is-busy");
    expect(btn).toBeDisabled();
    expect(btn).toHaveAttribute("aria-busy", "true");
    fireEvent.click(btn);
    expect(handleClick).not.toHaveBeenCalled();
  });

  it('kbd="⌘S" → <span class="btn__kbd">⌘S</span> 렌더', () => {
    render(<Button kbd="⌘S" data-testid="btn">Save</Button>);
    const kbdSpan = screen.getByTestId("btn").querySelector(".btn__kbd");
    expect(kbdSpan).not.toBeNull();
    expect(kbdSpan?.textContent).toBe("⌘S");
  });

  it("type 미지정 시 DOM type=button", () => {
    render(<Button data-testid="btn">Label</Button>);
    expect(screen.getByTestId("btn")).toHaveAttribute("type", "button");
  });

  it('type="submit" 명시 시 그대로 submit', () => {
    render(<Button type="submit" data-testid="btn">Submit</Button>);
    expect(screen.getByTestId("btn")).toHaveAttribute("type", "submit");
  });

  it("onMouseDown 패스스루 — 호출 횟수 검증", () => {
    const handleMouseDown = vi.fn();
    render(
      <Button onMouseDown={handleMouseDown} data-testid="btn">
        Label
      </Button>
    );
    fireEvent.mouseDown(screen.getByTestId("btn"));
    expect(handleMouseDown).toHaveBeenCalledTimes(1);
  });

  it("ref — forwardRef 동작 확인 (ref.current가 HTMLButtonElement)", () => {
    const ref = createRef<HTMLButtonElement>();
    render(<Button ref={ref} data-testid="btn">Label</Button>);
    expect(ref.current).toBeInstanceOf(HTMLButtonElement);
  });

  it('외부 className="extra" 추가 시 btn과 extra 동시 존재', () => {
    render(
      <Button className="extra" data-testid="btn">
        Label
      </Button>
    );
    const btn = screen.getByTestId("btn");
    expect(btn).toHaveClass("btn");
    expect(btn).toHaveClass("extra");
  });
});
